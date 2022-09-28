package im.threads.business.core

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import im.threads.BuildConfig
import im.threads.R
import im.threads.UserInfoBuilder
import im.threads.business.audio.audioConverter.AudioConverter
import im.threads.business.audio.audioConverter.callback.ILoadCallback
import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.config.BaseConfigBuilder
import im.threads.business.controllers.UnreadMessagesController
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.CampaignMessage
import im.threads.business.models.FileDescription
import im.threads.business.models.UpcomingUserMessage
import im.threads.business.rest.queries.BackendApi
import im.threads.business.rest.queries.DatastoreApi
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter
import im.threads.business.useractivity.UserActivityTimeProvider.initializeLastUserActivity
import im.threads.business.utils.FileProviderHelper
import im.threads.business.utils.FileUtils.getFileSize
import im.threads.business.utils.preferences.PrefUtilsBase
import im.threads.business.utils.preferences.PreferencesMigrationBase
import im.threads.ui.controllers.ChatController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.processors.FlowableProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

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

    fun initUser(userInfoBuilder: UserInfoBuilder) {
        // it will only affect GPB, every time they try to init user we will delete user related data
        ChatController.getInstance().cleanAll()
        PrefUtilsBase.appMarker = userInfoBuilder.appMarker
        PrefUtilsBase.setNewClientId(userInfoBuilder.clientId)
        PrefUtilsBase.authToken = userInfoBuilder.authToken
        PrefUtilsBase.authSchema = userInfoBuilder.authSchema
        PrefUtilsBase.clientIdSignature = userInfoBuilder.clientIdSignature
        PrefUtilsBase.userName = userInfoBuilder.userName
        PrefUtilsBase.data = userInfoBuilder.clientData
        PrefUtilsBase.setClientIdEncrypted(userInfoBuilder.clientIdEncrypted)
        ChatController.getInstance().sendInit()
        ChatController.getInstance().loadHistory()
    }

    /**
     * Used to stop receiving messages for user with provided clientId
     */
    fun logoutClient(clientId: String) {
        if (!TextUtils.isEmpty(clientId)) {
            BaseConfig.instance.transport.sendClientOffline(clientId)
        } else {
            LoggerEdna.info("clientId must not be empty")
        }
    }

    /**
     * Used to post messages to chat as if written by client
     *
     * @return true, if message was successfully added to messaging queue, otherwise false
     */
    fun sendMessage(message: String?, file: File?): Boolean {
        val fileUri = if (file != null) FileProviderHelper.getUriForFile(
            BaseConfig.instance.context,
            file
        ) else null
        return sendMessage(message, fileUri)
    }

    /**
     * Used to post messages to chat as if written by client
     *
     * @return true, if message was successfully added to messaging queue, otherwise false
     */
    fun sendMessage(message: String?, fileUri: Uri?): Boolean {
        val chatController = ChatController.getInstance()
        return if (!PrefUtilsBase.isClientIdEmpty) {
            var fileDescription: FileDescription? = null
            if (fileUri != null) {
                fileDescription = FileDescription(
                    BaseConfig.instance.context.getString(R.string.threads_I),
                    fileUri,
                    getFileSize(fileUri),
                    System.currentTimeMillis()
                )
            }
            val msg = UpcomingUserMessage(
                fileDescription,
                null,
                null,
                message,
                false
            )
            chatController.onUserInput(msg)
            true
        } else {
            LoggerEdna.info("You might need to initialize user first with ThreadsLib.userInfo()")
            false
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
                                LoggerEdna.info("AndroidAudioConverter was successfully loaded")
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
            ChatController.getInstance()
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

            LoggerEdna.info("Lib_init_time: ${System.currentTimeMillis() - startInitTime}ms")
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
    }
}
