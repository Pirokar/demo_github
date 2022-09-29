package im.threads.ui.config

import android.content.Context
import im.threads.business.config.BaseConfig
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.rest.config.RequestConfig
import im.threads.ui.ChatStyle
import im.threads.ui.core.PendingIntentCreator
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle.Companion.getDefaultDialogStyle
import im.threads.ui.styles.permissions.PermissionDescriptionType
import im.threads.ui.utils.MetadataUi
import im.threads.ui.utils.preferences.PrefUtilsUi
import okhttp3.Interceptor

class Config(
    context: Context,
    serverBaseUrl: String?,
    datastoreUrl: String?,
    threadsGateUrl: String?,
    threadsGateProviderUid: String?,
    threadsGateHCMProviderUid: String?,
    isNewChatCenterApi: Boolean?,
    loggerConfig: LoggerConfig?,
    val pendingIntentCreator: PendingIntentCreator,
    unreadMessagesCountListener: UnreadMessagesCountListener?,
    networkInterceptor: Interceptor?,
    chatStyle: ChatStyle?,
    isDebugLoggingEnabled: Boolean,
    historyLoadingCount: Int,
    surveyCompletionDelay: Int,
    requestConfig: RequestConfig,
    certificateRawResIds: List<Int>?
) : BaseConfig(
    context, serverBaseUrl, datastoreUrl, threadsGateUrl, threadsGateProviderUid, threadsGateHCMProviderUid,
    isNewChatCenterApi, loggerConfig, unreadMessagesCountListener, networkInterceptor,
    isDebugLoggingEnabled, historyLoadingCount, surveyCompletionDelay, requestConfig, certificateRawResIds
) {
    @Volatile
    private var chatStyle: ChatStyle? = null

    @Volatile
    private var storagePermissionDescriptionDialogStyle: PermissionDescriptionDialogStyle? = null

    @Volatile
    private var recordAudioPermissionDescriptionDialogStyle: PermissionDescriptionDialogStyle? = null

    @Volatile
    private var cameraPermissionDescriptionDialogStyle: PermissionDescriptionDialogStyle? = null

    var attachmentEnabled = false
    var filesAndMediaMenuItemEnabled = false

    init {
        attachmentEnabled = MetadataUi.getAttachmentEnabled(this.context)
        filesAndMediaMenuItemEnabled = MetadataUi.getFilesAndMediaMenuItemEnabled(this.context)
        setChatStyle(chatStyle)
    }

    fun getChatStyle(): ChatStyle {
        if (chatStyle == null) {
            synchronized(ChatStyle::class.java) {
                chatStyle = PrefUtilsUi.incomingStyle ?: ChatStyle()
            }
        }
        return chatStyle!!
    }

    fun setChatStyle(style: ChatStyle?) {
        style?.let {
            chatStyle = it
            PrefUtilsUi.setIncomingStyle(it)
        }
    }

    fun getStoragePermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle {
        if (storagePermissionDescriptionDialogStyle == null) {
            storagePermissionDescriptionDialogStyle = PrefUtilsUi.getIncomingStyle(PermissionDescriptionType.STORAGE)
                ?: getDefaultDialogStyle(PermissionDescriptionType.STORAGE)
        }
        return storagePermissionDescriptionDialogStyle!!
    }

    fun setStoragePermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?) {
        style?.let {
            storagePermissionDescriptionDialogStyle = it
            PrefUtilsUi.setIncomingStyle(PermissionDescriptionType.STORAGE, it)
        }
    }

    fun getRecordAudioPermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle {
        if (recordAudioPermissionDescriptionDialogStyle == null) {
            recordAudioPermissionDescriptionDialogStyle = PrefUtilsUi.getIncomingStyle(PermissionDescriptionType.RECORD_AUDIO)
                ?: getDefaultDialogStyle(PermissionDescriptionType.RECORD_AUDIO)
        }
        return recordAudioPermissionDescriptionDialogStyle!!
    }

    fun setRecordAudioPermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?) {
        style?.let {
            recordAudioPermissionDescriptionDialogStyle = it
            PrefUtilsUi.setIncomingStyle(PermissionDescriptionType.RECORD_AUDIO, it)
        }
    }

    fun getCameraPermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle {
        if (cameraPermissionDescriptionDialogStyle == null) {
            cameraPermissionDescriptionDialogStyle = PrefUtilsUi.getIncomingStyle(PermissionDescriptionType.CAMERA)
                ?: getDefaultDialogStyle(PermissionDescriptionType.CAMERA)
        }

        return cameraPermissionDescriptionDialogStyle!!
    }

    fun setCameraPermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?) {
        style?.let {
            cameraPermissionDescriptionDialogStyle = it
            PrefUtilsUi.setIncomingStyle(PermissionDescriptionType.CAMERA, it)
        }
    }

    companion object {
        @JvmStatic
        private var instance: Config? = null

        @JvmStatic
        fun getInstance(): Config {
            if (instance == null) {
                throw NullPointerException("Config instance is not initialized. Called from business logic?")
            }

            return instance!!
        }

        fun setInstance(config: Config) {
            instance = config
        }
    }
}
