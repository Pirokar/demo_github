package im.threads.ui.adapters.utils

import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.models.MessageStatus
import im.threads.business.models.UserPhrase
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.threadsGate.responses.Status
import im.threads.ui.adapters.ChatAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.Date

/**
 * Проверяет через определенный интервал статусы отправленных сообщений.
 * Выставляет failed, если статус не ушел дальше Sending
 * @param chatAdapterRef слабая ссылка на адаптер для управления статусами сообщений
 * @param interval интервал в мс для проверки статусов
 */
class SendingStatusObserver(private val chatAdapterRef: WeakReference<ChatAdapter>, private val interval: Long) {
    private val database: DatabaseHolder by inject()
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var isObserving = false

    /**
     * Начинает проверять статус отправки с указанным при создании класса интервалом.
     * Выставляет failed, если статус не ушел дальше Sending по окончанию интервала.
     * Не будет работать, если был вызван "finishObserving" ранее.
     */
    fun startObserving() {
        if (!isObserving) {
            isObserving = true
            coroutineScope.launch {
                while (true) {
                    delay(1000)
                    if (isObserving && isActive) {
                        val observedMessagesDef = async(Dispatchers.IO) {
                            chatAdapterRef.get()?.list?.let { list ->
                                val indexesToUpdate = ArrayList<Int>()
                                list.indices.forEach { index ->
                                    (list[index] as? UserPhrase)?.let { item ->
                                        val thisTime = Date().time
                                        val notDeliveredStatus = item.sentState == MessageStatus.SENDING ||
                                            item.sentState == MessageStatus.SENT
                                        if (notDeliveredStatus && thisTime - item.timeStamp > interval) {
                                            val failedStatus = MessageStatus.FAILED
                                            database.setStateOfUserPhraseByCorrelationId(item.id, failedStatus)
                                            item.sentState = failedStatus
                                            indexesToUpdate.add(index)
                                            withContext(Dispatchers.Main) {
                                                chatUpdateProcessor.postOutgoingMessageStatusChanged(
                                                    listOf(Status(item.id, item.backendMessageId, MessageStatus.FAILED))
                                                )
                                            }
                                        }
                                    }
                                }
                                indexesToUpdate
                            }
                        }
                        observedMessagesDef.await()?.forEach { indexToUpdate ->
                            chatAdapterRef.get()?.notifyItemChanged(indexToUpdate)
                        }
                    } else {
                        break
                    }
                }
            }
        }
    }

    /**
     * Приостанавливает проверку статусов. Можно использовать при сворачивании приложения.
     * Вызов "startObserving" возобновит наблюдение.
     */
    fun pauseObserving() {
        isObserving = false
    }

    /**
     * Завершает проверку статусов. Можно использовать при уничтожении адаптера вместе с его view.
     * Вызов "startObserving" более не даст эффекта.
     */
    fun finishObserving() {
        isObserving = false
        coroutineScope.cancel()
    }
}
