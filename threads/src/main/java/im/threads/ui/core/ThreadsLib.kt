package im.threads.ui.core

import im.threads.ChatStyle
import im.threads.business.config.BaseConfig
import im.threads.business.core.ThreadsLibBase
import im.threads.business.logger.LoggerEdna
import im.threads.ui.config.Config
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.utils.preferences.PreferencesMigrationUi

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
        fun init(configBuilder: ConfigBuilder) {
            BaseConfig.instance = configBuilder.build()
            createInstance()
            BaseConfig.instance.loggerConfig?.let { LoggerEdna.init(it) }
            PreferencesMigrationUi().migrateMainSharedPreferences()

            ThreadsLibBase.init(configBuilder)
        }

        @JvmStatic
        fun getInstance(): ThreadsLib {
            checkNotNull(libInstance) { "ThreadsLib should be initialized first with ThreadsLib.init()" }
            return libInstance as? ThreadsLib ?: ThreadsLib()
        }

        @JvmStatic
        private fun createInstance() {
            check(libInstance == null) { "ThreadsLib has already been initialized" }
            setLibraryInstance(ThreadsLib())
        }
    }
}
