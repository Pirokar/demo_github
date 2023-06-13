package im.threads.business.transport

import androidx.lifecycle.Lifecycle
import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.logger.LoggerEdna.info
import im.threads.business.models.ConsultInfo
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.rest.queries.BackendApi.Companion.get
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.serviceLocator.core.inject
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Предоставляет базовые методы для получения и отправки данных через веб-сокет
 */
abstract class Transport {
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private var compositeDisposable: CompositeDisposable? = null

    fun markMessagesAsRead(uuidList: List<String?>) {
        info(ThreadsApi.REST_TAG, "markMessagesAsRead : $uuidList")
        subscribe(
            Completable.fromAction { get().markMessageAsRead(uuidList)?.execute() }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        info(
                            ThreadsApi.REST_TAG,
                            "messagesAreRead : $uuidList"
                        )
                        for (messageId in uuidList) {
                            if (messageId != null) {
                                chatUpdateProcessor.postIncomingMessageWasRead(messageId)
                            }
                        }
                    }
                ) { e: Throwable ->
                    info(
                        ThreadsApi.REST_TAG,
                        "error on messages read : $uuidList"
                    )
                    val message = if (e.localizedMessage.isNullOrBlank()) e.message else e.localizedMessage
                    chatUpdateProcessor.postError(TransportException(message))
                }
        )
    }

    private fun subscribe(event: Disposable): Boolean {
        if (compositeDisposable == null || compositeDisposable?.isDisposed == true) {
            compositeDisposable = CompositeDisposable()
        }
        return compositeDisposable?.add(event) == true
    }

    abstract fun buildTransport()
    abstract fun sendRatingDone(survey: Survey)
    abstract fun sendResolveThread(approveResolve: Boolean)
    abstract fun sendUserTying(input: String)
    abstract fun sendRegisterDevice(forceRegistration: Boolean)
    abstract fun sendInitMessages()
    abstract fun closeWebSocket()
    internal abstract fun updatePushToken()

    /**
     * TODO THREADS-6292: this method can potentially lead to messages stuck in STATE_SENDING
     */
    abstract fun sendMessage(userPhrase: UserPhrase, consultInfo: ConsultInfo?, filePath: String?, quoteFilePath: String?): Boolean
    abstract fun sendClientOffline(clientId: String)
    abstract fun updateLocation(latitude: Double, longitude: Double)
    abstract fun getToken(): String
    abstract fun setLifecycle(lifecycle: Lifecycle?)
}
