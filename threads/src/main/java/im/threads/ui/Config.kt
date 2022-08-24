package im.threads.ui

import android.content.Context
import im.threads.ChatStyle
import im.threads.ThreadsLib
import im.threads.business.logger.LoggerConfig
import im.threads.business.rest.config.RequestConfig
import im.threads.internal.config.BaseConfig
import im.threads.internal.config.UIConfig
import im.threads.internal.utils.MetaDataUtils
import im.threads.internal.utils.PrefUtils.Companion.getIncomingStyle
import im.threads.internal.utils.PrefUtils.Companion.incomingStyle
import im.threads.internal.utils.PrefUtils.Companion.setIncomingStyle
import im.threads.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.styles.permissions.PermissionDescriptionDialogStyle.Companion.getDefaultDialogStyle
import im.threads.styles.permissions.PermissionDescriptionType
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
    pendingIntentCreator: ThreadsLib.PendingIntentCreator,
    unreadMessagesCountListener: ThreadsLib.UnreadMessagesCountListener?,
    networkInterceptor: Interceptor?,
    isDebugLoggingEnabled: Boolean,
    historyLoadingCount: Int,
    surveyCompletionDelay: Int,
    requestConfig: RequestConfig,
    certificateRawResIds: List<Int>?
) : BaseConfig(
    context, serverBaseUrl, datastoreUrl, threadsGateUrl, threadsGateProviderUid, threadsGateHCMProviderUid,
    isNewChatCenterApi, loggerConfig, pendingIntentCreator, unreadMessagesCountListener, networkInterceptor,
    isDebugLoggingEnabled, historyLoadingCount, surveyCompletionDelay, requestConfig, certificateRawResIds
),
    UIConfig {
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
        attachmentEnabled = MetaDataUtils.getAttachmentEnabled(this.context)
        filesAndMediaMenuItemEnabled = MetaDataUtils.getFilesAndMeniaMenuItemEnabled(this.context)
        ThreadsLib.getInstance().uiConfig = this
    }

    override fun getChatStyle(): ChatStyle {
        if (chatStyle == null) {
            synchronized(ChatStyle::class.java) {
                chatStyle = incomingStyle ?: ChatStyle()
            }
        }
        return chatStyle!!
    }

    override fun setChatStyle(style: ChatStyle?) {
        style?.let {
            chatStyle = it
            setIncomingStyle(it)
        }
    }

    override fun getStoragePermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle {
        if (storagePermissionDescriptionDialogStyle == null) {
            storagePermissionDescriptionDialogStyle = getIncomingStyle(PermissionDescriptionType.STORAGE)
                ?: getDefaultDialogStyle(PermissionDescriptionType.STORAGE)
        }
        return storagePermissionDescriptionDialogStyle!!
    }

    override fun setStoragePermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?) {
        style?.let {
            storagePermissionDescriptionDialogStyle = it
            setIncomingStyle(PermissionDescriptionType.STORAGE, it)
        }
    }

    override fun getRecordAudioPermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle {
        if (recordAudioPermissionDescriptionDialogStyle == null) {
            recordAudioPermissionDescriptionDialogStyle = getIncomingStyle(PermissionDescriptionType.RECORD_AUDIO)
                ?: getDefaultDialogStyle(PermissionDescriptionType.RECORD_AUDIO)
        }
        return recordAudioPermissionDescriptionDialogStyle!!
    }

    override fun setRecordAudioPermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?) {
        style?.let {
            recordAudioPermissionDescriptionDialogStyle = it
            setIncomingStyle(PermissionDescriptionType.RECORD_AUDIO, it)
        }
    }

    override fun getCameraPermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle {
        if (cameraPermissionDescriptionDialogStyle == null) {
            cameraPermissionDescriptionDialogStyle = getIncomingStyle(PermissionDescriptionType.CAMERA)
                ?: getDefaultDialogStyle(PermissionDescriptionType.CAMERA)
        }

        return cameraPermissionDescriptionDialogStyle!!
    }

    override fun setCameraPermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?) {
        style?.let {
            cameraPermissionDescriptionDialogStyle = it
            setIncomingStyle(PermissionDescriptionType.CAMERA, it)
        }
    }
}
