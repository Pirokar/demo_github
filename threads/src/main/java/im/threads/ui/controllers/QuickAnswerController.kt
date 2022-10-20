package im.threads.ui.controllers

import androidx.fragment.app.Fragment
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.logger.LoggerEdna.info
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.UpcomingUserMessage
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.HistoryLoader.getHistorySync
import im.threads.business.transport.HistoryParser
import im.threads.ui.activities.QuickAnswerActivity
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Контроллер для диалога с быстрыми ответами
 */
class QuickAnswerController : Fragment() {
    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()
    private val database: DatabaseHolder by inject()

    fun onBind(activity: QuickAnswerActivity) {
        info("onBind in " + QuickAnswerController::class.java.simpleName)
        ChatController.getInstance().loadHistory()
        compositeDisposable?.add(
            Single.fromCallable {
                HistoryParser.getChatItems(
                    getHistorySync(100, true)
                )
            }
                .doOnSuccess { chatItems: List<ChatItem>? ->
                    database.putChatItems(chatItems)
                    val uuidList: List<String?> = database.getUnreadMessagesUuid()
                    if (uuidList.isNotEmpty()) {
                        BaseConfig.instance.transport.markMessagesAsRead(uuidList)
                    }
                }
                .flatMap<ConsultPhrase> { database.lastConsultPhrase }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { consultPhrase ->
                        if (consultPhrase != null) {
                            activity.setLastUnreadMessage(consultPhrase)
                        }
                    }
                ) { e: Throwable? -> error("onBind", e) }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (compositeDisposable != null) {
            compositeDisposable?.dispose()
            compositeDisposable = null
        }
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
