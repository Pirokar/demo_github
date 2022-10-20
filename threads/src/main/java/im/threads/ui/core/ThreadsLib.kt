package im.threads.ui.core

import android.content.Context
import im.threads.business.UserInfoBuilder
import im.threads.business.config.BaseConfig
import im.threads.business.core.ThreadsLibBase
import im.threads.business.logger.LoggerEdna
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.ui.ChatStyle
import im.threads.ui.config.Config
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.controllers.ChatController
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.utils.preferences.PreferencesMigrationUi

class ThreadsLib(context: Context) : ThreadsLibBase(context) {
    private val config by lazy {
        Config.getInstance()
    }

    public override fun initUser(userInfoBuilder: UserInfoBuilder) {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val newClientId = preferences.get<String>(PreferencesCoreKeys.TAG_NEW_CLIENT_ID)
        val oldClientId = userInfo?.clientId
        if (!newClientId.isNullOrEmpty() && newClientId != oldClientId) {
            ChatController.getInstance().cleanAll()
        }
        super.initUser(userInfoBuilder)
        ChatController.getInstance().hideEmptyState()
        ChatController.getInstance().loadHistory()
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
            createLibInstance(configBuilder.context)
            Config.setInstance(configBuilder.build())
            BaseConfig.instance = Config.getInstance()
            BaseConfig.instance.loggerConfig?.let { LoggerEdna.init(it) }
            PreferencesMigrationUi(BaseConfig.instance.context).apply {
                migrateMainSharedPreferences()
                migrateUserInfo()
            }

            ThreadsLibBase.init(configBuilder)
        }

        @JvmStatic
        fun getInstance(): ThreadsLib {
            checkNotNull(libInstance) { "ThreadsLib should be initialized first with ThreadsLib.init()" }
            return libInstance as ThreadsLib
        }

        @JvmStatic
        private fun createLibInstance(context: Context) {
            check(libInstance == null) { "ThreadsLib has already been initialized" }
            setLibraryInstance(ThreadsLib(context))
        }
    }
}
