package im.threads

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.util.ObjectsCompat
import im.threads.internal.Config
import im.threads.internal.chat_updates.ChatUpdateProcessor
import im.threads.internal.controllers.ChatController
import im.threads.internal.controllers.UnreadMessagesController
import im.threads.internal.domain.audio_converter.AudioConverter
import im.threads.internal.domain.audio_converter.callback.ILoadCallback
import im.threads.internal.helpers.FileProviderHelper
import im.threads.internal.model.FileDescription
import im.threads.internal.model.UpcomingUserMessage
import im.threads.internal.useractivity.LastUserActivityTimeCounterSingletonProvider.getLastUserActivityTimeCounter
import im.threads.internal.utils.FileUtils.getFileSize
import im.threads.internal.utils.PrefUtils
import im.threads.internal.utils.ThreadsLogger
import im.threads.styles.permissions.PermissionDescriptionDialogStyle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.processors.FlowableProcessor
import java.io.File

@Suppress("unused")
class ThreadsLib private constructor(context: Context?) {
    /**
     * @return time in seconds since the last user activity
     */
    val secondsSinceLastActivity: Long
        get() {
            val timeCounter = getLastUserActivityTimeCounter()
            return timeCounter.getSecondsSinceLastActivity()
        }

    val isUserInitialized: Boolean get() = !PrefUtils.isClientIdEmpty

    /**
     * @return FlowableProcessor that emits responses from WebSocket connection
     */
    val socketResponseMapProcessor: FlowableProcessor<Map<String, Any>>
        get() = ChatUpdateProcessor.getInstance().socketResponseMapProcessor

    fun initUser(userInfoBuilder: UserInfoBuilder) {
        if (!Config.instance.clientIdIgnoreEnabled) {
            val currentClientId = PrefUtils.clientID
            if (currentClientId != null && !ObjectsCompat.equals(
                    currentClientId,
                    userInfoBuilder.clientId
                )
            ) {
                logoutClient(currentClientId)
            }
        } else {
            // it will only affect GPB, every time they try to init user we will delete user related data
            ChatController.getInstance().cleanAll()
        }
        PrefUtils.appMarker = userInfoBuilder.appMarker
        PrefUtils.setNewClientId(userInfoBuilder.clientId)
        PrefUtils.authToken = userInfoBuilder.authToken
        PrefUtils.authSchema = userInfoBuilder.authSchema
        PrefUtils.clientIdSignature = userInfoBuilder.clientIdSignature
        PrefUtils.userName = userInfoBuilder.userName
        PrefUtils.data = userInfoBuilder.clientData
        PrefUtils.setClientIdEncrypted(userInfoBuilder.clientIdEncrypted)
        ChatController.getInstance().sendInit()
        ChatController.getInstance().loadHistory()
    }

    fun applyChatStyle(chatStyle: ChatStyle?) {
        Config.instance.applyChatStyle(chatStyle)
    }

    fun applyStoragePermissionDescriptionDialogStyle(
        dialogStyle: PermissionDescriptionDialogStyle
    ) {
        Config.instance.applyStoragePermissionDescriptionDialogStyle(dialogStyle)
    }

    fun applyRecordAudioPermissionDescriptionDialogStyle(
        dialogStyle: PermissionDescriptionDialogStyle
    ) {
        Config.instance.applyRecordAudioPermissionDescriptionDialogStyle(dialogStyle)
    }

    fun applyCameraPermissionDescriptionDialogStyle(
        dialogStyle: PermissionDescriptionDialogStyle
    ) {
        Config.instance.applyCameraPermissionDescriptionDialogStyle(dialogStyle)
    }

    /**
     * Used to stop receiving messages for user with provided clientId
     */
    fun logoutClient(clientId: String) {
        if (!TextUtils.isEmpty(clientId)) {
            Config.instance.transport.sendClientOffline(clientId)
        } else {
            ThreadsLogger.i(javaClass.simpleName, "clientId must not be empty")
        }
    }

    /**
     * Used to post messages to chat as if written by client
     *
     * @return true, if message was successfully added to messaging queue, otherwise false
     */
    fun sendMessage(message: String?, file: File?): Boolean {
        val fileUri = if (file != null) FileProviderHelper.getUriForFile(
            Config.instance.context,
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
        return if (!PrefUtils.isClientIdEmpty) {
            var fileDescription: FileDescription? = null
            if (fileUri != null) {
                fileDescription = FileDescription(
                    Config.instance.context.getString(R.string.threads_I),
                    fileUri,
                    getFileSize(fileUri),
                    System.currentTimeMillis()
                )
            }
            val msg = UpcomingUserMessage(
                fileDescription, null, null, message, false
            )
            chatController.onUserInput(msg)
            true
        } else {
            ThreadsLogger.i(
                javaClass.simpleName,
                "You might need to initialize user first with ThreadsLib.userInfo()"
            )
            false
        }
    }

    interface PendingIntentCreator {
        fun create(context: Context, appMarker: String?): PendingIntent?
    }

    interface UnreadMessagesCountListener {
        fun onUnreadMessagesCountChanged(count: Int)
    }

    companion object {
        private val TAG = ThreadsLib::class.java.simpleName
        private var instance: ThreadsLib? = null

        @JvmStatic
        fun getLibVersion() = BuildConfig.VERSION_NAME

        @SuppressLint("CheckResult")
        @JvmStatic
        fun init(configBuilder: ConfigBuilder) {
            check(instance == null) { "ThreadsLib has already been initialized" }
            Config.instance = configBuilder.build()
            instance = ThreadsLib(configBuilder.context)
            PrefUtils.migrateMainSharedPreferences()

            Config.instance.unreadMessagesCountListener?.let { unreadMessagesCountListener ->
                unreadMessagesCountListener.onUnreadMessagesCountChanged(
                    UnreadMessagesController.INSTANCE.unreadMessages
                )
                UnreadMessagesController.INSTANCE.unreadMessagesPublishProcessor
                    .distinctUntilChanged()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { count: Int ->
                            unreadMessagesCountListener.onUnreadMessagesCountChanged(count)
                        }
                    ) { error: Throwable -> ThreadsLogger.e(TAG, "init " + error.message) }
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                try {
                    AudioConverter.load(
                        Config.instance.context,
                        object : ILoadCallback {
                            override fun onSuccess() {
                                ThreadsLogger.i(TAG, "AndroidAudioConverter was successfully loaded")
                            }

                            override fun onFailure(error: Exception) {
                                ThreadsLogger.e(TAG, "AndroidAudioConverter failed to load", error)
                            }
                        }
                    )
                } catch (e: UnsatisfiedLinkError) {
                    ThreadsLogger.e(
                        TAG,
                        "AndroidAudioConverter failed to load (UnsatisfiedLinkError)",
                        e
                    )
                }
            }
            ChatController.getInstance()
            getLastUserActivityTimeCounter()
            if (RxJavaPlugins.getErrorHandler() == null) {
                RxJavaPlugins.setErrorHandler { throwable: Throwable? ->
                    var throwableCause: Throwable? = null
                    if (throwable is UndeliverableException) {
                        throwableCause = throwable.cause
                        if (throwableCause != null) {
                            ThreadsLogger.e(TAG, "global handler: ", throwableCause)
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
        }

        @JvmStatic
        fun getInstance(): ThreadsLib {
            checkNotNull(instance) { "ThreadsLib should be initialized first with ThreadsLib.init()" }
            return instance ?: ThreadsLib(null)
        }
    }
}
