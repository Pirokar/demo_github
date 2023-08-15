package im.threads.business.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import im.threads.BuildConfig
import im.threads.business.AuthMethod
import im.threads.business.UserInfoBuilder
import im.threads.business.audio.audioConverter.AudioConverter
import im.threads.business.audio.audioConverter.callback.ILoadCallback
import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.config.BaseConfigBuilder
import im.threads.business.controllers.UnreadMessagesController
import im.threads.business.logger.LoggerEdna
import im.threads.business.logger.LoggerEdna.info
import im.threads.business.models.CampaignMessage
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.rest.models.VersionsModel
import im.threads.business.rest.queries.BackendApi
import im.threads.business.rest.queries.DatastoreApi
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.serviceLocator.core.startEdnaLocator
import im.threads.business.serviceLocator.coreSLModule
import im.threads.business.state.ChatState
import im.threads.business.state.ChatStateEnum
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter
import im.threads.business.useractivity.UserActivityTimeProvider.initializeLastUserActivity
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.preferences.PreferencesMigrationBase
import im.threads.ui.controllers.ChatController
import im.threads.ui.fragments.ChatFragment
import im.threads.ui.serviceLocator.uiSLModule
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.processors.FlowableProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("unused")
open class ThreadsLibBase protected constructor(context: Context) {
    init {
        ContextHolder.context = context
        startEdnaLocator { modules(coreSLModule, uiSLModule) }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val preferences: Preferences by inject()
    private val clientUseCase: ClientUseCase by inject()
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private val database: DatabaseHolder by inject()
    private val chatState: ChatState by inject()

    /**
     * @return time in seconds since the last user activity
     */
    val secondsSinceLastActivity: Long
        get() {
            val timeCounter = getLastUserActivityTimeCounter()
            return timeCounter.getSecondsSinceLastActivity()
        }

    val isUserInitialized: Boolean get() {
        return clientUseCase.isClientIdNotEmpty()
    }

    var currentUiTheme: CurrentUiTheme
        get() {
            val intValue = preferences.get(PreferencesCoreKeys.USER_SELECTED_UI_THEME_KEY, CurrentUiTheme.SYSTEM.value)
            return CurrentUiTheme.fromInt(intValue)
        }
        set(value) {
            preferences.save(PreferencesCoreKeys.USER_SELECTED_UI_THEME_KEY, value.value)
        }

    /**
     * @return FlowableProcessor that emits responses from WebSocket connection
     */
    val socketResponseMapProcessor: FlowableProcessor<Map<String, Any>>
        get() = chatUpdateProcessor.socketResponseMapProcessor

    /**
     * Инициализирует пользователя
     * @param userInfoBuilder данные о пользователе
     * @param forceRegistration открывает сокет, отправляет данные о регистрации, закрывает сокет
     */
    open fun initUser(userInfoBuilder: UserInfoBuilder, forceRegistration: Boolean = false) {
        val clientId = clientUseCase.getUserInfo()?.clientId
        if (!clientId.isNullOrBlank()) {
            chatState.onLogout()
            BaseConfig.getInstance().transport.sendClientOffline(clientId) {
                ChatController.getInstance().cleanAll()
                clientUseCase.saveUserInfo(null)
                database.cleanDatabase()
                initUser(userInfoBuilder, forceRegistration)
            }
        }
        chatState.changeState(ChatStateEnum.LOGGING_IN)
        isForceRegistration = forceRegistration
        clientUseCase.saveUserInfo(userInfoBuilder)
        if (forceRegistration && preferences.get<String>(PreferencesCoreKeys.DEVICE_ADDRESS).isNullOrBlank()) {
            BaseConfig.getInstance().transport.sendRegisterDevice(true)
            if (!ChatFragment.isShown) {
                BaseConfig.getInstance().transport.closeWebSocket()
            }
        }
    }

    /**
     * Used to stop receiving messages for user
     */
    fun logoutClient() {
        chatState.onLogout()

        val clientId = clientUseCase.getUserInfo()?.clientId
        if (!clientId.isNullOrBlank()) {
            BaseConfig.getInstance().transport.sendClientOffline(clientId)
        } else {
            info("clientId must not be empty")
        }
        BaseConfig.getInstance().transport.closeWebSocket()
        clientUseCase.saveUserInfo(null)
        ChatController.getInstance().cleanAll()
    }

    /**
     * Устанавливает [CampaignMessage], который необходим для цитирования сообщений
     */
    fun setCampaignMessage(campaignMessage: CampaignMessage) {
        preferences.save(PreferencesCoreKeys.CAMPAIGN_MESSAGE, campaignMessage)
    }

    /**
     * Запускает обновление подключений в соответствии с текущими данными и способами авторизации
     *
     */
    fun updateAuthData(
        authToken: String?,
        authSchema: String?,
        authMethod: AuthMethod = AuthMethod.HEADERS
    ) {
        clientUseCase.getUserInfo()?.let {
            it.setAuthData(authToken, authSchema, authMethod)
            clientUseCase.saveUserInfo(it)
        }
        BaseConfig.getInstance().transport.buildTransport()
    }

    companion object {
        internal var isForceRegistration = false

        @JvmStatic
        @SuppressLint("StaticFieldLeak")
        protected var libInstance: ThreadsLibBase? = null

        private val clientInteractor: ClientUseCase by inject()

        private val coroutineScope = CoroutineScope(Dispatchers.Main)

        @JvmStatic
        fun getLibVersion() = BuildConfig.VERSION_NAME

        @JvmStatic
        fun init(configBuilder: BaseConfigBuilder) {
            val startInitTime = System.currentTimeMillis()

            createLibInstance(configBuilder.context)
            BaseConfig.setInstance(configBuilder.build())
            BaseConfig.getInstance().loggerConfig?.let { LoggerEdna.init(it) }
            PreferencesMigrationBase(BaseConfig.getInstance().context).apply {
                migrateMainSharedPreferences()
                migrateUserInfo()
            }
            initBaseParams()

            info("Lib_init_time: ${System.currentTimeMillis() - startInitTime}ms")
        }

        @SuppressLint("CheckResult")
        internal fun initBaseParams() {
            BackendApi.init(BaseConfig.getInstance())
            DatastoreApi.init(BaseConfig.getInstance())

            BaseConfig.getInstance().unreadMessagesCountListener?.let { unreadMessagesCountListener ->
                UnreadMessagesController.INSTANCE.unreadMessagesPublishProcessor
                    .distinctUntilChanged()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { count: Int ->
                            unreadMessagesCountListener.onUnreadMessagesCountChanged(count)
                        }
                    ) { error: Throwable -> LoggerEdna.error("init ${error.message}") }

                coroutineScope.launch {
                    val task = async(Dispatchers.IO) {
                        UnreadMessagesController.INSTANCE.unreadMessages
                    }
                    unreadMessagesCountListener.onUnreadMessagesCountChanged(task.await())
                }
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                try {
                    AudioConverter.load(
                        BaseConfig.getInstance().context,
                        object : ILoadCallback {
                            override fun onSuccess() {
                                info("AndroidAudioConverter was successfully loaded")
                            }

                            override fun onFailure(error: Exception) {
                                LoggerEdna.error("AndroidAudioConverter failed to load", error)
                            }
                        }
                    )
                } catch (e: UnsatisfiedLinkError) {
                    LoggerEdna.error(
                        "AndroidAudioConverter failed to load (UnsatisfiedLinkError)",
                        e
                    )
                }
            }
            clientInteractor.initClientId()
            initializeLastUserActivity()
            if (RxJavaPlugins.getErrorHandler() == null) {
                RxJavaPlugins.setErrorHandler { throwable: Throwable? ->
                    var throwableCause: Throwable? = null
                    if (throwable is UndeliverableException) {
                        throwableCause = throwable.cause
                        if (throwableCause != null) {
                            LoggerEdna.error("global handler: ", throwableCause)
                        }
                        return@setErrorHandler
                    }
                    throwableCause?.let {
                        Thread
                            .currentThread()
                            .uncaughtExceptionHandler
                            ?.uncaughtException(Thread.currentThread(), it)
                    }
                }
            }

            updateTransport()
            showVersionsLog()
        }

        /**
         * Меняет параметры подключения к серверу. Применяются не null параметры
         * @param baseUrl базовый url для основных бэкэнд запросов
         * @param datastoreUrl базовый url для работы с файлами
         * @param threadsGateUrl url вебсокета. Если не null,
         * должен быть не null также и параметр threadsGateProviderUid
         * @param threadsGateProviderUid uid для вебсокета. Если не null,
         * должен быть не null также и параметр threadsGateUrl
         * @param trustedSSLCertificates список id сертификатов
         * @param allowUntrustedSSLCertificate флаг разрешать ли использовать непроверенные сертификаты
         */
        @JvmStatic
        fun changeServerSettings(
            baseUrl: String? = null,
            datastoreUrl: String? = null,
            threadsGateUrl: String? = null,
            threadsGateProviderUid: String? = null,
            trustedSSLCertificates: List<Int>?,
            allowUntrustedSSLCertificate: Boolean
        ) {
            try {
                BaseConfig.getInstance().sslSocketFactoryConfig = null
                BaseConfig.getInstance().allowUntrustedSSLCertificate = allowUntrustedSSLCertificate
                if (baseUrl != null) BaseConfig.getInstance().serverBaseUrl = baseUrl
                if (datastoreUrl != null) BaseConfig.getInstance().datastoreUrl = datastoreUrl
                if (threadsGateUrl != null && threadsGateProviderUid != null) {
                    BaseConfig.getInstance().updateTransport(threadsGateUrl, threadsGateProviderUid, trustedSSLCertificates)
                }
                BackendApi.init(BaseConfig.getInstance())
                DatastoreApi.init(BaseConfig.getInstance())
            } catch (exc: Exception) {
                coroutineScope.launch {
                    LoggerEdna.error(exc)
                    delay(500)
                    throw exc
                }
            }
        }

        @JvmStatic
        fun getInstance(): ThreadsLibBase {
            checkNotNull(libInstance) { "ThreadsLib should be initialized first with ThreadsLib.init()" }
            return libInstance!!
        }

        @JvmStatic
        protected fun setLibraryInstance(instance: ThreadsLibBase) {
            libInstance = instance
        }

        protected fun createLibInstance(context: Context) {
            check(libInstance == null) { "ThreadsLib has already been initialized" }
            libInstance = ThreadsLibBase(context)
        }

        private fun updateTransport() {
            val threadsGateUrl = BaseConfig.getInstance().threadsGateUrl
            val threadsGateProviderUid = BaseConfig.getInstance().threadsGateProviderUid
            val trustedSSLCertificates = BaseConfig.getInstance().trustedSSLCertificates

            if (threadsGateUrl != null && threadsGateProviderUid != null) {
                BaseConfig.getInstance().updateTransport(
                    threadsGateUrl,
                    threadsGateProviderUid,
                    trustedSSLCertificates
                )
            }
        }

        private fun showVersionsLog() {
            if (BaseConfig.getInstance().loggerConfig != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    info("Getting versions from \"api/versions\"...")
                    val response = try {
                        BackendApi.get().versions()?.execute()
                    } catch (exc: Exception) {
                        null
                    }
                    if (response?.isSuccessful == true) {
                        response.body()?.let { info(it.toTableString()) }
                    } else {
                        info(
                            "Failed to get versions from \"api/versions\", " +
                                "error code: ${response?.code()}," +
                                " message: ${response?.message()}"
                        )
                        info(VersionsModel().toTableString())
                    }
                }
            }
        }
    }
}
