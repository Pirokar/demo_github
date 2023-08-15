package im.threads.ui.core

import android.content.Context
import android.net.Uri
import im.threads.R
import im.threads.business.config.BaseConfig
import im.threads.business.core.ThreadsLibBase
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.FileDescription
import im.threads.business.models.UpcomingUserMessage
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.FileProvider
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
    private val config by lazy { Config.getInstance() }
    private val clientUseCase: ClientUseCase by inject()
    private val fileProvider: FileProvider by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Применяет настройки светлой темы
     * @param lightTheme набор параметров для светлой темы. Передайте null, если хотите отключить светлую тему
     */
    fun applyLightTheme(lightTheme: ChatStyle?) {
        config.lightTheme = lightTheme
        LoggerEdna.info("Applied light theme. $lightTheme")
    }

    /**
     * Применяет настройки темной темы
     * @param darkTheme набор параметров для темной темы. Передайте null, если хотите отключить темную тему
     */
    fun applyDarkTheme(darkTheme: ChatStyle?) {
        config.darkTheme = darkTheme
        LoggerEdna.info("Applied dark theme. $darkTheme")
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
            fileProvider.getUriForFile(
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
        val clientId = clientUseCase.getUserInfo()?.clientId
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
            BaseConfig.getInstance().loggerConfig?.let { LoggerEdna.init(it) }
            LoggerEdna.info(configBuilder.toString())
            PreferencesMigrationUi(BaseConfig.getInstance().context).apply {
                removeStyleFromPreferences()
                migrateMainSharedPreferences()
                migrateUserInfo()
            }

            initBaseParams()
        }

        /**
         * Меняет параметры подключения к серверу. Применяются не null параметры
         * @param baseUrl базовый url для основных бэкэнд запросов
         * @param datastoreUrl базовый url для работы с файлами
         * @param threadsGateUrl url вебсокета. Если не null,
         * должен быть не null также и параметр threadsGateProviderUid
         * @param threadsGateProviderUid uid для вебсокета. Если не null,
         * должен быть не null также и параметр threadsGateUrl
         * @param trustedSSLCertificates список id сертификатов
         */
        @JvmStatic
        fun changeServerSettings(
            baseUrl: String? = null,
            datastoreUrl: String? = null,
            threadsGateUrl: String? = null,
            threadsGateProviderUid: String? = null,
            trustedSSLCertificates: List<Int>?,
            allowUntrustedSSLCertificate: Boolean
        ) {
            ThreadsLibBase.changeServerSettings(
                baseUrl,
                datastoreUrl,
                threadsGateUrl,
                threadsGateProviderUid,
                trustedSSLCertificates,
                allowUntrustedSSLCertificate
            )
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

        @JvmStatic
        fun isInitialized(): Boolean {
            return libInstance != null
        }
    }
}
