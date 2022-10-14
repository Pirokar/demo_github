package im.threads.business.utils.messenger

import android.os.Handler
import android.os.Looper
import android.os.Message
import im.threads.business.UserInfoBuilder
import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.logger.LoggerEdna.debug
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.logger.LoggerEdna.info
import im.threads.business.models.ChatItem
import im.threads.business.models.ChatItemSendErrorModel
import im.threads.business.models.ConsultInfo
import im.threads.business.models.MessageState
import im.threads.business.models.UserPhrase
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.HistoryLoader.getHistorySync
import im.threads.business.transport.HistoryParser
import im.threads.business.utils.ConsultWriter
import im.threads.business.utils.getErrorStringResByCode
import im.threads.business.utils.internet.NetworkInteractor
import im.threads.business.utils.internet.NetworkInteractorImpl
import im.threads.business.utils.postFile
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessengerImpl(private var compositeDisposable: CompositeDisposable?) : Messenger {
    private val context = BaseConfig.instance.context
    private val transport = BaseConfig.instance.transport
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private var isDownloadingMessages = false
    private var isAllMessagesDownloaded = false
    private var lastMessageTimestamp = 0L
    private val unsentMessages: ArrayList<UserPhrase> = ArrayList()
    private val sendQueue: ArrayList<UserPhrase> = ArrayList()
    private var unsentMessageHandler: Handler? = null
    private val networkInteractor: NetworkInteractor = NetworkInteractorImpl()
    private val mainCoroutineScope = CoroutineScope(Dispatchers.Main)
    private val preferences: Preferences by inject()
    private val database: DatabaseHolder by inject()

    private val resendMessageKey = 123
    var pageItemsCount = 100

    override val resendStream = PublishSubject.create<String>()

    override fun sendMessage(userPhrase: UserPhrase) {
        info("sendMessage: $userPhrase")
        val consultWriter = ConsultWriter(preferences)
        var consultInfo: ConsultInfo? = null

        if (null != userPhrase.quote && userPhrase.quote.isFromConsult) {
            userPhrase.quote.quotedPhraseConsultId?.let { id ->
                consultInfo = consultWriter.getConsultInfo(id)
            }
        }
        if (!userPhrase.hasFile()) {
            sendTextMessage(userPhrase, consultInfo)
        } else {
            sendFileMessage(userPhrase, consultInfo)
        }
    }

    override fun downloadMessagesTillEnd(): Single<List<ChatItem>> {
        return Single.fromCallable<List<ChatItem>> {
            synchronized(this) {
                if (!isDownloadingMessages) {
                    isDownloadingMessages = true
                    debug(ThreadsApi.REST_TAG, "downloadMessagesTillEnd")
                    while (!isAllMessagesDownloaded) {
                        val response = getHistorySync(lastMessageTimestamp, pageItemsCount)
                        val serverItems = HistoryParser.getChatItems(response)
                        if (serverItems.isEmpty()) {
                            isAllMessagesDownloaded = true
                        } else {
                            lastMessageTimestamp = serverItems[0].timeStamp
                            isAllMessagesDownloaded =
                                serverItems.size < pageItemsCount
                            saveMessages(serverItems)
                        }
                    }
                }
                debug(ThreadsApi.REST_TAG, "Messages are loaded")
                isDownloadingMessages = false
                return@fromCallable database.getChatItems(0, -1)
            }
        }
            .doOnError { throwable: Throwable? -> isDownloadingMessages = false }
    }

    override fun saveMessages(chatItems: List<ChatItem>) {
        database.putChatItems(chatItems)
    }

    override fun clearSendQueue() {
        sendQueue.clear()
    }

    override fun recreateUnsentMessagesWith(phrases: List<UserPhrase>) {
        unsentMessages.clear()
        unsentMessages.addAll(phrases)
    }

    override fun queueMessageSending(userPhrase: UserPhrase) {
        info("queueMessageSending: $userPhrase")
        synchronized(sendQueue) { sendQueue.add(userPhrase) }
        if (sendQueue.size == 1) {
            sendMessage(userPhrase)
        }
    }

    override fun resendMessages() {
        mainCoroutineScope.launch {
            unsentMessageHandler = Handler(Looper.getMainLooper()) { msg: Message ->
                if (msg.what == resendMessageKey) {
                    if (!unsentMessages.isEmpty()) {
                        if (networkInteractor.hasNoInternet(context)) {
                            scheduleResend()
                        } else {
                            // try to send all unsent messages
                            unsentMessageHandler?.removeMessages(resendMessageKey)
                            synchronized(unsentMessages) {
                                val iterator: MutableListIterator<UserPhrase> = unsentMessages.listIterator()
                                while (iterator.hasNext()) {
                                    val phrase = iterator.next()
                                    checkAndResendPhrase(phrase)
                                    iterator.remove()
                                }
                            }
                        }
                    }
                }
                false
            }
        }
    }

    override fun scheduleResend() {
        if (unsentMessageHandler?.hasMessages(resendMessageKey) == false) {
            val resendInterval = BaseConfig.instance.requestConfig.socketClientSettings
                .resendIntervalMillis
            unsentMessageHandler?.sendEmptyMessageDelayed(resendMessageKey, resendInterval.toLong())
        }
    }

    override fun proceedSendingQueue(chatItem: UserPhrase) {
        synchronized(sendQueue) {
            val iterator = sendQueue.iterator()
            while (iterator.hasNext()) {
                val queueItem = iterator.next()
                if (queueItem.isTheSameItem(chatItem)) {
                    iterator.remove()
                    break
                }
            }
        }
        if (sendQueue.isNotEmpty()) {
            sendMessage(sendQueue[0])
        }
    }

    override fun addMsgToResendQueue(userPhrase: UserPhrase) {
        if (!unsentMessages.contains(userPhrase)) {
            unsentMessages.add(userPhrase)
            scheduleResend()
        }
    }

    override fun forceResend(userPhrase: UserPhrase) {
        if (userPhrase.sentState == MessageState.STATE_NOT_SENT) {
            synchronized(unsentMessages) {
                unsentMessages.removeAll { it.isTheSameItem(userPhrase) }
                checkAndResendPhrase(userPhrase)
            }
        }
    }

    private fun checkAndResendPhrase(userPhrase: UserPhrase) {
        if (userPhrase.sentState == MessageState.STATE_NOT_SENT) {
            userPhrase.providerId?.run { resendStream.onNext(this) }
            queueMessageSending(userPhrase)
        }
    }

    private fun sendTextMessage(userPhrase: UserPhrase, consultInfo: ConsultInfo?) {
        info("sendTextMessage: $userPhrase, $consultInfo")
        BaseConfig.instance.transport.sendMessage(userPhrase, consultInfo, null, null)
    }

    private fun sendFileMessage(userPhrase: UserPhrase, consultInfo: ConsultInfo?) {
        info("sendFileMessage: $userPhrase, $consultInfo")
        val fileDescription = userPhrase.fileDescription
        val quoteFileDescription = if (userPhrase.quote != null) userPhrase.quote.fileDescription else null
        subscribe(
            Completable.fromAction {
                var filePath: String? = null
                var quoteFilePath: String? = null
                val clientId = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)?.clientId
                if (fileDescription != null) {
                    filePath = postFile(fileDescription, clientId)
                }
                if (quoteFileDescription != null) {
                    quoteFilePath = postFile(quoteFileDescription, clientId)
                }
                transport.sendMessage(userPhrase, consultInfo, filePath, quoteFilePath)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {}
                ) { e: Throwable ->
                    val errorCode = getErrorStringResByCode(e.message!!)
                    val message: String = context.getString(errorCode)
                    val model = ChatItemSendErrorModel(
                        null,
                        userPhrase.id,
                        message
                    )
                    chatUpdateProcessor.postChatItemSendError(model)
                    error(e)
                }
        )
    }

    private fun subscribe(event: Disposable): Boolean {
        if (compositeDisposable?.isDisposed == false) {
            compositeDisposable = CompositeDisposable()
        }
        return compositeDisposable?.run { add(event) } ?: false
    }
}
