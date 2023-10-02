package im.threads.ui.config

import android.content.Context
import android.util.Size
import im.threads.business.config.BaseConfig
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.models.enums.CurrentUiTheme
import im.threads.business.rest.config.RequestConfig
import im.threads.business.serviceLocator.core.inject
import im.threads.ui.ChatStyle
import im.threads.ui.core.PendingIntentCreator
import im.threads.ui.core.ThreadsLib
import im.threads.ui.extensions.isDarkThemeOn
import im.threads.ui.styles.StyleUseCase
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle.Companion.getDefaultDialogStyle
import im.threads.ui.styles.permissions.PermissionDescriptionType
import im.threads.ui.utils.MetadataUi
import okhttp3.Interceptor

class Config(
    context: Context,
    serverBaseUrl: String?,
    datastoreUrl: String?,
    threadsGateUrl: String?,
    threadsGateProviderUid: String?,
    isNewChatCenterApi: Boolean,
    loggerConfig: LoggerConfig?,
    val pendingIntentCreator: PendingIntentCreator,
    unreadMessagesCountListener: UnreadMessagesCountListener?,
    networkInterceptor: Interceptor?,
    var lightTheme: ChatStyle?,
    var darkTheme: ChatStyle?,
    isDebugLoggingEnabled: Boolean,
    historyLoadingCount: Int,
    surveyCompletionDelay: Int,
    requestConfig: RequestConfig,
    trustedSSLCertificate: List<Int>?,
    allowUntrustedSSLCertificate: Boolean,
    keepSocketActive: Boolean,
    notificationImportance: Int,
    private val isAttachmentsEnabled: Boolean?
) : BaseConfig(
    context, serverBaseUrl, datastoreUrl, threadsGateUrl, threadsGateProviderUid,
    isNewChatCenterApi, loggerConfig, unreadMessagesCountListener, networkInterceptor, isDebugLoggingEnabled,
    historyLoadingCount, surveyCompletionDelay, requestConfig, notificationImportance, trustedSSLCertificate,
    allowUntrustedSSLCertificate, keepSocketActive
) {
    val chatStyle: ChatStyle
        get() {
            if (lightTheme == null && darkTheme == null) {
                synchronized(ChatStyle::class.java) {
                    styleUseCase.incomingStyle.apply {
                        lightTheme = first
                        darkTheme = second
                    }
                    if (lightTheme == null && darkTheme == null) {
                        lightTheme = ChatStyle()
                    }
                }
            }

            return when (ThreadsLib.getInstance().currentUiTheme) {
                CurrentUiTheme.LIGHT -> lightTheme!!
                CurrentUiTheme.DARK -> darkTheme ?: lightTheme!!
                CurrentUiTheme.SYSTEM -> {
                    if (context.isDarkThemeOn()) {
                        darkTheme ?: lightTheme!!
                    } else {
                        lightTheme!!
                    }
                }
            }
        }

    @Volatile
    private var storagePermissionDescriptionDialogStyle: PermissionDescriptionDialogStyle? = null

    @Volatile
    private var recordAudioPermissionDescriptionDialogStyle: PermissionDescriptionDialogStyle? = null

    @Volatile
    private var cameraPermissionDescriptionDialogStyle: PermissionDescriptionDialogStyle? = null

    private val styleUseCase: StyleUseCase by inject()

    var filesAndMediaMenuItemEnabled = false

    /**
     * Представляет ширину и высоту экрана в пикселях
     */
    internal var screenSize = Size(0, 0)

    init {
        filesAndMediaMenuItemEnabled = MetadataUi.getFilesAndMediaMenuItemEnabled(this.context)
            ?: context.resources.getBoolean(chatStyle.filesAndMediaItemEnabled)
        lightTheme?.let { styleUseCase.setIncomingLightStyle(it) }
        darkTheme?.let { styleUseCase.setIncomingDarkStyle(it) }
    }

    fun getStoragePermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle {
        if (storagePermissionDescriptionDialogStyle == null) {
            storagePermissionDescriptionDialogStyle = styleUseCase.getIncomingStyle(PermissionDescriptionType.STORAGE)
                ?: getDefaultDialogStyle(PermissionDescriptionType.STORAGE)
        }
        return storagePermissionDescriptionDialogStyle!!
    }

    fun setStoragePermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?) {
        style?.let {
            storagePermissionDescriptionDialogStyle = it
            styleUseCase.setIncomingStyle(PermissionDescriptionType.STORAGE, it)
        }
    }

    fun getRecordAudioPermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle {
        if (recordAudioPermissionDescriptionDialogStyle == null) {
            recordAudioPermissionDescriptionDialogStyle = styleUseCase.getIncomingStyle(PermissionDescriptionType.RECORD_AUDIO)
                ?: getDefaultDialogStyle(PermissionDescriptionType.RECORD_AUDIO)
        }
        return recordAudioPermissionDescriptionDialogStyle!!
    }

    fun setRecordAudioPermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?) {
        style?.let {
            recordAudioPermissionDescriptionDialogStyle = it
            styleUseCase.setIncomingStyle(PermissionDescriptionType.RECORD_AUDIO, it)
        }
    }

    fun getCameraPermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle {
        if (cameraPermissionDescriptionDialogStyle == null) {
            cameraPermissionDescriptionDialogStyle = styleUseCase.getIncomingStyle(PermissionDescriptionType.CAMERA)
                ?: getDefaultDialogStyle(PermissionDescriptionType.CAMERA)
        }

        return cameraPermissionDescriptionDialogStyle!!
    }

    fun setCameraPermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?) {
        style?.let {
            cameraPermissionDescriptionDialogStyle = it
            styleUseCase.setIncomingStyle(PermissionDescriptionType.CAMERA, it)
        }
    }

    fun getIsAttachmentsEnabled(): Boolean {
        return isAttachmentsEnabled ?: MetadataUi.getAttachmentEnabled(context) ?: false
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

        @JvmStatic
        fun isInstanceNull(): Boolean {
            return instance == null
        }

        fun setInstance(config: Config) {
            instance = config
            BaseConfig.setInstance(config)
        }
    }
}
