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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File

class ThreadsLib(context: Context) : ThreadsLibBase(context) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val config by lazy {
        Config.getInstance()
    }

    /**
     * Инициализирует пользователя синхронно и загружает его историю в фоновом потоке
     * (изменения истории сообщений применяются в потоке UI)
     * @param userInfoBuilder данные о пользователе
     * @param forceRegistration открывает сокет, отправляет данные о регистрации, закрывает сокет
     */
    public override fun initUser(userInfoBuilder: UserInfoBuilder, forceRegistration: Boolean) {
        super.initUser(userInfoBuilder, forceRegistration)
        ChatController.getInstance().loadHistory(applyUiChanges = false)
    }

    /**
     * Применяет настройки светлой темы
     * @param lightTheme набор параметров для светлой темы. Передайте null, если хотите отключить светлую тему
     */
    fun applyLightTheme(lightTheme: ChatStyle?) {
        config.lightTheme = lightTheme
    }

    /**
     * Применяет настройки темной темы
     * @param darkTheme набор параметров для темной темы. Передайте null, если хотите отключить темную тему
     */
    fun applyDarkTheme(darkTheme: ChatStyle?) {
        config.darkTheme = darkTheme
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
        val fileUri = if (file != null) {
            FileProviderHelper.getUriForFile(
                Config.getInstance().context,
                file
            )
        } else {
            null
        }
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
                    Config.getInstance().context.getString(R.string.ecc_I),
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

        /**
         * Меняет параметры подключения к серверу. Применяются не null параметры
         * @param baseUrl базовый url для основных бэкэнд запросов
         * @param datastoreUrl базовый url для работы с файлами
         * @param threadsGateUrl url вебсокета. Если не null,
         * должен быть не null также и параметр threadsGateProviderUid
         * @param threadsGateProviderUid uid для вебсокета. Если не null,
         * должен быть не null также и параметр threadsGateUrl
         */
        @JvmStatic
        fun changeServerSettings(
            baseUrl: String? = null,
            datastoreUrl: String? = null,
            threadsGateUrl: String? = null,
            threadsGateProviderUid: String? = null
        ) {
            ThreadsLibBase.changeServerSettings(baseUrl, datastoreUrl, threadsGateUrl, threadsGateProviderUid)
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
