package im.threads.ui.controllers

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.annotation.MainThread
import androidx.core.util.Consumer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.JsonObject
import im.threads.R
import im.threads.business.UserInfoBuilder
import im.threads.business.broadcastReceivers.ProgressReceiver
import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.controllers.UnreadMessagesController
import im.threads.business.core.ContextHolder
import im.threads.business.core.ThreadsLibBase
import im.threads.business.formatters.ChatItemType
import im.threads.business.logger.LoggerEdna.debug
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
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.models.SettingsResponse
import im.threads.business.rest.queries.BackendApi.Companion.get
import im.threads.business.rest.queries.ThreadsApi
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.transport.ChatItemProviderData
import im.threads.business.transport.HistoryLoader
import im.threads.business.transport.HistoryParser
import im.threads.business.transport.TransportException
import im.threads.business.transport.models.Attachment
import im.threads.business.transport.threadsGate.responses.Status
import im.threads.business.utils.ChatMessageSeeker
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.ConsultWriter
import im.threads.business.utils.FileUtils.getMimeType
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.FileUtils.isVoiceMessage
import im.threads.business.utils.messenger.Messenger
import im.threads.business.utils.messenger.MessengerImpl
import im.threads.business.workers.FileDownloadWorker.Companion.startDownloadFD
import im.threads.business.workers.FileDownloadWorker.Companion.startDownloadWithNoStop
import im.threads.ui.activities.ConsultActivity.Companion.startActivity
import im.threads.ui.activities.ImagesActivity.Companion.getStartIntent
import im.threads.ui.config.Config
import im.threads.ui.fragments.ChatFragment
import im.threads.ui.preferences.PreferencesUiKeys
import im.threads.ui.utils.preferences.PreferencesMigrationUi
import im.threads.ui.utils.runOnUiThread
import im.threads.ui.workers.NotificationWorker.Companion.removeNotification
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.Date
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
    private val chatStyle = Config.getInstance().getChatStyle()
    private val appContext: Context by inject()
    private val preferences: Preferences by inject()
    private val historyLoader: HistoryLoader by inject()
    private val clientUseCase: ClientUseCase by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // this flag is keeping the visibility state of the request to resolve thread
    private var surveyCompletionInProgress = false

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
    private var isAllMessagesDownloaded = false
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

    init {
        PreferencesMigrationUi(appContext).migrateNamedPreferences(ChatController::class.java.simpleName)
        subscribeToChatEvents()
        messenger.resendMessages()
    }

    fun onViewStart() {
        messenger.onViewStart()
    }

    fun onViewStop() {
        isAllMessagesDownloaded = false
        messenger.onViewStop()
    }

    fun onViewDestroy() {
        messenger.onViewDestroy()
    }

    fun onRatingClick(survey: Survey) {
        if (!surveyCompletionInProgress) {
            surveyCompletionInProgress = true
        }
        BaseConfig.instance.transport.sendRatingDone(survey)
    }

    fun onResolveThreadClick(approveResolve: Boolean) {
        BaseConfig.instance.transport.sendResolveThread(approveResolve)
    }

    fun onUserTyping(input: String?) {
        if (input != null) {
            BaseConfig.instance.transport.sendUserTying(input)
        }
    }

    fun onUserInput(upcomingUserMessage: UpcomingUserMessage) {
        info("onUserInput: $upcomingUserMessage")
        // If user has written a message while the request to resolve the thread is visible
        // we should make invisible the resolve request
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

    fun fancySearch(
        query: String?,
        forward: Boolean,
        consumer: Consumer<Pair<List<ChatItem?>?, ChatItem?>?>
    ) {
        info("Trying to start search")
        subscribe(
            Single.just(isAllMessagesDownloaded)
                .flatMap { isAllMessagesDownloaded: Boolean ->
                    if (!isAllMessagesDownloaded) {
                        info("Not all messages has been downloaded before the search.")

                        if (query?.length == 1) {
                            Runnable {
                                fragment?.showProgressBar()
                                fragment?.showBalloon(appContext.getString(R.string.ecc_history_loading_message))
                            }.runOnUiThread()
                        }
                        return@flatMap messenger.downloadMessagesTillEnd()
                    } else {
                        return@flatMap Single.fromCallable { ArrayList<Any>() }
                    }
                }
                .flatMapCompletable {
                    Completable.fromAction {
                        if (System.currentTimeMillis() > lastFancySearchDate + 3000) {
                            lastItems = database.getChatItems(0, -1)
                            lastFancySearchDate = System.currentTimeMillis()
                        }
                        if (query != null && (query.isEmpty() || query != lastSearchQuery)) {
                            info("Search starting")
                            seeker = ChatMessageSeeker()
                        }
                        lastSearchQuery = query
                        Runnable { fragment?.hideProgressBar() }.runOnUiThread()
                    }
                }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { consumer.accept(seeker.searchMessages(lastItems, !forward, query)) }
                ) { e: Throwable? ->
                    error(e)
                    Runnable { fragment?.hideProgressBar() }.runOnUiThread()
                }
        )
    }

    fun onFileClick(fileDescription: FileDescription) {
        info("onFileClick $fileDescription")
        if (fragment?.isAdded == true) {
            val activity: Activity? = fragment?.activity
            if (activity != null) {
                if (fileDescription.fileUri == null) {
                    startDownloadFD(activity, fileDescription)
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

    fun setActivityIsForeground(isForeground: Boolean) {
        info("setActivityIsForeground")
        isActive = isForeground
        if (isForeground && fragment?.isAdded == true) {
            val cm =
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnectedOrConnecting) {
                val uuidList = database.getUnreadMessagesUuid()
                firstUnreadUuidId = if (uuidList.isNotEmpty()) {
                    BaseConfig.instance.transport.markMessagesAsRead(uuidList)
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

    private fun updateDoubleItems(serverItems: ArrayList<ChatItem>) {
        updateServerItemsBySendingItems(serverItems, getSendingItems())
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

    fun requestItems(currentItemsCount: Int, fromBeginning: Boolean): Observable<List<ChatItem>>? {
        return Observable
            .fromCallable {
                if (instance?.fragment != null && ThreadsLibBase.getInstance().isUserInitialized) {
                    info(ThreadsApi.REST_TAG, "Requesting history items")
                    val count = BaseConfig.instance.historyLoadingCount
                    try {
                        val response = historyLoader.getHistorySync(null, fromBeginning)
                        var serverItems = HistoryParser.getChatItems(response)
                        serverItems = addLocalUserMessages(serverItems)
                        updateDoubleItems(serverItems as ArrayList<ChatItem>)
                        parseHistoryItemsForSentStatus(serverItems)
                        messenger.saveMessages(serverItems)
                        clearUnreadPush()
                        processSystemMessages(serverItems)
                        return@fromCallable setLastAvatars(serverItems)
                    } catch (e: Exception) {
                        error(ThreadsApi.REST_TAG, "Requesting history items error", e)
                        return@fromCallable setLastAvatars(
                            database.getChatItems(
                                currentItemsCount,
                                count
                            )
                        )
                    }
                }
                java.util.ArrayList()
            }
            .subscribeOn(Schedulers.io())
    }

    fun onFileDownloadRequest(fileDescription: FileDescription?) {
        if (fragment?.isAdded == true) {
            fragment?.activity?.let {
                if (fileDescription != null) {
                    startDownloadWithNoStop(it, fileDescription)
                }
            }
        }
    }

    fun onConsultChoose(activity: Activity?, consultId: String?) {
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

    val isNeedToShowWelcome: Boolean
        get() = database.getMessagesCount() == 0

    val stateOfConsult: Int
        get() = if (consultWriter.isSearchingConsult) {
            CONSULT_STATE_SEARCHING
        } else if (consultWriter.isConsultConnected) {
            CONSULT_STATE_FOUND
        } else {
            CONSULT_STATE_DEFAULT
        }

    val isConsultFound: Boolean
        get() = isChatWorking && consultWriter.isConsultConnected

    val currentConsultInfo: ConsultInfo?
        get() = consultWriter.currentConsultInfo

    fun bindFragment(chatFragment: ChatFragment?) {
        info("bindFragment: $chatFragment")
        val activity = chatFragment?.activity ?: return

        if (HistoryLoader.getHistoryMock(appContext).isNotEmpty()) {
            database.cleanDatabase()
        }

        fragment = chatFragment

        chatFragment.showProgressBar()
        loadItemsFromDB()
        if (consultWriter.isSearchingConsult) {
            chatFragment.setStateSearchingConsult()
        }
        if (!ThreadsLibBase.getInstance().isUserInitialized) {
            chatFragment.showEmptyState()
        }
        subscribe(
            Single.fromCallable {
                val historyLoadingCount = BaseConfig.instance.historyLoadingCount
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
                        chatFragment.addChatItems(chatItems)
                        handleQuickReplies(chatItems)
                        loadHistory()
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

    fun unbindFragment() {
        fragment?.let {
            val activity: Activity? = it.activity
            if (activity != null && progressReceiver != null) {
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(progressReceiver!!)
            }
        }
        fragment = null
    }

    private fun loadItemsFromDB() {
        fragment?.let {
            it.addChatItems(database.getChatItems(0, -1))
            it.hideProgressBar()
        }
    }

    fun setMessagesInCurrentThreadAsReadInDB() {
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

    fun clearUnreadPushCount() {
        preferences.save(PreferencesCoreKeys.UNREAD_PUSH_COUNT, 0)
    }

    private fun subscribe(event: Disposable): Boolean {
        if (compositeDisposable == null || compositeDisposable?.isDisposed == true) {
            compositeDisposable = CompositeDisposable()
        }
        return compositeDisposable?.add(event) == true
    }

    fun setAllMessagesWereRead() {
        removePushNotification()
        subscribe(
            database.setAllConsultMessagesWereRead()
                .subscribe(
                    { UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount() }
                ) { error: Throwable? -> error("setAllMessagesWereRead() ", error) }
        )
        fragment?.setAllMessagesWereRead()
    }

    fun isMessageSent(correlationId: String?): Boolean {
        return database
            .getNotDeliveredChatItems()
            .firstOrNull { it.id == correlationId } == null
    }

    private val isChatWorking: Boolean
        get() = currentScheduleInfo == null || currentScheduleInfo?.isChatWorking == true

    @Throws(Exception::class)
    private fun onClientIdChanged(): List<ChatItem> {
        info(ThreadsApi.REST_TAG, "Client id changed. Loading history.")
        cleanAll()
        fragment?.removeSearching()
        val response = historyLoader.getHistorySync(null, true)
        var serverItems = HistoryParser.getChatItems(response)
        serverItems = addLocalUserMessages(serverItems)
        parseHistoryItemsForSentStatus(serverItems)
        messenger.saveMessages(serverItems)
        clearUnreadPush()
        processSystemMessages(serverItems)
        fragment?.let { chatFragment ->
            response?.consultInfo?.let { chatFragment.setStateConsultConnected(it) }
        }
        return setLastAvatars(serverItems)
    }

    fun loadHistory(fromBeginning: Boolean) {
        if (isAllMessagesDownloaded) {
            return
        }
        synchronized(this) {
            if (!isDownloadingMessages) {
                info(
                    ThreadsApi.REST_TAG,
                    "Loading history from ${ChatController::class.java.simpleName}"
                )
                isDownloadingMessages = true
                subscribe(
                    Single.fromCallable {
                        val count = BaseConfig.instance.historyLoadingCount
                        val response = historyLoader.getHistorySync(
                            count,
                            fromBeginning
                        )
                        val serverItems = HistoryParser.getChatItems(response)
                        if (serverItems.size == 0) {
                            isAllMessagesDownloaded = true
                        }
                        parseHistoryItemsForSentStatus(serverItems)
                        parseHistoryItemsForAttachmentStatus(serverItems)
                        messenger.saveMessages(serverItems)
                        processSystemMessages(serverItems)
                        if (fragment != null && isActive) {
                            val uuidList: List<String?> = database.getUnreadMessagesUuid()
                            if (uuidList.isNotEmpty()) {
                                BaseConfig.instance.transport.markMessagesAsRead(uuidList)
                            }
                        }
                        Pair(response?.consultInfo, serverItems.size)
                    }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { _: Pair<ConsultInfo?, Int?> ->
                                if (fragment != null) {
                                    val items = database.getChatItems(0, -1)
                                    fragment?.addHistoryChatItems(items)
                                    fragment?.hideProgressBar()
                                }
                                isDownloadingMessages = false
                            }
                        ) { e: Throwable? ->
                            if (fragment != null) {
                                fragment?.hideProgressBar()
                            }
                            error(e)
                            isDownloadingMessages = false
                        }
                )
            } else {
                info(ThreadsApi.REST_TAG, "Loading history cancelled. isDownloadingMessages = true")
            }
        }
    }

    fun loadHistory() {
        if (isAllMessagesDownloaded) {
            return
        }

        if (!isDownloadingMessages) {
            info(
                ThreadsApi.REST_TAG,
                "Loading history from ${ChatController::class.java.simpleName}"
            )
            isDownloadingMessages = true
            subscribe(
                Single.fromCallable {
                    var count = BaseConfig.instance.historyLoadingCount
                    if (count < database.getMessagesCount()) {
                        count = database.getMessagesCount()
                    }
                    val response = historyLoader.getHistorySync(
                        count,
                        true
                    )
                    val serverItems = HistoryParser.getChatItems(response)
                    if (serverItems.size == 0) {
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
                            BaseConfig.instance.transport.markMessagesAsRead(uuidList)
                        }
                    }
                    Pair(response?.consultInfo, serverItems.size)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { pair: Pair<ConsultInfo?, Int?> ->
                            isDownloadingMessages = false
                            val serverCount = if (pair.second == null) 0 else pair.second!!
                            val items = setLastAvatars(database.getChatItems(0, serverCount))
                            if (fragment != null) {
                                fragment?.addChatItems(items)
                                handleQuickReplies(items)
                                handleInputAvailability(items)
                                val info = pair.first
                                if (info != null) {
                                    fragment?.setStateConsultConnected(info)
                                }
                                fragment?.hideProgressBar()
                            }
                        }
                    ) { e: Throwable? ->
                        isDownloadingMessages = false
                        if (fragment != null) {
                            fragment?.hideProgressBar()
                        }
                        error(e)
                    }
            )
        } else {
            info(ThreadsApi.REST_TAG, "Loading history cancelled. isDownloadingMessages = true")
        }
    }

    val settings: Unit
        get() {
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
                                info("getting settings : $responseBody")
                                val clientNotificationType =
                                    responseBody.clientNotificationDisplayType
                                if (clientNotificationType != null && clientNotificationType.isNotEmpty()) {
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
                        info("error on getting settings : " + e.message)
                        chatUpdateProcessor.postError(TransportException(e.message))
                    }
            )
        }

    private fun getScheduleInfo(fullMessage: JsonObject?): ScheduleInfo? {
        return fullMessage?.get("content")?.let {
            val scheduleInfo = BaseConfig.instance.gson.fromJson(it, ScheduleInfo::class.java)
            scheduleInfo.date = Date().time
            scheduleInfo
        }
    }

    fun downloadMessagesTillEnd(): Single<List<ChatItem>> {
        return messenger.downloadMessagesTillEnd()
    }

    fun forceResend(userPhrase: UserPhrase?) {
        if (isScheduleActive(currentScheduleInfo) && userPhrase != null) {
            messenger.forceResend(userPhrase)
        }
    }

    fun removeUserPhraseFromDatabaseAsync(userPhrase: UserPhrase) {
        coroutineScope.launch(Dispatchers.IO) {
            database.removeItem(userPhrase.id, userPhrase.backendMessageId)
        }
    }

    private fun addLocalUserMessages(serverItems: List<ChatItem>): List<ChatItem> {
        val items = serverItems.toMutableList()
        val localMessagesToDelete = java.util.ArrayList<UserPhrase>()
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

    fun hideEmptyState() {
        if (ThreadsLibBase.getInstance().isUserInitialized && fragment != null && fragment?.isAdded == true) {
            fragment?.hideEmptyState()
        }
    }

    private fun subscribeToChatEvents() {
        subscribeToTyping()
        subscribeToOutgoingMessageStatusChanged()
        subscribeToIncomingMessageRead()
        subscribeToNewMessage()
        subscribeToUpdateAttachments()
        subscribeToMessageSendSuccess()
        subscribeToTransportException()
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
    }

    fun checkSubscribing() {
        if (fragment?.isAdded != true) return
        if (!chatUpdateProcessor.typingProcessor.hasSubscribers()) subscribeToTyping()
        if (!chatUpdateProcessor.outgoingMessageStatusChangedProcessor.hasSubscribers()) subscribeToOutgoingMessageStatusChanged()
        if (!chatUpdateProcessor.incomingMessageReadProcessor.hasSubscribers()) subscribeToIncomingMessageRead()
        if (!chatUpdateProcessor.newMessageProcessor.hasSubscribers()) subscribeToNewMessage()
        if (!chatUpdateProcessor.updateAttachmentsProcessor.hasSubscribers()) subscribeToUpdateAttachments()
        if (!chatUpdateProcessor.messageSendSuccessProcessor.hasSubscribers()) subscribeToMessageSendSuccess()
        if (!chatUpdateProcessor.errorProcessor.hasSubscribers()) subscribeToTransportException()
        if (!chatUpdateProcessor.campaignMessageReplySuccessProcessor.hasSubscribers()) subscribeToCampaignMessageReplySuccess()
        if (!chatUpdateProcessor.messageSendErrorProcessor.hasSubscribers()) subscribeToMessageSendError()
        if (!chatUpdateProcessor.surveySendSuccessProcessor.hasSubscribers()) subscribeToSurveySendSuccess()
        if (!chatUpdateProcessor.removeChatItemProcessor.hasSubscribers()) subscribeToRemoveChatItem()
        if (!chatUpdateProcessor.deviceAddressChangedProcessor.hasSubscribers()) subscribeToDeviceAddressChanged()
        if (!chatUpdateProcessor.quickRepliesProcessor.hasSubscribers()) subscribeToQuickReplies()
        if (!chatUpdateProcessor.attachAudioFilesProcessor.hasSubscribers()) subscribeToAttachAudioFiles()
        if (!chatUpdateProcessor.clientNotificationDisplayTypeProcessor.hasSubscribers()) subscribeToClientNotificationDisplayTypeProcessor()
        if (!chatUpdateProcessor.speechMessageUpdateProcessor.hasSubscribers()) subscribeSpeechMessageUpdated()
        if (!messenger.resendStream.hasObservers()) subscribeForResendMessage()
        if (!messageErrorProcessor.hasObservers()) subscribeOnMessageError()
        if (!chatUpdateProcessor.uploadResultProcessor.hasSubscribers()) subscribeOnFileUploadResult()
    }

    private fun subscribeToTransportException() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.errorProcessor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    error("subscribeToTransportException ", it)
                }
        )
    }

    private fun subscribeToCampaignMessageReplySuccess() {
        subscribe(
            Flowable.fromPublisher(chatUpdateProcessor.campaignMessageReplySuccessProcessor)
                .observeOn(Schedulers.io())
                .delay(1000, TimeUnit.MILLISECONDS)
                .subscribe({ loadHistory() }) { onError: Throwable? ->
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
                        info(
                            "Received new status: ${status.status.name}, correlationId: ${status.correlationId}," +
                                " messageId: ${status.messageId}"
                        )
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
                                        if ((incomingNameEquals || isUrlHashFileName) && fragment?.isAdded == true) {
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
                    if (chatItem is MessageRead) {
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
                    } else if (chatItem is ScheduleInfo) {
                        currentScheduleInfo = chatItem
                        currentScheduleInfo?.calculateServerTimeDiff()
                        refreshUserInputState()
                        if (!isChatWorking) {
                            consultWriter.isSearchingConsult = false
                            fragment?.removeSearching()
                            fragment?.setTitleStateDefault()
                        }
                        fragment?.addChatItem(currentScheduleInfo)
                    } else if (chatItem is ConsultConnectionMessage) {
                        processConsultConnectionMessage(chatItem)
                    } else if (chatItem is SearchingConsult) {
                        fragment?.setStateSearchingConsult()
                        consultWriter.isSearchingConsult = true
                        return@doOnNext
                    } else if (chatItem is SimpleSystemMessage) {
                        processSimpleSystemMessage(chatItem)
                    } else if (chatItem is ConsultPhrase) {
                        refreshUserInputState(chatItem.isBlockInput)
                    }
                    addMessage(chatItem)
                }
                .filter { chatItem: ChatItem? -> chatItem is Hidable }
                .map { chatItem: ChatItem -> chatItem as Hidable }
                .delay { item: Hidable -> Flowable.timer(item.hideAfter, TimeUnit.SECONDS) }
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
                        debug("server answer on phrase sent with id " + chatItemSent.messageId)
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
                        debug("server answer on phrase sent with id $phraseUuid")
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
                        if (hasQuickReplies) {
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
        info("removeResolveRequest")
        subscribe(
            database.setOldRequestResolveThreadDisplayMessageToFalse()
                .subscribe(
                    { info("removeResolveRequest") }
                ) { obj: Throwable -> obj.message }
        )
        fragment?.removeResolveRequest()
    }

    private fun removeActiveSurvey() {
        info("removeActiveSurvey")
        subscribe(
            database.setNotSentSurveyDisplayMessageToFalse()
                .subscribe(
                    { info("setOldSurveyDisplayMessageToFalse") }
                ) { obj: Throwable -> obj.message }
        )
        fragment?.let { chatFragment ->
            activeSurvey?.sendingId?.let { chatFragment.removeSurvey(it) }
        }
    }

    private fun resetActiveSurvey() {
        info("resetActiveSurvey")
        activeSurvey = null
    }

    /**
     *  Вызывается когда получено новое сообщение из канала (TG/PUSH)
     */
    private fun addMessage(chatItem: ChatItem) {
        info("addMessage: $chatItem")
        if (chatItem is Survey) {
            chatItem.questions.forEach { it.generateCorrelationId() }
        }
        database.putChatItem(chatItem)
        UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount()
        if (fragment != null) {
            val ci = setLastAvatars(listOf(chatItem))[0]
            if (ci !is ConsultConnectionMessage || ci.isDisplayMessage) {
                fragment?.addChatItem(ci)
            }
            if (ci is ConsultChatPhrase) {
                fragment?.notifyConsultAvatarChanged(
                    (ci as ConsultChatPhrase).avatarPath,
                    (ci as ConsultChatPhrase).consultId
                )
            }
        }
        if (chatItem is ConsultPhrase && isActive) {
            handleQuickReplies(listOf<ChatItem>(chatItem))
        }
        if (chatItem is SimpleSystemMessage && isActive) {
            hideQuickReplies()
        }
        if (chatItem is Survey && isActive) {
            BaseConfig.instance.transport.markMessagesAsRead(listOf(chatItem.uuid))
        }
        if (chatItem is RequestResolveThread && isActive) {
            BaseConfig.instance.transport.markMessagesAsRead(listOf(chatItem.uuid))
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
            chatItem is ConsultConnectionMessage && ChatItemType.OPERATOR_JOINED.name == chatItem.type ||
            chatItem is ScheduleInfo && chatItem.isChatWorking
        ) {
            if (fragment?.isAdded == true) {
                fragment?.removeSchedule(false)
            }
        }
    }

    fun cleanAll() {
        info("cleanAll: ")
        isAllMessagesDownloaded = false
        messenger.clearSendQueue()
        database.cleanDatabase()
        fragment?.cleanChat()
        threadId = -1L
        consultWriter.setCurrentConsultLeft()
        consultWriter.isSearchingConsult = false
        removePushNotification()
        UnreadMessagesController.INSTANCE.refreshUnreadMessagesCount()
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
            subscribe(
                Single.fromCallable {
                    BaseConfig.instance.transport.sendInit(false)
                    val response = historyLoader.getHistorySync(
                        null,
                        true
                    )
                    var chatItems = HistoryParser.getChatItems(response)
                    chatItems = addLocalUserMessages(chatItems)
                    parseHistoryItemsForSentStatus(chatItems)
                    messenger.saveMessages(chatItems)
                    clearUnreadPush()
                    processSystemMessages(chatItems)
                    androidx.core.util.Pair(response?.consultInfo, setLastAvatars(chatItems))
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { pair: androidx.core.util.Pair<ConsultInfo?, List<ChatItem>> ->
                            val chatItems = pair.second
                            if (fragment != null) {
                                fragment?.addChatItems(chatItems)
                                handleQuickReplies(chatItems)
                                val info = pair.first
                                if (info != null) {
                                    fragment?.setStateConsultConnected(info)
                                }
                            }
                        }
                    ) { obj: Throwable -> obj.message }
            )
        } else {
            info(
                ThreadsApi.REST_TAG,
                "Loading history cancelled in onDeviceAddressChanged. " +
                    "fragment != null && !TextUtils.isEmpty(clientId) == false"
            )
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
        if (!isChatWorking) {
            return
        }
        var latestSystemMessage: ChatItem? = null
        for (chatItem in chatItems) {
            if (chatItem is SystemMessage) {
                val threadId = chatItem.threadId
                if (threadId != null && threadId >= threadId) {
                    val type = (chatItem as SystemMessage).type
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
        if (ccm.type.equals(ChatItemType.OPERATOR_JOINED.name, ignoreCase = true)) {
            if (ccm.threadId != null) {
                threadId = ccm.threadId ?: 0L
                fragment?.setCurrentThreadId(ccm.threadId ?: 0L)
            }
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
        val type = systemMessage.type
        if (ChatItemType.THREAD_CLOSED.name.equals(type, ignoreCase = true)) {
            threadId = -1L
            removeResolveRequest()
            consultWriter.setCurrentConsultLeft()
            if (!consultWriter.isSearchingConsult) {
                fragment?.setTitleStateDefault()
            }
        } else {
            if (systemMessage.threadId != null) {
                threadId = systemMessage.threadId ?: 0L
                fragment?.setCurrentThreadId(systemMessage.threadId ?: 0L)
            }
            if (ChatItemType.THREAD_ENQUEUED.name.equals(type, ignoreCase = true) ||
                ChatItemType.THREAD_WILL_BE_REASSIGNED.name.equals(type, ignoreCase = true) ||
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
        info("UserInputState_change. isInputBlockedFromMessage: $isInputBlockedFromMessage, $inputFieldEnableModel")
        fragment?.updateInputEnable(inputFieldEnableModel)
        fragment?.updateChatAvailabilityMessage(inputFieldEnableModel)
    }

    private fun isInputFieldEnabled(): Boolean {
        val fileDescription = try {
            fragment?.fileDescription?.get()?.get()
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
        return scheduleInfo?.isChatWorking == true || scheduleInfo?.isSendDuringInactive == true
    }

    private fun handleQuickReplies(chatItems: List<ChatItem>) {
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

    fun hideQuickReplies() {
        chatUpdateProcessor.postQuickRepliesChanged(QuickReplyItem(java.util.ArrayList(), 0))
    }

    private fun getQuickReplyMessageCandidate(chatItems: List<ChatItem>?): ConsultPhrase? {
        if (chatItems != null && chatItems.isNotEmpty()) {
            val listIterator = chatItems.listIterator(chatItems.size)
            while (listIterator.hasPrevious()) {
                val chatItem = listIterator.previous()
                // При некоторых ситуациях (пока неизвестно каких) последнее сообщение в истории ConsultConnectionMessage, который не отображается, его нужно игнорировать
                if (chatItem is ConsultConnectionMessage) {
                    if (!chatItem.isDisplayMessage) {
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
        instance?.subscribe(
            Single.fromCallable {
                val userInfo = clientUseCase.getUserInfo()
                val newClientId = clientUseCase.getTagNewClientId()
                val oldClientId = userInfo?.clientId
                if (!newClientId.isNullOrEmpty() && newClientId != oldClientId) {
                    instance?.onClientIdChanged() ?: ArrayList()
                } else {
                    ArrayList()
                }
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { chatItems: List<ChatItem> ->
                        if (instance?.fragment != null) {
                            instance?.fragment?.addChatItems(chatItems)
                            instance?.handleQuickReplies(chatItems)
                        }
                    }
                ) { obj: Throwable -> obj.message }
        )
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
