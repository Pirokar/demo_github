package im.threads.business.controllers

import im.threads.business.models.ConsultPhrase
import im.threads.business.models.ConsultRole
import im.threads.business.models.UserPhrase
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import io.reactivex.processors.BehaviorProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class UnreadMessagesController {
    INSTANCE;

    private val database: DatabaseHolder by inject()
    private val preferences: Preferences by inject()

    private val scope = CoroutineScope(Dispatchers.IO)

    val unreadMessagesPublishProcessor = BehaviorProcessor.create<Int>()

    private var unreadPushCount: Int
        get() = preferences.get(PreferencesCoreKeys.UNREAD_PUSH_COUNT) ?: 0
        set(value) { preferences.save(PreferencesCoreKeys.UNREAD_PUSH_COUNT, value) }

    fun incrementUnreadPush() {
        unreadPushCount++
        refreshUnreadMessagesCount()
    }

    fun clearUnreadPush() {
        unreadPushCount = 0
        refreshUnreadMessagesCount()
    }

    /**
     * Оповещает об изменении количества непрочитанных сообщений.
     * Срабатывает при показе пуш уведомления в Статус Баре и
     * при прочтении сообщений, если в истории есть сообщения не только от чат ботов.
     * Все места, где срабатывает прочтение сообщений, можно найти по
     * NotificationService.BROADCAST_ALL_MESSAGES_WERE_READ.
     * Данный тип сообщения отправляется в сервис пуш уведомлений при прочтении сообщений.
     */
    fun refreshUnreadMessagesCount() {
        executeIfHumanMessageFound { updateUnreadMessagesProcessor() }
    }

    private fun updateUnreadMessagesProcessor() {
        unreadMessagesPublishProcessor.onNext(unreadMessages)
    }

    private fun executeIfHumanMessageFound(block: () -> Unit) {
        scope.launch {
            val itemsToGet = 50
            var itemsOffset = 0

            while (true) {
                val items = database.getChatItems(itemsOffset, itemsToGet)
                if (items.isEmpty()) return@launch
                itemsOffset += items.size

                items.forEach { item ->
                    when (item) {
                        is ConsultPhrase -> {
                            if (item.role == ConsultRole.OPERATOR || item.role == ConsultRole.SUPERVISOR) {
                                withContext(Dispatchers.Main) { block() }
                                return@launch
                            }
                        }
                        is UserPhrase -> {
                            withContext(Dispatchers.Main) { block() }
                            return@launch
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    val unreadMessages: Int
        get() = database.getUnreadMessagesCount() + unreadPushCount
}
