package im.threads.ui.controllers

import androidx.fragment.app.Fragment
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna.info
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.UpcomingUserMessage
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.HistoryLoader
import im.threads.ui.activities.QuickAnswerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Контроллер для диалога с быстрыми ответами
 */
class QuickAnswerController : Fragment() {
    private val database: DatabaseHolder by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun onBind(activity: QuickAnswerActivity) {
        info("onBind in " + QuickAnswerController::class.java.simpleName)
        ChatController.getInstance().loadHistoryAfterWithLastMessageCheck(
            applyUiChanges = false,
            forceLoad = true,
            fromQuickAnswerController = true,
            callback = object : HistoryLoader.HistoryLoadingCallback {
                override fun onLoaded(items: List<ChatItem>) {
                    coroutineScope.launch {
                        val lastConsultPhraseDef = async(Dispatchers.IO) {
                            database.putChatItems(items)
                            val uuidList: List<String?> = database.getUnreadMessagesUuid()
                            if (uuidList.isNotEmpty()) {
                                BaseConfig.getInstance().transport.markMessagesAsRead(uuidList)
                            }
                            items.last { it is ConsultPhrase } as? ConsultPhrase
                        }
                        if (isActive) {
                            val lastConsultPhrase = lastConsultPhraseDef.await()
                            activity.setLastUnreadMessage(lastConsultPhrase)
                        }
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

    fun onUserAnswer(upcomingUserMessage: UpcomingUserMessage) {
        info("onUserAnswer")
        val cc = ChatController.getInstance()
        cc.onUserInput(upcomingUserMessage)
        cc.setAllMessagesWereRead()
    }

    companion object {
        @JvmStatic
        val instance: QuickAnswerController
            get() = QuickAnswerController()
    }
}
