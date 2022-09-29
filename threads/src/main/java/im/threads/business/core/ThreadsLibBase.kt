package im.threads.business.core

import android.annotation.SuppressLint
import android.os.Build
import android.text.TextUtils
import im.threads.BuildConfig
import im.threads.business.UserInfoBuilder
import im.threads.business.audio.audioConverter.AudioConverter
import im.threads.business.audio.audioConverter.callback.ILoadCallback
import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.config.BaseConfigBuilder
import im.threads.business.controllers.UnreadMessagesController
import im.threads.business.logger.VersionLogger
import im.threads.business.logger.core.LoggerEdna
import im.threads.business.logger.core.LoggerEdna.info
import im.threads.business.models.CampaignMessage
import im.threads.business.rest.queries.BackendApi
import im.threads.business.rest.queries.DatastoreApi
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter
import im.threads.business.useractivity.UserActivityTimeProvider.initializeLastUserActivity
import im.threads.business.utils.ClientInteractor
import im.threads.business.utils.preferences.PrefUtilsBase
import im.threads.business.utils.preferences.PreferencesMigrationBase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.processors.FlowableProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Suppress("unused")
open class ThreadsLibBase protected constructor() {
    /**
     * @return time in seconds since the last user activity
     */
    val secondsSinceLastActivity: Long
        get() {
            val timeCounter = getLastUserActivityTimeCounter()
            return timeCounter.getSecondsSinceLastActivity()
        }

    val isUserInitialized: Boolean get() = !PrefUtilsBase.isClientIdEmpty

    /**
     * @return FlowableProcessor that emits responses from WebSocket connection
     */
    val socketResponseMapProcessor: FlowableProcessor<Map<String, Any>>
        get() = ChatUpdateProcessor.getInstance().socketResponseMapProcessor

    protected open fun initUser(userInfoBuilder: UserInfoBuilder) {
        PrefUtilsBase.appMarker = userInfoBuilder.appMarker
        PrefUtilsBase.setNewClientId(userInfoBuilder.clientId)
        PrefUtilsBase.authToken = userInfoBuilder.authToken
        PrefUtilsBase.authSchema = userInfoBuilder.authSchema
        PrefUtilsBase.clientIdSignature = userInfoBuilder.clientIdSignature
        PrefUtilsBase.userName = userInfoBuilder.userName
        PrefUtilsBase.data = userInfoBuilder.clientData
        PrefUtilsBase.setClientIdEncrypted(userInfoBuilder.clientIdEncrypted)
        BaseConfig.instance.transport.sendInit()
    }

    /**
     * Used to stop receiving messages for user with provided clientId
     */
    fun logoutClient(clientId: String) {
        if (!TextUtils.isEmpty(clientId)) {
            BaseConfig.instance.transport.sendClientOffline(clientId)
        } else {
            info("clientId must not be empty")
        }
    }

    /**
     * Устанавливает [CampaignMessage], который необходим для цитирования сообщений
     */
    fun setCampaignMessage(campaignMessage: CampaignMessage) {
        PrefUtilsBase.campaignMessage = campaignMessage
    }

    companion object {
        @JvmStatic
        protected var libInstance: ThreadsLibBase? = null
        private val clientInteractor: ClientInteractor = ClientInteractor()
        private val coroutineScope = CoroutineScope(Dispatchers.Main)

        @JvmStatic
        fun getLibVersion() = BuildConfig.VERSION_NAME

        @SuppressLint("CheckResult")
        @JvmStatic
        fun init(configBuilder: BaseConfigBuilder) {
            val startInitTime = System.currentTimeMillis()
            val isUIMode = BaseConfig.instance != null

            if (!isUIMode) {
                BaseConfig.instance = configBuilder.build()
                createLibInstance()
                BaseConfig.instance.loggerConfig?.let { LoggerEdna.init(it) }
                PreferencesMigrationBase().migrateMainSharedPreferences()
            }

            BackendApi.init(BaseConfig.instance)
            DatastoreApi.init(BaseConfig.instance)

            BaseConfig.instance.unreadMessagesCountListener?.let { unreadMessagesCountListener ->
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
                        BaseConfig.instance.context,
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
            showVersionsLog()

            info("Lib_init_time: ${System.currentTimeMillis() - startInitTime}ms")
        }

        @JvmStatic
        fun getInstance(): ThreadsLibBase {
            checkNotNull(libInstance) { "ThreadsLib should be initialized first with ThreadsLib.init()" }
            return libInstance ?: ThreadsLibBase()
        }

        @JvmStatic
        protected fun setLibraryInstance(instance: ThreadsLibBase) {
            libInstance = instance
        }

        private fun createLibInstance() {
            check(libInstance == null) { "ThreadsLib has already been initialized" }
            libInstance = ThreadsLibBase()
        }

        private fun showVersionsLog() {
            coroutineScope.launch(Dispatchers.IO) {
                VersionLogger().logVersions()
            }
        }
    }
}
