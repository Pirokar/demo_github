package im.threads.ui.controllers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.MainThread
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import im.threads.R
import im.threads.business.broadcastReceivers.ProgressReceiver
import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.controllers.UnreadMessagesController
import im.threads.business.core.ContextHolder
import im.threads.business.core.ThreadsLibBase
import im.threads.business.extensions.withMainContext
import im.threads.business.formatters.ChatItemType
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.logger.LoggerEdna.info
import im.threads.business.logger.LoggerEdna.warning
import im.threads.business.models.CampaignMessage
import im.threads.business.models.ChatItem
import im.threads.business.models.ChatItemSendErrorModel
import im.threads.business.models.ChatPhrase
import im.threads.business.models.ClientNotificationDisplayType
import im.threads.business.models.ConsultChatPhrase
import im.threads.business.models.ConsultConnectionMessage
import im.threads.business.models.ConsultInfo
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.ConsultTyping
import im.threads.business.models.FileDescription
import im.threads.business.models.Hidable
import im.threads.business.models.InputFieldEnableModel
import im.threads.business.models.MessageRead
import im.threads.business.models.MessageStatus
import im.threads.business.models.QuickReplyItem
import im.threads.business.models.RequestResolveThread
import im.threads.business.models.ScheduleInfo
import im.threads.business.models.SearchingConsult
import im.threads.business.models.SimpleSystemMessage
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.Survey
import im.threads.business.models.SystemMessage
import im.threads.business.models.UpcomingUserMessage
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.AttachmentStateEnum
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.models.SettingsResponse
import im.threads.business.rest.queries.BackendApi.Companion.get
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.state.ChatState
import im.threads.business.state.ChatStateEnum
import im.threads.business.transport.ChatItemProviderData
import im.threads.business.transport.HistoryLoader
import im.threads.business.transport.HistoryParser
import im.threads.business.transport.TransportException
import im.threads.business.transport.models.Attachment
import im.threads.business.transport.threadsGate.responses.Status
import im.threads.business.utils.ChatMessageSeeker
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.ConsultWriter
import im.threads.business.utils.DateHelper
import im.threads.business.utils.DemoModeProvider
import im.threads.business.utils.FileUtils.getMimeType
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.FileUtils.isVoiceMessage
import im.threads.business.utils.getDuration
import im.threads.business.utils.messenger.Messenger
import im.threads.business.utils.messenger.MessengerImpl
import im.threads.business.workers.FileDownloadWorker.Companion.startDownload
import im.threads.ui.activities.ConsultActivity.Companion.startActivity
import im.threads.ui.activities.ImagesActivity.Companion.getStartIntent
import im.threads.ui.config.Config
import im.threads.ui.fragments.ChatFragment
import im.threads.ui.preferences.PreferencesUiKeys
import im.threads.ui.utils.runOnUiThread
import im.threads.ui.views.formatAsDuration
import im.threads.ui.workers.NotificationWorker.Companion.removeNotification
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Collections
import java.util.concurrent.TimeUnit

/**
 * controller for chat Fragment. all bells and whistles in fragment,
 * all work here
 * don't forget to unbindFragment() in ChatFragment onDestroy, to avoid leaks;
 */
class ChatController private constructor() {
    val messageErrorProcessor = PublishSubject.create<Long>()
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private val database: DatabaseHolder by inject()
    private val consultWriter: ConsultWriter by inject()
    private val chatStyle = Config.getInstance().chatStyle
    private val appContext: Context by inject()
    private val preferences: Preferences by inject()
    private val historyLoader: HistoryLoader by inject()
    private val clientUseCase: ClientUseCase by inject()
    private val chatState: ChatState by inject()
    private val demoModeProvider: DemoModeProvider by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // this flag is keeping the visibility state of the request to resolve thread
    private var surveyCompletionInProgress = false
    private val surveyCompletionProcessor = PublishProcessor.create<Survey>()

    // Ссылка на фрагмент, которым управляет контроллер
    private var fragment: ChatFragment? = null

    // Для приема сообщений из сервиса по скачиванию файлов
    private var progressReceiver: ProgressReceiver? = null

    // keep an active and visible for user survey id
    private var activeSurvey: Survey? = null
    private var isActive = false
    private var lastItems: List<ChatItem> = ArrayList()

    // TODO: вынести в отдельный класс поиск сообщений
    private var seeker = ChatMessageSeeker()
    private var lastFancySearchDate: Long = 0
    private var lastSearchQuery: String? = ""
    internal var isAllMessagesDownloaded = false
    private var isDownloadingMessages = false

    var firstUnreadUuidId: String? = null
        private set

    // На основе этих переменных определяется возможность отправки сообщений в чат
    private var currentScheduleInfo: ScheduleInfo? = null

    // Если пользователь не ответил на вопрос (quickReply), то блокируем поле ввода
    private var hasQuickReplies = false

    // Если пользователь не ответил на вопрос (quickReply), то блокируем поле ввода
    private var inputEnabledDuringQuickReplies = chatStyle.inputEnabledDuringQuickReplies
    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()
    private val messenger: Messenger = MessengerImpl(compositeDisposable, clientUseCase)
    private val localUserMessages = ArrayList<UserPhrase>()
    private val attachmentsHistory = HashMap<String, AttachmentStateEnum>()

    private var enableModel: InputFieldEnableModel? = null

    init {
        subscribeToChatEvents()
    }

    fun onViewStart() {
        checkStateOnViewStart()
        messenger.onViewStart()
        checkEmptyStateVisibility()
    }

    fun onViewStop() {
        isAllMessagesDownloaded = false
        messenger.onViewStop()
    }

    fun onViewDestroy() {
        messenger.onViewDestroy()
    }

    internal fun onRatingClick(survey: Survey) {
        if (!surveyCompletionInProgress) {
            surveyCompletionInProgress = true
            subscribeToSurveyCompletion()
        }
        surveyCompletionProcessor.onNext(survey)
    }

    internal fun onResolveThreadClick(approveResolve: Boolean) {
        BaseConfig.getInstance().transport.sendResolveThread(approveResolve)
    }

    internal fun onUserTyping(input: String?) {
        if (input != null && isChatReady()) {
            BaseConfig.getInstance().transport.sendUserTying(input)
        }
    }

    internal fun onUserInput(upcomingUserMessage: UpcomingUserMessage) {
        removeResolveRequest()
        // If user has written a message while the active survey is visible
        // we should make invisible the survey
        removeActiveSurvey()
        val um = convert(upcomingUserMessage)
        if (um.fileDescription != null) {
            localUserMessages.add(um)
        }
        addMessage(um)
        messenger.queueMessageSending(um)
    }

    internal fun onFileClick(fileDescription: FileDescription) {
        info("onFileClick $fileDescription")
        if (fragment?.isAdded == true) {
            val activity: Activity? = fragment?.activity
            if (activity != null) {
                if (fileDescription.fileUri == null) {
                    startDownload(activity, fileDescription)
                } else if (isImage(fileDescription)) {
                    fragment?.setupStartSecondLevelScreen()
                    activity.startActivity(getStartIntent(activity, fileDescription))
                } else {
                    val target = Intent(Intent.ACTION_VIEW)
                    target.setDataAndType(fileDescription.fileUri, getMimeType(fileDescription))
                    target.flags =
                        Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    try {
                        fragment?.setupStartSecondLevelScreen()
                        activity.startActivity(target)
                    } catch (e: ActivityNotFoundException) {
                        fragment?.showBalloon("No application support this type of file")
                    }
                }
            }
        }
    }

    internal fun setActivityIsForeground(isForeground: Boolean) {
        info("setActivityIsForeground")
        isActive = isForeground
        if (isForeground && fragment?.isAdded == true) {
            val cm =
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnectedOrConnecting) {
                val uuidList = database.getUnreadMessagesUuid()
                firstUnreadUuidId = if (uuidList.isNotEmpty()) {
                    BaseConfig.getInstance().transport.markMessagesAsRead(uuidList)
                    uuidList[0] // для скролла к первому непрочитанному сообщению
                } else {
                    null
                }
            }
        }
        subscribe(
            Observable.timer(1500, TimeUnit.MILLISECONDS)
                .filter { isActive }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    removePushNotification()
                    UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount()
                }) { obj: Throwable -> obj.message }
        )
    }

    private fun checkStateOnViewStart() {
        if (chatState.getCurrentState() < ChatStateEnum.REGISTERING_DEVICE) {
            BaseConfig.getInstance().transport.sendRegisterDevice(false)
        }
    }

    private fun updateDoubleItems(serverItems: ArrayList<ChatItem>) {
        updateServerItemsBySendingItems(serverItems, getSendingItems())
    }

    internal fun getUnreadMessagesCount() = database.getUnreadMessagesCount()

    internal fun setMessageAsRead(item: ChatItem?, async: Boolean = true) {
        if (item is ConsultPhrase) {
            if (async) {
                coroutineScope.launch(Dispatchers.IO) { database.setMessageWasRead(item.id) }
            } else {
                database.setMessageWasRead(item.id)
            }
        }
    }

    internal fun isChatReady() = chatState.isChatReady()

    internal fun onRetryInitChatClick() {
        fragment?.hideErrorView()
        fragment?.showProgressBar()

        val state = chatState.getCurrentState()
        val transport = BaseConfig.getInstance().transport
        if (state < ChatStateEnum.DEVICE_REGISTERED) {
            transport.sendRegisterDevice(false)
        } else if (state < ChatStateEnum.INIT_USER_SENT) {
            transport.sendInitMessages()
        } else if (state < ChatStateEnum.ATTACHMENT_SETTINGS_LOADED) {
            loadSettings()
        }
    }

    private fun getSendingItems(): ArrayList<UserPhrase> {
        return database.getSendingChatItems() as ArrayList<UserPhrase>
    }

    private fun updateServerItemsBySendingItems(
        serverItems: ArrayList<ChatItem>,
        sendingItems: ArrayList<UserPhrase>
    ) {
        sendingItems.forEach { notSendedItem ->
            serverItems.forEach { serverItem ->
                if (serverItem is UserPhrase) {
                    if (notSendedItem.timeStamp == serverItem.timeStamp) {
                        serverItem.fileDescription?.fileUri = notSendedItem.fileDescription?.fileUri
                        sendingItems.remove(notSendedItem)
                        database.updateChatItemByTimeStamp(serverItem)
                    }
                }
            }
        }
        serverItems.addAll(sendingItems)
    }

    internal fun onFileDownloadRequest(fileDescription: FileDescription?) {
        if (fragment?.isAdded == true) {
            fragment?.activity?.let {
                if (fileDescription != null) {
                    startDownload(it, fileDescription, true)
                }
            }
        }
    }

    internal fun onConsultChoose(activity: Activity?, consultId: String?) {
        if (consultId == null) {
            warning("Can't show consult info: consultId == null")
        } else {
            val info = database.getConsultInfo(consultId)
            if (info != null) {
                startActivity(activity, info)
            } else {
                startActivity(activity)
            }
        }
    }

    internal fun onSearchResultsClick(uuid: String, date: String?) {
        val position = getItemPositionByUuid(fragment?.elements, uuid)
        if (position >= 0) {
            fragment?.scrollToPosition(position, true)
        } else {
            fragment?.showBalloon(R.string.ecc_history_loading_message)
            val dateTimestamp = DateHelper.getMessageTimestampFromDateString(date)
            loadHistory(
                dateTimestamp + 1,
                untilCondition = { items ->
                    val isItemsOutdated = items.isEmpty() || items.first().timeStamp < dateTimestamp
                    isItemsOutdated || getItemPositionByUuid(items, uuid) >= 0
                },
                callback = object : HistoryLoader.HistoryLoadingCallback {
                    override fun onLoaded(items: List<ChatItem>) {
                        coroutineScope.launch(Dispatchers.Main) {
                            delay(500)
                            val itemPosition = getItemPositionByUuid(fragment?.elements, uuid)
                            if (itemPosition >= 0) {
                                fragment?.scrollToPosition(itemPosition, true)
                            } else {
                                fragment?.showBalloon(R.string.ecc_request_failed)
                            }
                        }
                    }
                }
            )
        }
    }

    private fun getItemPositionByUuid(items: List<ChatItem>?, uuid: String): Int {
        val notNullItems = items ?: listOf()
        val item = notNullItems
            .mapNotNull { it as? ChatPhrase }
            .firstOrNull { it.id == uuid }
        return fragment?.elements?.indexOfFirst { it.timeStamp == item?.timeStamp } ?: -1
    }

    fun isNeedToShowWelcome(): Boolean =
        database.getMessagesCount() == 0 && fragment?.getDisplayedMessagesCount() == 0 && isChatReady() && !isDownloadingMessages

    val stateOfConsult: Int
        get() = if (consultWriter.isSearchingConsult) {
            CONSULT_STATE_SEARCHING
        } else if (consultWriter.isConsultConnected) {
            CONSULT_STATE_FOUND
        } else {
            CONSULT_STATE_DEFAULT
        }

    val isConsultFound: Boolean
        get() = isChatWorking() && consultWriter.isConsultConnected

    val currentConsultInfo: ConsultInfo?
        get() = consultWriter.currentConsultInfo

    internal fun bindFragment(chatFragment: ChatFragment?) {
        info("bindFragment: $chatFragment")
        val activity = chatFragment?.activity ?: return

        if (demoModeProvider.isDemoModeEnabled()) {
            database.cleanDatabase()
        }

        fragment = chatFragment

        chatFragment.showProgressBar()
        if (consultWriter.isSearchingConsult) {
            chatFragment.setStateSearchingConsult()
        }
        subscribe(
            Single.fromCallable {
                val historyLoadingCount = BaseConfig.getInstance().historyLoadingCount
                val unsentUserPhrase = database.getUnsendUserPhrase(historyLoadingCount)
                if (unsentUserPhrase.isNotEmpty()) {
                    messenger.recreateUnsentMessagesWith(unsentUserPhrase)
                }
                setLastAvatars(database.getChatItems(0, historyLoadingCount))
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { chatItems: List<ChatItem> ->
                        chatFragment.addChatItems(chatItems, true)
                        handleQuickReplies(chatItems)
                    }
                ) { obj: Throwable -> obj.message }
        )
        if (consultWriter.isConsultConnected) {
            chatFragment.setStateConsultConnected(consultWriter.currentConsultInfo)
        } else if (consultWriter.isSearchingConsult) {
            chatFragment.setStateSearchingConsult()
        } else {
            chatFragment.setTitleStateDefault()
        }
        progressReceiver = ProgressReceiver(chatFragment)
        val intentFilter = IntentFilter()
        intentFilter.addAction(ProgressReceiver.PROGRESS_BROADCAST)
        intentFilter.addAction(ProgressReceiver.DOWNLOADED_SUCCESSFULLY_BROADCAST)
        intentFilter.addAction(ProgressReceiver.DOWNLOAD_ERROR_BROADCAST)
        progressReceiver?.let {
            LocalBroadcastManager.getInstance(activity).registerReceiver(it, intentFilter)
        }
    }

    internal fun unbindFragment() {
        fragment?.let {
            val activity: Activity? = it.activity
            if (activity != null && progressReceiver != null) {
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(progressReceiver!!)
            }
        }
        fragment = null
    }

    private fun checkEmptyStateVisibility() {
        if (!ThreadsLibBase.getInstance().isUserInitialized) {
            fragment?.showEmptyState()
        } else {
            fragment?.hideEmptyState()
        }
    }

    private fun loadItemsFromDB() {
        fragment?.let {
            coroutineScope.launch() {
                val itemsDef = async(Dispatchers.IO) { database.getChatItems(0, -1) }
                it.addChatItems(itemsDef.await(), true)
                it.hideProgressBar()
            }
        }
    }

    internal fun setMessagesInCurrentThreadAsReadInDB() {
        subscribe(
            database.setAllConsultMessagesWereReadInThread(threadId)
                .subscribe(
                    { UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount() }
                ) { error: Throwable? -> error("setAllMessagesWereRead() ", error) }
        )
    }

    var threadId: Long?
        get() = preferences.get(PreferencesCoreKeys.THREAD_ID, 0L)
        set(value) = preferences.save(PreferencesCoreKeys.THREAD_ID, value)

    var campaignMessage: CampaignMessage?
        get() = preferences.get(PreferencesCoreKeys.CAMPAIGN_MESSAGE)
        set(value) = preferences.save(PreferencesCoreKeys.CAMPAIGN_MESSAGE, value)

    var fileDescriptionDraft: FileDescription?
        get() = preferences.get(PreferencesCoreKeys.FILE_DESCRIPTION_DRAFT)
        set(fileDescription) {
            preferences.save(PreferencesCoreKeys.FILE_DESCRIPTION_DRAFT, fileDescription)
        }

    internal fun clearUnreadPushCount() {
        preferences.save(PreferencesCoreKeys.UNREAD_PUSH_COUNT, 0)
    }

    private fun subscribe(event: Disposable): Boolean {
        if (compositeDisposable == null || compositeDisposable?.isDisposed == true) {
            compositeDisposable = CompositeDisposable()
        }
        return compositeDisposable?.add(event) == true
    }

    internal fun setAllMessagesWereRead() {
        removePushNotification()
        subscribe(
            database.setAllConsultMessagesWereRead()
                .subscribe(
                    { UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount() }
                ) { error: Throwable? -> error("setAllMessagesWereRead() ", error) }
        )
        fragment?.setAllMessagesWereRead()
    }

    internal fun isMessageSent(correlationId: String?): Boolean {
        return database
            .getNotDeliveredChatItems()
            .firstOrNull { it.id == correlationId } == null
    }

    internal fun isChatWorking(): Boolean = currentScheduleInfo == null || currentScheduleInfo?.isChatWorking == true

    internal fun isSendDuringInactive() = currentScheduleInfo?.sendDuringInactive == true

    @Throws(Exception::class)
    private fun onClientIdChanged() {
        info(ThreadsApi.REST_TAG, "Client id changed. Loading history.")
        cleanAll(true)
        fragment?.removeSearching()
        if (isChatReady()) loadHistory()
    }

    internal fun loadHistoryAfterWithLastMessageCheck(
        applyUiChanges: Boolean = true,
        forceLoad: Boolean = false,
        fromQuickAnswerController: Boolean = false,
        callback: HistoryLoader.HistoryLoadingCallback? = null
    ) {
        coroutineScope.launch {
            val lastTimeStampDef = async(Dispatchers.IO) { getItemTimestampForHistoryLoad() }
            lastTimeStampDef.await()?.let {
                loadHistory(
                    it,
                    isAfterAnchor = true,
                    loadToTheEnd = true,
                    forceLoad = forceLoad,
                    applyUiChanges = applyUiChanges,
                    fromQuickAnswerController = fromQuickAnswerController,
                    callback = callback
                )
            } ?: loadHistory(
                applyUiChanges = applyUiChanges,
                callback = callback,
                fromQuickAnswerController = fromQuickAnswerController
            )
        }
    }

    @Synchronized
    internal fun loadHistory(
        anchorTimestamp: Long? = null,
        isAfterAnchor: Boolean? = null,
        loadToTheEnd: Boolean = false,
        forceLoad: Boolean = false,
        applyUiChanges: Boolean = true,
        fromQuickAnswerController: Boolean = false,
        untilCondition: ((List<ChatItem>) -> Boolean)? = null,
        callback: HistoryLoader.HistoryLoadingCallback? = null
    ) {
        if (!forceLoad && isAllMessagesDownloaded) {
            coroutineScope.launch {
                fragment?.hideProgressBar()
                fragment?.hideErrorView()
            }
            return
        }
        if (!chatState.isChatReady() && !fromQuickAnswerController) return
        synchronized(this) {
            if (!isDownloadingMessages) {
                isDownloadingMessages = true
                subscribe(
                    Single.fromCallable {
                        val count = BaseConfig.getInstance().historyLoadingCount
                        val response = if (isAfterAnchor == true && anchorTimestamp != null) {
                            historyLoader.getHistorySync(anchorTimestamp, count, true)
                        } else {
                            historyLoader.getHistorySync(
                                count,
                                isAfterAnchor == null
                            )
                        }
                        val serverItems = HistoryParser.getChatItems(response)
                        if (serverItems.isEmpty()) {
                            isAllMessagesDownloaded = true
                        }
                        parseHistoryItemsForSentStatus(serverItems)
                        parseHistoryItemsForAttachmentStatus(serverItems)
                        messenger.saveMessages(serverItems)
                        clearUnreadPush()
                        processSystemMessages(serverItems)
                        if (fragment != null && isActive && !fragment!!.isStartSecondLevelScreen()) {
                            val uuidList: List<String?> = database.getUnreadMessagesUuid()
                            if (uuidList.isNotEmpty()) {
                                BaseConfig.getInstance().transport.markMessagesAsRead(uuidList)
                            }
                        }
                        Pair(response?.getConsultInfo(), serverItems)
                    }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { pair: Pair<ConsultInfo?, List<ChatItem>> ->
                                chatState.changeState(ChatStateEnum.HISTORY_LOADED)
                                isDownloadingMessages = false
                                val (consultInfo, serverItems) = pair
                                val moreWithUntilCondition = untilCondition?.invoke(serverItems) ?: false
                                val isShouldBeLoadedMore = (loadToTheEnd || moreWithUntilCondition) &&
                                    serverItems.size == BaseConfig.getInstance().historyLoadingCount
                                if (applyUiChanges) {
                                    val items = setLastAvatars(serverItems)
                                    if (fragment != null) {
                                        fragment?.addChatItems(items)
                                        if (isAfterAnchor == null) { // from beginning
                                            handleQuickReplies(items)
                                        }
                                        handleInputAvailability(items)
                                        if (consultInfo != null) {
                                            fragment?.setStateConsultConnected(consultInfo)
                                        }

                                        if (!isShouldBeLoadedMore) {
                                            fragment?.hideProgressBar()
                                            fragment?.showBottomBar()
                                            fragment?.hideErrorView()
                                        }
                                    }
                                    callback?.onLoaded(items)
                                }
                                if (!applyUiChanges) callback?.onLoaded(serverItems)
                                if (isShouldBeLoadedMore) {
                                    loadHistory(anchorTimestamp, isAfterAnchor, true, applyUiChanges = applyUiChanges)
                                }
                            }
                        ) { e: Throwable? ->
                            isDownloadingMessages = false
                            if (fragment != null) {
                                fragment?.hideProgressBar()
                                fragment?.showWelcomeScreenIfNeed()
                                fragment?.showBottomBar()
                            }
                            error(e)
                        }
                )
            } else {
                info(ThreadsApi.REST_TAG, "Loading history cancelled. isDownloadingMessages = true")
            }
        }
    }

    private fun loadSettings() {
        subscribe(
            Single.fromCallable {
                get().settings()?.execute()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response: Response<SettingsResponse?>? ->
                        val responseBody = response?.body()
                        if (responseBody != null) {
                            val clientNotificationType = responseBody.clientNotificationDisplayType
                            if (!clientNotificationType.isNullOrEmpty()) {
                                val type = ClientNotificationDisplayType.fromString(
                                    clientNotificationType
                                )
                                preferences.save(
                                    PreferencesUiKeys.CLIENT_NOTIFICATION_DISPLAY_TYPE,
                                    type.name
                                )
                                chatUpdateProcessor.postClientNotificationDisplayType(type)
                            }
                        }
                    }
                ) { e: Throwable ->
                    val message = if (e.localizedMessage.isNullOrBlank()) e.message else e.localizedMessage
                    info("error on getting settings: $message")
                    chatUpdateProcessor.postError(TransportException(message))
                }
        )
    }

    internal fun downloadMessagesTillEnd(): Single<List<ChatItem>> {
        return messenger.downloadMessagesTillEnd()
    }

    internal fun forceResend(userPhrase: UserPhrase?) {
        if (isScheduleActive(currentScheduleInfo) && userPhrase != null) {
            messenger.forceResend(userPhrase)
        }
    }

    internal fun removeUserPhraseFromDatabaseAsync(userPhrase: UserPhrase) {
        coroutineScope.launch(Dispatchers.IO) {
            database.removeItem(userPhrase.id, userPhrase.backendMessageId)
        }
    }

    private fun addLocalUserMessages(serverItems: List<ChatItem>): List<ChatItem> {
        val items = serverItems.toMutableList()
        val localMessagesToDelete = ArrayList<UserPhrase>()
        for (localUserMessage in localUserMessages) {
            for (serverItem in items) {
                if (serverItem.isTheSameItem(localUserMessage)) {
                    localMessagesToDelete.add(localUserMessage)
                    break
                }
            }
        }
        for (localMessageToDelete in localMessagesToDelete) {
            localUserMessages.remove(localMessageToDelete)
        }
        items.addAll(localUserMessages)
        return items
    }

    private fun setLastAvatars(list: List<ChatItem>): List<ChatItem> {
        for (ci in list) {
            if (ci is ConsultPhrase) {
                val consultInfo = if (ci.consultId != null) {
                    database.getConsultInfo(ci.consultId!!)
                } else {
                    null
                }
                if (consultInfo != null) {
                    ci.avatarPath = consultInfo.photoUrl
                }
            }
        }
        return list
    }

    private fun subscribeToChatEvents() {
        subscribeToTyping()
        subscribeToOutgoingMessageStatusChanged()
        subscribeToIncomingMessageRead()
        subscribeToNewMessage()
        subscribeToUpdateAttachments()
        subscribeToMessageSendSuccess()
        subscribeToCampaignMessageReplySuccess()
        subscribeToMessageSendError()
        subscribeToSurveySendSuccess()
        subscribeToRemoveChatItem()
        subscribeToDeviceAddressChanged()
        subscribeToQuickReplies()
        subscribeToAttachAudioFiles()
        subscribeToClientNotificationDisplayTypeProcessor()
        subscribeSpeechMessageUpdated()
        subscribeForResendMessage()
        subscribeOnClientIdChange()
        subscribeOnMessageError()
        subscribeOnFileUploadResult()
        subscribeToTransportException()
        subscribeOnChatState()
    }

    private fun subscribeToTransportException() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.errorProcessor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (chatState.getCurrentState() < ChatStateEnum.INIT_USER_SENT) {
                        fragment?.showErrorView(it.message)
                    }
                    error("subscribeToTransportException ", it)
                }
        )
    }

    private fun subscribeToCampaignMessageReplySuccess() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.campaignMessageReplySuccessProcessor)
                .observeOn(Schedulers.io())
                .delay(1000, TimeUnit.MILLISECONDS)
                .subscribe(
                    {
                        if (isChatReady()) {
                            loadHistory()
                        }
                    }
                ) { onError: Throwable? ->
                    error("subscribeToCampaignMessageReplySuccess ", onError)
                }
        )
    }

    private fun subscribeToTyping() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.typingProcessor)
                .map {
                    ConsultTyping(
                        consultWriter.currentConsultId,
                        System.currentTimeMillis(),
                        consultWriter.currentPhotoUrl
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { chatItem: ConsultTyping -> addMessage(chatItem) }
                ) { error: Throwable? -> error("subscribeToTyping ", error) }
        )
    }

    private fun subscribeToOutgoingMessageStatusChanged() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.outgoingMessageStatusChangedProcessor)
                .concatMap { Flowable.fromIterable(it) }
                .observeOn(Schedulers.io())
                .doOnNext { status: Status ->
                    database.setOrUpdateMessageId(status.correlationId, status.messageId)
                    val message = if (status.messageId != null) {
                        database.setStateOfUserPhraseByBackendMessageId(status.messageId, status.status)
                        database.getChatItemByBackendMessageId(status.messageId)
                    } else {
                        database.setStateOfUserPhraseByCorrelationId(status.correlationId, status.status)
                        database.getChatItemByCorrelationId(status.correlationId)
                    }
                    if (message is UserPhrase) {
                        if (status.status > MessageStatus.FAILED) {
                            messenger.removeUserMessageFromQueue(message)
                        } else if (status.status == MessageStatus.FAILED) {
                            messenger.addMsgToResendQueue(message)
                        }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { status: Status ->
                        sentStatus(status)
                    }
                ) { error: Throwable? ->
                    error("subscribeToOutgoingMessageRead ", error)
                }
        )
    }

    private fun sentStatus(status: Status) {
        coroutineScope.launch {
            val result = coroutineScope.async(Dispatchers.IO) {
                database.getChatItemByBackendMessageId(status.messageId)
            }
            val chatItem = result.await()
            val messageStatus = getMessageStatus(chatItem, status)
            fragment?.setMessageState(messageStatus.correlationId, messageStatus.messageId, messageStatus.status)
        }
    }

    private fun getMessageStatus(chatItem: ChatItem?, receivedStatus: Status): Status {
        val item = (chatItem as? UserPhrase)
        return if (item != null) {
            if (item.sentState.ordinal > receivedStatus.status.ordinal) {
                Status(
                    item.backendMessageId ?: receivedStatus.correlationId,
                    item.backendMessageId ?: receivedStatus.messageId,
                    item.sentState
                )
            } else {
                receivedStatus
            }
        } else {
            receivedStatus
        }
    }

    private fun subscribeToIncomingMessageRead() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.incomingMessageReadProcessor)
                .observeOn(Schedulers.io())
                .onBackpressureBuffer()
                .subscribe(
                    { uuid: String? ->
                        database.setMessageWasRead(uuid)
                        UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount()
                    }
                ) { error: Throwable? -> error("subscribeToIncomingMessageRead ", error) }
        )
    }

    private fun subscribeSpeechMessageUpdated() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.speechMessageUpdateProcessor)
                .debounce(UPDATE_SPEECH_STATUS_DEBOUNCE, TimeUnit.MILLISECONDS)
                .map { speechMessageUpdate: SpeechMessageUpdate ->
                    database.saveSpeechMessageUpdate(speechMessageUpdate)
                    val itemFromDb = database.getChatItemByCorrelationId(speechMessageUpdate.uuid)
                    if (itemFromDb == null) {
                        return@map speechMessageUpdate
                    } else {
                        return@map itemFromDb
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { chatItem: ChatItem? ->
                        fragment?.addChatItem(chatItem)
                    }
                ) { error: Throwable -> error("subscribeSpeechMessageUpdated $error") }
        )
    }

    private fun subscribeToUpdateAttachments() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.updateAttachmentsProcessor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { attachments: List<Attachment> ->
                        for (attachment in attachments) {
                            for (item in database.getChatItems(0, PER_PAGE_COUNT)) {
                                if (item is ChatPhrase) {
                                    if (item.fileDescription != null) {
                                        val attachmentName = attachment.name ?: ""
                                        val incomingNameEquals =
                                            (item.fileDescription?.incomingName == attachmentName)
                                        val isUrlHashFileName =
                                            (
                                                item.fileDescription?.fileUri.toString()
                                                    .contains(attachmentName)
                                                )
                                        val isOriginalUrlValid = attachment.originalUrl?.let {
                                            val downloadPathContainsUrl = item.fileDescription?.downloadPath?.contains(it) ?: false
                                            val fileUriContainsUrl = item.fileDescription?.fileUri?.toString()?.contains(it) ?: false

                                            downloadPathContainsUrl || fileUriContainsUrl
                                        } ?: false
                                        if ((incomingNameEquals || isUrlHashFileName || isOriginalUrlValid) && fragment?.isAdded == true) {
                                            item.fileDescription?.state = attachment.state
                                            item.fileDescription?.errorCode =
                                                attachment.getErrorCodeState()
                                            item.fileDescription?.downloadPath = attachment.result
                                            fragment?.updateProgress(item.fileDescription)
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { e: Throwable? -> error(e) }
        )
    }

    private fun subscribeToNewMessage() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.newMessageProcessor)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { chatItem: ChatItem ->
                    when (chatItem) {
                        is MessageRead -> {
                            val readMessagesIds = chatItem.messageId
                            for (readId in readMessagesIds) {
                                val databaseItem = database.getChatItemByCorrelationId(readId)
                                (databaseItem as UserPhrase?)?.let { userPhrase ->
                                    userPhrase.id?.let {
                                        info("Sending read status to update processor. CorrelationId: $it")
                                        if (!chatUpdateProcessor.outgoingMessageStatusChangedProcessor.hasSubscribers()) {
                                            subscribeToOutgoingMessageStatusChanged()
                                        }
                                        chatUpdateProcessor.postOutgoingMessageStatusChanged(listOf(Status(it, status = MessageStatus.READ)))
                                    }
                                }
                            }
                            return@doOnNext
                        }

                        is ScheduleInfo -> {
                            currentScheduleInfo = chatItem
                            currentScheduleInfo?.calculateServerTimeDiff()
                            refreshUserInputState()
                            if (!isChatWorking()) {
                                consultWriter.isSearchingConsult = false
                                fragment?.removeSearching()
                                fragment?.setTitleStateDefault()
                                hideQuickReplies()
                            } else {
                                if (fragment?.quickReplyItem != null) {
                                    fragment?.showQuickReplies(fragment?.quickReplyItem)
                                }
                            }
                            fragment?.addChatItem(currentScheduleInfo)
                        }

                        is ConsultConnectionMessage -> {
                            processConsultConnectionMessage(chatItem)
                        }

                        is SearchingConsult -> {
                            fragment?.setStateSearchingConsult()
                            consultWriter.isSearchingConsult = true
                            return@doOnNext
                        }

                        is SimpleSystemMessage -> {
                            processSimpleSystemMessage(chatItem)
                        }

                        is ConsultPhrase -> {
                            refreshUserInputState(chatItem.isBlockInput)
                        }
                    }
                    addMessage(chatItem)
                }
                .filter { chatItem: ChatItem? -> chatItem is Hidable }
                .map { chatItem: ChatItem -> chatItem as Hidable }
                .delay { item: Hidable -> Flowable.timer(item.hideAfter ?: 0, TimeUnit.SECONDS) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hidable: Hidable? ->
                        if (hidable is Survey) {
                            removeActiveSurvey()
                        }
                    }
                ) { obj: Throwable -> obj.message }
        )
    }

    private fun subscribeToMessageSendSuccess() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.messageSendSuccessProcessor)
                .observeOn(Schedulers.io())
                .flatMapMaybe { chatItemSent: ChatItemProviderData ->
                    val chatItem = database.getChatItemByCorrelationId(chatItemSent.uuid)
                    if (chatItem is UserPhrase) {
                        if (chatItemSent.sentAt > 0) {
                            chatItem.timeStamp = chatItemSent.sentAt
                        }
                        chatItem.sentState = MessageStatus.SENT
                        database.putChatItem(chatItem)
                    }
                    if (chatItem == null) {
                        error("chatItem not found")
                    }
                    Maybe.fromCallable { chatItem }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { chatItem: ChatItem? ->
                        if (chatItem is UserPhrase) {
                            fragment?.addChatItem(chatItem)
                            messenger.proceedSendingQueue(chatItem)
                        }
                    }
                ) { error: Throwable? -> error("subscribeToMessageSendSuccess ", error) }
        )
    }

    private fun subscribeToMessageSendError() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.messageSendErrorProcessor)
                .observeOn(Schedulers.io())
                .flatMapMaybe { (_, phraseUuid): ChatItemSendErrorModel ->
                    val chatItem = database.getChatItemByCorrelationId(phraseUuid)
                    if (chatItem is UserPhrase) {
                        chatItem.sentState = MessageStatus.FAILED
                        database.putChatItem(chatItem)
                    }
                    if (chatItem == null) {
                        error("chatItem not found")
                    }
                    Maybe.fromCallable { chatItem }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { chatItem: ChatItem? ->
                        if (chatItem is UserPhrase) {
                            fragment?.setMessageState(chatItem.id, null, chatItem.sentState)
                            messenger.addMsgToResendQueue(chatItem)
                            messenger.proceedSendingQueue(chatItem)
                        }
                    }
                ) { error: Throwable? -> error("subscribeToMessageSendError ", error) }
        )
    }

    private fun subscribeToSurveyCompletion() {
        subscribe(
            Flowable.fromPublisher(surveyCompletionProcessor)
                .throttleLast(Config.getInstance().surveyCompletionDelay.toLong(), TimeUnit.MILLISECONDS)
                .firstElement()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ survey ->
                    BaseConfig.getInstance().transport.sendRatingDone(survey)
                }) { error ->
                    error("subscribeToSurveyCompletion: $error.message")
                }
        )
    }

    private fun subscribeToSurveySendSuccess() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.surveySendSuccessProcessor)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { survey: Survey ->
                        surveyCompletionInProgress = false
                        setSurveyStateSent(survey)
                        resetActiveSurvey()
                    }
                ) { error: Throwable? -> error("subscribeToSurveySendSuccess ", error) }
        )
    }

    private fun subscribeToRemoveChatItem() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.removeChatItemProcessor)
                .observeOn(AndroidSchedulers.mainThread())
                .filter { chatItemType: ChatItemType -> chatItemType == ChatItemType.REQUEST_CLOSE_THREAD }
                .subscribe({ removeResolveRequest() }) { error: Throwable? ->
                    error("subscribeToRemoveChatItem ", error)
                }
        )
    }

    private fun subscribeToDeviceAddressChanged() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.deviceAddressChangedProcessor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDeviceAddressChanged() }) { error: Throwable? ->
                    error("subscribeToDeviceAddressChanged ", error)
                }
        )
    }

    private fun subscribeToQuickReplies() {
        subscribe(
            chatUpdateProcessor.quickRepliesProcessor
                .subscribe(
                    { quickReplies: QuickReplyItem? ->
                        hasQuickReplies = quickReplies != null && quickReplies.items.isNotEmpty()
                        if (hasQuickReplies && isChatWorking()) {
                            fragment?.showQuickReplies(quickReplies)
                        } else {
                            fragment?.hideQuickReplies()
                        }
                        refreshUserInputState()
                    }
                ) { error: Throwable? -> error("subscribeToQuickReplies ", error) }
        )
    }

    private fun subscribeToAttachAudioFiles() {
        subscribe(
            chatUpdateProcessor.attachAudioFilesProcessor
                .subscribe({
                    refreshUserInputState()
                }) { error: Throwable? -> error("subscribeToAttachAudioFiles ", error) }
        )
    }

    private fun subscribeToClientNotificationDisplayTypeProcessor() {
        subscribe(
            chatUpdateProcessor.clientNotificationDisplayTypeProcessor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { type: ClientNotificationDisplayType? ->
                        fragment?.setClientNotificationDisplayType(type)
                    }
                ) { obj: Throwable -> obj.message }
        )
    }

    private fun subscribeForResendMessage() {
        subscribe(
            messenger.resendStream
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ messageId: String? ->
                    fragment?.setMessageState(messageId, null, MessageStatus.SENDING)
                }) { obj: Throwable -> obj.message }
        )
    }

    private fun removeResolveRequest() {
        subscribe(
            database.setOldRequestResolveThreadDisplayMessageToFalse()
                .subscribe({}) { obj: Throwable -> obj.message }
        )
        fragment?.removeResolveRequest()
    }

    private fun removeActiveSurvey() {
        subscribe(
            database.setNotSentSurveyDisplayMessageToFalse()
                .subscribe({}) { obj: Throwable -> obj.message }
        )
        fragment?.let { chatFragment ->
            activeSurvey?.sendingId?.let { chatFragment.removeSurvey(it) }
        }
    }

    private fun resetActiveSurvey() {
        activeSurvey = null
    }

    /**
     *  Вызывается когда получено новое сообщение из канала (TG/PUSH)
     */
    private fun addMessage(chatItem: ChatItem) {
        if (chatItem is Survey) {
            chatItem.questions?.forEach { it.generateCorrelationId() }
        }
        database.putChatItem(chatItem)
        UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount()
        if (fragment != null) {
            val ci = setLastAvatars(listOf(chatItem))[0]
            if (ci !is ConsultConnectionMessage || ci.display) {
                fragment?.addChatItem(ci)
            } else {
                fragment?.notifyConsultAvatarChanged(
                    (ci as ConsultChatPhrase).avatarPath,
                    (ci as ConsultChatPhrase).consultId
                )
            }
        }
        if (chatItem is ConsultPhrase && isActive) {
            handleQuickReplies(listOf<ChatItem>(chatItem))
        } else if (chatItem is SimpleSystemMessage && isActive) {
            hideQuickReplies()
        } else if (chatItem is Survey && isActive) {
            BaseConfig.getInstance().transport.markMessagesAsRead(listOf(chatItem.uuid))
        } else if (chatItem is RequestResolveThread && isActive) {
            BaseConfig.getInstance().transport.markMessagesAsRead(listOf(chatItem.uuid))
        }
        subscribe(
            Observable.timer(1500, TimeUnit.MILLISECONDS)
                .filter { isActive }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        removePushNotification()
                        UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount()
                    }
                ) { error: Throwable? -> error("addMessage ", error) }
        )
        // Если пришло сообщение от оператора,
        // или новое расписание в котором сейчас чат работает
        // - нужно удалить расписание из чата
        if (chatItem is ConsultPhrase ||
            chatItem is ConsultConnectionMessage && ChatItemType.OPERATOR_JOINED.name == chatItem.getType() ||
            chatItem is ScheduleInfo && chatItem.isChatWorking
        ) {
            if (fragment?.isAdded == true) {
                fragment?.removeSchedule(false)
            }
        }
    }

    internal fun cleanAll(keepClientId: Boolean = false) {
        info("cleanAll!")
        isAllMessagesDownloaded = false
        messenger.clearSendQueue()
        fragment?.cleanChat()
        threadId = -1L
        consultWriter.setCurrentConsultLeft()
        consultWriter.isSearchingConsult = false
        removePushNotification()
        clientUseCase.cleanUserInfoFromRam()
        clearPreferences(keepClientId)
        UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount()
        localUserMessages.clear()
        database.cleanDatabase()
    }

    @SuppressLint("ApplySharedPref")
    private fun clearPreferences(keepClientId: Boolean = false) {
        val fcmToken = preferences.get<String>(PreferencesCoreKeys.FCM_TOKEN)
        val hcmToken = preferences.get<String>(PreferencesCoreKeys.HCM_TOKEN)
        val currentUiThemeValue = preferences.get(PreferencesCoreKeys.USER_SELECTED_UI_THEME_KEY, CurrentUiTheme.SYSTEM.value)
        val clientInfo = if (keepClientId) clientUseCase.getUserInfo() else null
        preferences.sharedPreferences.edit().clear().commit()
        preferences.save(PreferencesCoreKeys.FCM_TOKEN, fcmToken)
        preferences.save(PreferencesCoreKeys.HCM_TOKEN, hcmToken)
        preferences.save(PreferencesCoreKeys.USER_SELECTED_UI_THEME_KEY, currentUiThemeValue)

        if (clientInfo != null) {
            clientUseCase.saveUserInfo(clientInfo)
        }
    }

    private fun removePushNotification() {
        removeNotification(appContext)
    }

    private fun setSurveyStateSent(survey: Survey) {
        survey.sentState = MessageStatus.SENT
        survey.isDisplayMessage = true
        fragment?.setSurveySentStatus(survey)
        database.putChatItem(survey)
    }

    private fun convert(message: UpcomingUserMessage): UserPhrase {
        val up = UserPhrase(
            message.text,
            message.quote,
            System.currentTimeMillis(),
            message.fileDescription,
            null
        )
        up.isCopy = message.copied
        up.campaignMessage = message.campaignMessage
        return up
    }

    private fun onDeviceAddressChanged() {
        info(ThreadsApi.REST_TAG, "onDeviceAddressChanged. Loading history.")
        val clientId = clientUseCase.getUserInfo()?.clientId
        if (fragment != null && !clientId.isNullOrBlank()) {
            BaseConfig.getInstance().transport.sendRegisterDevice(false)
            clearUnreadPush()
            if (fragment?.isResumed == true && isChatReady()) {
                loadHistory()
            }
        }
    }

    private fun parseHistoryItemsForSentStatus(items: List<ChatItem>) {
        items
            .mapNotNull { it as? UserPhrase }
            .forEach { userPhrase ->
                if (userPhrase.isRead && userPhrase.errorMock != true) {
                    userPhrase.sentState = MessageStatus.READ
                } else if (userPhrase.errorMock == true) {
                    userPhrase.sentState = MessageStatus.FAILED
                } else if (userPhrase.sentState.ordinal < MessageStatus.DELIVERED.ordinal) {
                    userPhrase.sentState = MessageStatus.DELIVERED
                }
            }
    }

    private fun parseHistoryItemsForAttachmentStatus(items: List<ChatItem>) {
        items
            .filter { it is UserPhrase && it.fileDescription?.fileUri != null }
            .map { it as UserPhrase }
            .forEach { item ->
                attachmentsHistory[item.fileDescription!!.fileUri!!.toString()]?.let { historyItem ->
                    if (historyItem > item.fileDescription!!.state) {
                        item.fileDescription?.state = historyItem
                    } else {
                        attachmentsHistory[item.fileDescription!!.fileUri!!.toString()] = item.fileDescription!!.state
                    }
                }
            }
    }

    private fun clearUnreadPush() {
        UnreadMessagesController.INSTANCE.clearUnreadPush()
    }

    private fun processSystemMessages(chatItems: List<ChatItem>) {
        if (!isChatWorking()) {
            return
        }
        var latestSystemMessage: ChatItem? = null
        for (chatItem in chatItems) {
            if (chatItem is SystemMessage) {
                val threadId = chatItem.threadId
                if (threadId != null && threadId >= threadId) {
                    val type = (chatItem as SystemMessage).getType()
                    if (ChatItemType.OPERATOR_JOINED.toString().equals(type, ignoreCase = true) ||
                        ChatItemType.THREAD_ENQUEUED.toString().equals(type, ignoreCase = true) ||
                        ChatItemType.THREAD_WILL_BE_REASSIGNED.toString()
                            .equals(type, ignoreCase = true) ||
                        ChatItemType.AVERAGE_WAIT_TIME.toString().equals(type, ignoreCase = true) ||
                        ChatItemType.THREAD_CLOSED.toString().equals(type, ignoreCase = true)
                    ) {
                        if (latestSystemMessage == null || latestSystemMessage.timeStamp <= chatItem.timeStamp) {
                            latestSystemMessage = chatItem
                        }
                    }
                }
            }
        }
        if (latestSystemMessage != null) {
            val systemMessage: ChatItem = latestSystemMessage
            Runnable {
                if (systemMessage is ConsultConnectionMessage) {
                    processConsultConnectionMessage(systemMessage)
                } else {
                    processSimpleSystemMessage(systemMessage as SimpleSystemMessage)
                }
            }.runOnUiThread()
        }
    }

    @MainThread
    private fun processConsultConnectionMessage(ccm: ConsultConnectionMessage) {
        if (ccm.getType().equals(ChatItemType.OPERATOR_JOINED.name, ignoreCase = true)) {
            threadId = ccm.threadId
            fragment?.setCurrentThreadId(ccm.threadId)
            consultWriter.isSearchingConsult = false
            consultWriter.setCurrentConsultInfo(ccm)
            fragment?.setStateConsultConnected(
                ConsultInfo(
                    ccm.name,
                    ccm.consultId,
                    ccm.status,
                    ccm.orgUnit,
                    ccm.avatarPath,
                    ccm.role
                )
            )
        }
    }

    @MainThread
    private fun processSimpleSystemMessage(systemMessage: SimpleSystemMessage) {
        val type = systemMessage.getType()
        if (ChatItemType.THREAD_CLOSED.name.equals(type, ignoreCase = true)) {
            threadId = -1L
            removeResolveRequest()
            consultWriter.setCurrentConsultLeft()
            if (!consultWriter.isSearchingConsult) {
                fragment?.setTitleStateDefault()
            }
        } else {
            threadId = systemMessage.threadId
            fragment?.setCurrentThreadId(systemMessage.threadId)
            if (ChatItemType.THREAD_ENQUEUED.name.equals(type, ignoreCase = true) ||
                ChatItemType.AVERAGE_WAIT_TIME.name.equals(type, ignoreCase = true)
            ) {
                fragment?.setStateSearchingConsult()
                consultWriter.isSearchingConsult = true
            }
        }
    }

    private fun refreshUserInputState(isInputBlockedFromMessage: Boolean? = null) {
        val inputFieldEnableModel = when (isInputBlockedFromMessage) {
            true -> {
                InputFieldEnableModel(isEnabledInputField = false, isEnabledSendButton = false)
            }
            false -> {
                InputFieldEnableModel(isEnabledInputField = true, isEnabledSendButton = true)
            }
            else -> {
                InputFieldEnableModel(isInputFieldEnabled(), isSendButtonEnabled)
            }
        }
        if (enableModel.toString() != inputFieldEnableModel.toString()) {
            info("UserInputState_change. isInputBlockedFromMessage: $isInputBlockedFromMessage, $inputFieldEnableModel")
        }
        enableModel = inputFieldEnableModel
        fragment?.updateInputEnable(inputFieldEnableModel)
        fragment?.updateChatAvailabilityMessage(inputFieldEnableModel)
    }

    private fun isInputFieldEnabled(): Boolean {
        val fileDescription = try {
            fragment?.fileDescription?.value?.get()
        } catch (exc: NoSuchElementException) {
            null
        }
        return if (fileDescription != null && isVoiceMessage(fileDescription)) {
            false
        } else {
            isSendButtonEnabled
        }
    }

    private val isSendButtonEnabled: Boolean
        get() = if (hasQuickReplies && !inputEnabledDuringQuickReplies) {
            false
        } else {
            enableInputBySchedule()
        }

    private fun enableInputBySchedule(): Boolean {
        return if (currentScheduleInfo == null) {
            true
        } else {
            isScheduleActive(currentScheduleInfo)
        }
    }

    private fun isScheduleActive(scheduleInfo: ScheduleInfo?): Boolean {
        return scheduleInfo?.isChatWorking == true || scheduleInfo?.sendDuringInactive == true
    }

    private fun handleQuickReplies(chatItems: List<ChatItem>) {
        if (chatItems.isEmpty()) {
            return
        }
        val quickReplyMessageCandidate = getQuickReplyMessageCandidate(chatItems)
        if (quickReplyMessageCandidate != null) {
            inputEnabledDuringQuickReplies = if (quickReplyMessageCandidate.isBlockInput != null) {
                java.lang.Boolean.FALSE == quickReplyMessageCandidate.isBlockInput
            } else {
                chatStyle.inputEnabledDuringQuickReplies
            }

            if (quickReplyMessageCandidate.quickReplies != null) {
                chatUpdateProcessor.postQuickRepliesChanged(
                    QuickReplyItem(
                        quickReplyMessageCandidate.quickReplies,
                        quickReplyMessageCandidate.timeStamp + 1
                    )
                )
            }
        } else {
            hideQuickReplies()
        }
    }

    private fun handleInputAvailability(chatItems: List<ChatItem>) {
        (chatItems.lastOrNull { it is ConsultPhrase } as? ConsultPhrase)?.let { lastOperatorPhrase ->
            if (lastOperatorPhrase.isBlockInput != null) {
                refreshUserInputState(lastOperatorPhrase.isBlockInput)
            }
        }
    }

    internal fun hideQuickReplies() {
        chatUpdateProcessor.postQuickRepliesChanged(QuickReplyItem(java.util.ArrayList(), 0))
    }

    private fun getQuickReplyMessageCandidate(chatItems: List<ChatItem>?): ConsultPhrase? {
        if (!chatItems.isNullOrEmpty()) {
            val listIterator = chatItems.listIterator(chatItems.size)
            while (listIterator.hasPrevious()) {
                val chatItem = listIterator.previous()
                // При некоторых ситуациях (пока неизвестно каких) последнее сообщение в истории ConsultConnectionMessage, который не отображается, его нужно игнорировать
                if (chatItem is ConsultConnectionMessage) {
                    if (!chatItem.display) {
                        continue
                    }
                } else if (chatItem is ConsultPhrase) {
                    return chatItem
                }
                break
            }
        }
        return null
    }

    private fun subscribeOnClientIdChange() {
        val userInfo = clientUseCase.getUserInfo()
        val newClientId = clientUseCase.getTagNewClientId()
        val oldClientId = userInfo?.clientId
        if (!newClientId.isNullOrEmpty() && newClientId != oldClientId) {
            instance?.onClientIdChanged()
        }
    }

    private fun subscribeOnMessageError() {
        subscribe(
            messageErrorProcessor
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val lastVisibleItem = fragment?.lastVisibleItemPosition
                    val items = fragment?.elements

                    if (lastVisibleItem != null && items != null && items.last().timeStamp == items[lastVisibleItem].timeStamp) {
                        fragment?.scrollToElementByIndex(lastVisibleItem)
                    }
                }
        )
    }

    private fun subscribeOnFileUploadResult() {
        subscribe(
            chatUpdateProcessor.uploadResultProcessor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { fileDescription ->
                    fileDescription.fileUri?.let { attachmentsHistory[it.toString()] = fileDescription.state }
                    fragment?.updateProgress(fileDescription)
                }
        )
    }

    private fun subscribeOnChatState() {
        coroutineScope.launch(Dispatchers.IO) {
            chatState.getStateFlow().collect { stateEvent ->
                if (demoModeProvider.isDemoModeEnabled() && stateEvent.state < ChatStateEnum.ATTACHMENT_SETTINGS_LOADED) {
                    chatState.changeState(ChatStateEnum.ATTACHMENT_SETTINGS_LOADED)
                } else {
                    info("ChatState name: ${stateEvent.state.name}, isTimeout: ${stateEvent.isTimeout}")
                    if (!stateEvent.isTimeout && stateEvent.state < ChatStateEnum.HISTORY_LOADED) {
                        withContext(Dispatchers.Main) { fragment?.showProgressBar() }
                    }
                    if (stateEvent.isTimeout && chatState.getCurrentState() < ChatStateEnum.ATTACHMENT_SETTINGS_LOADED) {
                        val timeoutMessage = if (stateEvent.state >= ChatStateEnum.INIT_USER_SENT && fragment != null) {
                            fragment?.getString(R.string.ecc_attachments_not_loaded)
                        } else {
                            "${fragment?.getString(R.string.ecc_timeout_message) ?: "Превышен интервал ожидания для запроса"} (${chatState.getCurrentState()})"
                        }
                        withContext(Dispatchers.Main) { fragment?.showErrorView(timeoutMessage) }
                    } else if (stateEvent.state == ChatStateEnum.DEVICE_REGISTERED) {
                        if (preferences.get(PreferencesCoreKeys.DEVICE_ADDRESS, "").isNullOrBlank()) {
                            BaseConfig.getInstance().transport.sendRegisterDevice(false)
                        } else {
                            BaseConfig.getInstance().transport.sendInitMessages()
                        }
                    } else if (stateEvent.state == ChatStateEnum.ATTACHMENT_SETTINGS_LOADED) {
                        loadItemsFromDB()
                        if (fragment?.isResumed == true) loadHistoryAfterWithLastMessageCheck()
                        loadSettings()
                    } else if (isChatReady()) {
                        messenger.resendMessages()
                    }
                }
            }
        }
    }

    private fun getItemTimestampForHistoryLoad(): Long? {
        val timeStamp = getUncompletedUserPhraseTimestamp()
        if (timeStamp != null) {
            return timeStamp
        }
        return getLastDbItemTimestamp()
    }

    private fun getUncompletedUserPhraseTimestamp(): Long? {
        val items = database.getChatItems(0, BaseConfig.getInstance().historyLoadingCount).toMutableList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(items, Comparator.comparingLong(ChatItem::timeStamp))
        } else {
            items.sortWith { lhs: ChatItem, rhs: ChatItem ->
                lhs.timeStamp.compareTo(rhs.timeStamp)
            }
        }
        for (i in items.size - 1 downTo 0) {
            if (items[i] is UserPhrase) {
                val userPhrase = items[i] as UserPhrase
                if (userPhrase.sentState != MessageStatus.READ) {
                    return userPhrase.timeStamp - 1
                }
            }
        }
        return null
    }

    private fun getLastDbItemTimestamp(): Long? {
        return try {
            database
                .getChatItems(0, 1)
                .map { it.timeStamp }
                .last()
        } catch (exc: Exception) {
            null
        }
    }

    internal fun setFormattedDurations(
        list: List<ChatItem?>?,
        mediaMetadataRetriever: MediaMetadataRetriever,
        callback: () -> Unit
    ) {
        if (list == null) {
            callback()
            return
        }

        coroutineScope.launch(Dispatchers.IO) {
            list
                .filterNotNull()
                .forEach { chatItem ->
                    val fileDescription = (chatItem as? ConsultPhrase)?.fileDescription ?: (chatItem as? UserPhrase)?.fileDescription
                    fileDescription?.voiceFormattedDuration = getFormattedDuration(fileDescription, mediaMetadataRetriever)
                }
            withMainContext { callback() }
        }
    }

    private fun getFormattedDuration(fileDescription: FileDescription?, mediaMetadataRetriever: MediaMetadataRetriever): String {
        var duration = 0L
        if (fileDescription != null && isVoiceMessage(fileDescription) && fileDescription.fileUri != null) {
            duration = mediaMetadataRetriever.getDuration(fileDescription.fileUri!!)
        }
        return duration.formatAsDuration()
    }

    companion object {
        // Состояния консультанта
        const val CONSULT_STATE_FOUND = 1
        const val CONSULT_STATE_SEARCHING = 2
        const val CONSULT_STATE_DEFAULT = 3
        private const val PER_PAGE_COUNT = 100
        private const val UPDATE_SPEECH_STATUS_DEBOUNCE = 400L
        private var instance: ChatController? = null

        @JvmStatic
        @Synchronized
        fun getInstance(): ChatController {
            val clientUseCase = ClientUseCase(Preferences(ContextHolder.context))
            if (instance == null) {
                instance = ChatController()
            }
            clientUseCase.initClientId()
            return instance ?: ChatController()
        }
    }
}
