package im.threads.ui.core

import im.threads.ChatStyle
import im.threads.business.config.BaseConfig
import im.threads.business.config.BaseConfigBuilder
import im.threads.business.core.ThreadsLibBase
import im.threads.ui.config.Config
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle

class ThreadsLib : ThreadsLibBase() {
    private val config by lazy {
        BaseConfig.instance as Config
    }
    fun applyChatStyle(chatStyle: ChatStyle?) {
        config.setChatStyle(chatStyle)
    }

    fun applyStoragePermissionDescriptionDialogStyle(
        dialogStyle: PermissionDescriptionDialogStyle
    ) {
        config.setStoragePermissionDescriptionDialogStyle(dialogStyle)
    }

    fun applyRecordAudioPermissionDescriptionDialogStyle(
        dialogStyle: PermissionDescriptionDialogStyle
    ) {
        config.setRecordAudioPermissionDescriptionDialogStyle(dialogStyle)
    }

    fun applyCameraPermissionDescriptionDialogStyle(
        dialogStyle: PermissionDescriptionDialogStyle
    ) {
        config.setCameraPermissionDescriptionDialogStyle(dialogStyle)
    }

    companion object {
        @JvmStatic
        fun getLibVersion() = ThreadsLibBase.getLibVersion()

        @JvmStatic
        fun init(configBuilder: BaseConfigBuilder) = ThreadsLibBase.init(configBuilder)

        @JvmStatic
        fun getInstance(): ThreadsLib {
            checkNotNull(libInstance) { "ThreadsLib should be initialized first with ThreadsLib.init()" }
            return libInstance as? ThreadsLib ?: ThreadsLib()
        }
    }
}
