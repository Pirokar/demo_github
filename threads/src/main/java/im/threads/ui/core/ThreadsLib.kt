package im.threads.ui.core

import android.content.Context
import android.net.Uri
import im.threads.R
import im.threads.business.UserInfoBuilder
import im.threads.business.config.BaseConfig
import im.threads.business.core.ThreadsLibBase
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.FileDescription
import im.threads.business.models.UpcomingUserMessage
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.utils.FileProviderHelper
import im.threads.business.utils.FileUtils.getFileSize
import im.threads.ui.ChatStyle
import im.threads.ui.config.Config
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.controllers.ChatController
import im.threads.ui.styles.permissions.PermissionDescriptionDialogStyle
import im.threads.ui.utils.preferences.PreferencesMigrationUi
import java.io.File

class ThreadsLib(context: Context) : ThreadsLibBase(context) {
    private val config by lazy {
        Config.getInstance()
    }

    public override fun initUser(userInfoBuilder: UserInfoBuilder) {
        val userInfo = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)
        val oldClientId = userInfo?.clientId
        val newClientId = userInfoBuilder.clientId
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

    /**
     * Used to post messages to chat as if written by client
     *
     * @return true, if message was successfully added to messaging queue, otherwise false
     */
    fun sendMessage(message: String?, file: File?): Boolean {
        val fileUri = if (file != null) FileProviderHelper.getUriForFile(
            Config.getInstance().context,
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
        val clientId = preferences.get<UserInfoBuilder>(PreferencesCoreKeys.USER_INFO)?.clientId
        return if (!clientId.isNullOrBlank()) {
            var fileDescription: FileDescription? = null
            if (fileUri != null) {
                fileDescription = FileDescription(
                    Config.getInstance().context.getString(R.string.threads_I),
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
            LoggerEdna.info(javaClass.simpleName, "You might need to initialize user first with ThreadsLib.userInfo()")
            false
        }
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
