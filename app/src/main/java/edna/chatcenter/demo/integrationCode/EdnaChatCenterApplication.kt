package edna.chatcenter.demo.integrationCode

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.perf.FirebasePerformance
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import edna.chatcenter.demo.R
import edna.chatcenter.demo.appCode.business.PreferencesProvider
import edna.chatcenter.demo.appCode.business.ServersProvider
import edna.chatcenter.demo.appCode.business.appModule
import edna.chatcenter.demo.appCode.business.jsonStringToMap
import edna.chatcenter.demo.appCode.fragments.log.LogViewModel
import edna.chatcenter.demo.appCode.models.LogModel
import edna.chatcenter.demo.appCode.models.ServerConfig
import edna.chatcenter.demo.appCode.push.HCMTokenRefresher
import edna.chatcenter.demo.integrationCode.fragments.launch.LaunchFragment
import edna.chatcenter.ui.core.config.ChatAuth
import edna.chatcenter.ui.core.config.ChatUser
import edna.chatcenter.ui.core.config.transport.ChatNetworkConfig
import edna.chatcenter.ui.core.config.transport.ChatSSLCertificate
import edna.chatcenter.ui.core.config.transport.ChatTransportConfig
import edna.chatcenter.ui.core.config.transport.HTTPConfig
import edna.chatcenter.ui.core.config.transport.SSLPinningConfig
import edna.chatcenter.ui.core.config.transport.WSConfig
import edna.chatcenter.ui.core.core.ChatCenterUIListener
import edna.chatcenter.ui.core.logger.ChatLoggerConfig
import edna.chatcenter.ui.core.models.enums.ChatApiVersion
import edna.chatcenter.ui.visual.ChatConfig
import edna.chatcenter.ui.visual.core.ChatCenterUI
import edna.chatcenter.ui.visual.uiStyle.settings.ChatTheme
import edna.chatcenter.ui.visual.uiStyle.settings.components.ChatComponents
import edna.chatcenter.ui.visual.uiStyle.settings.theme.ChatColors
import edna.chatcenter.ui.visual.uiStyle.settings.theme.ChatImages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EdnaChatCenterApplication : Application() {
    private var chatLightTheme: ChatTheme? = null
    private var chatDarkTheme: ChatTheme? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val asyncInit = false

    private val serversProvider: ServersProvider by inject()
    private val preferences: PreferencesProvider by inject()

    var chatCenterUI: ChatCenterUI? = null

    override fun onCreate() {
        super.onCreate()
        startAppCenter()

        startKoin {
            androidContext(this@EdnaChatCenterApplication)
            modules(appModule)
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!edna.chatcenter.demo.BuildConfig.DEBUG)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = !edna.chatcenter.demo.BuildConfig.DEBUG

        initLibrary()
    }

    private fun initLibrary() {
        if (asyncInit) {
            coroutineScope.launch {
                initThemes()
                initChatCenterUI()
                initUser()
                applicationContext.sendBroadcast(
                    Intent(LaunchFragment.APP_INIT_THREADS_LIB_ACTION)
                )
            }
        } else {
            initThemes()
            initChatCenterUI()
            initUser()
        }
        checkAndUpdateTokens()
    }

    private fun checkAndUpdateTokens() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                chatCenterUI?.setFcmToken(token)
            }
        }
        coroutineScope.launch {
            HCMTokenRefresher.requestToken(this@EdnaChatCenterApplication)
        }
    }

    private fun initThemes() {
        // Пример задания тем
        val lightColors = ChatColors(
            main = R.color.light_main,
            searchingProgressLoader = R.color.light_main,
            bodyIconsTint = R.color.light_main,
            incomingText = R.color.black_color,
            incomingTimeText = R.color.light_time_text,
            outgoingTimeText = R.color.light_time_text,
            outgoingText = R.color.black_color,
            incomingBubble = R.color.alt_white,
            outgoingBubble = R.color.light_outgoing_bubble,
            toolbarText = R.color.white_color,
            messageSendingStatus = R.color.light_icons,
            messageSentStatus = R.color.light_icons,
            messageDeliveredStatus = R.color.light_icons,
            messageReadStatus = R.color.light_icons,
            messageFailedStatus = R.color.light_icons,
            incomingLink = R.color.light_links,
            outgoingLink = R.color.light_links,
            toolbar = R.color.light_main,
            statusBar = R.color.light_statusbar,
            menuItem = R.color.light_main
        )
        val darkColors = ChatColors(
            main = R.color.dark_main,
            searchingProgressLoader = R.color.dark_main,
            bodyIconsTint = R.color.dark_main,
            chatBackground = R.color.dark_chat_background,
            incomingText = R.color.dark_messages_text,
            incomingTimeText = R.color.dark_time_text,
            outgoingText = R.color.dark_messages_text,
            incomingBubble = R.color.dark_incoming_bubble,
            outgoingBubble = R.color.dark_outgoing_bubble,
            toolbarText = R.color.white_color,
            outgoingTimeText = R.color.dark_time_text,
            messageSendingStatus = R.color.dark_icons,
            messageSentStatus = R.color.dark_icons,
            messageDeliveredStatus = R.color.dark_icons,
            messageReadStatus = R.color.dark_icons,
            messageFailedStatus = R.color.dark_icons,
            incomingLink = R.color.dark_links,
            outgoingLink = R.color.dark_links,
            statusBar = R.color.alt_threads_chat_status_bar,
            toolbar = R.color.dark_main,
            toolbarContextMenu = R.color.alt_threads_chat_context_menu,
            menuItem = R.color.alt_threads_chat_toolbar_menu_item_black,
            systemMessage = R.color.dark_system_text,
            welcomeScreenTitleText = R.color.dark_system_text,
            welcomeScreenSubtitleText = R.color.dark_system_text,
            chatErrorScreenImageTint = R.color.white_color
        )
        val lightImages = ChatImages(
            backBtn = R.drawable.alt_ic_arrow_back_24dp,
            scrollDownButtonIcon = R.drawable.alt_threads_scroll_down_icon_light
        )
        val darkImages = ChatImages(
            backBtn = R.drawable.alt_ic_arrow_back_24dp,
            scrollDownButtonIcon = R.drawable.alt_threads_scroll_down_icon_black
        )
        val lightChatComponents = ChatComponents(
            applicationContext,
            colors = lightColors,
            images = lightImages
        )

        val darkChatComponents = ChatComponents(
            applicationContext,
            colors = darkColors,
            images = darkImages
        )

        chatDarkTheme = ChatTheme(darkChatComponents)

        /*chatLightTheme = ChatTheme(
            lightChatComponents.apply {
                inputTextComponent.inputMessageColor = R.color.alt_blue
            }
        ) // создайте инстанс, задав основные компоненты. В данном случае цвет текста во всех инпутах изменен*/

        // chatLightTheme = ChatTheme(applicationContext, lightColors, lightImages) // можно передать только нужные ресуры

//        val flows = ChatFlows(ChatComponents(applicationContext)).apply {
//            chatFlow.navigationBar.backButton = IconButtonChatStyle(
//                IconButtonColorStyle(
//                    iconTintColor = R.color.blue_color
//                ),
//                R.drawable.ic_cloud
//            )
//        }
//        chatLightTheme = ChatTheme(flows) // создайте инстанс, переопределив точечно нужные элементы
    }

    fun initChatCenterUI(
        serverConfig: ServerConfig? = null,
        apiVersion: ChatApiVersion = ChatApiVersion.defaultApiVersionEnum
    ) {
        val loggerConfig = ChatLoggerConfig(
            applicationContext,
            logFileSize = 50,
            logInterceptor = object : ChatLoggerConfig.LogInterceptor {
                override fun addLogEvent(logLevel: edna.chatcenter.ui.core.logger.ChatLogLevel, logText: String) {
                    coroutineScope.launch(Dispatchers.IO) {
                        val pattern = "yyyy-MM-dd HH:mm:ss.SSS"
                        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
                        val time = simpleDateFormat.format(Date(System.currentTimeMillis()))
                        LogViewModel.logsData.add(LogModel(time, logLevel, logText))
                    }
                }
            }
        )

        val server = serverConfig ?: try {
            serversProvider.getSelectedServer() ?: serversProvider.readServersFromFile().first()
        } catch (exc: Exception) {
            Toast.makeText(
                applicationContext,
                applicationContext.getString(R.string.no_servers),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val transportConfig = ChatTransportConfig(
            server.serverBaseUrl ?: "",
            server.threadsGateUrl ?: "",
            server.datastoreUrl ?: "",
            hashMapOf(),
            apiVersion = apiVersion
        )
        val certificates = server.trustedSSLCertificates?.map { ChatSSLCertificate(it) }?.toTypedArray() ?: arrayOf()
        val networkConfig = ChatNetworkConfig(
            HTTPConfig(),
            WSConfig(),
            SSLPinningConfig(certificates, server.allowUntrustedSSLCertificate)
        )

        val chatConf = ChatConfig(
            transportConfig,
            networkConfig,
            searchEnabled = true,
            linkPreviewEnabled = true,
            voiceRecordingEnabled = true,
            autoScrollToLatest = true
        ).apply {
            userInputEnabled = server.isInputEnabled
        }

        chatCenterUI = ChatCenterUI(applicationContext, loggerConfig).apply {
            theme = chatLightTheme
            darkTheme = chatDarkTheme
            init(server.threadsGateProviderUid ?: "", server.appMarker ?: "", chatConf)
        }

        chatCenterUI?.setChatCenterUIListener(object : ChatCenterUIListener {
            override fun unreadMessageCountChanged(count: UInt) {
                val intent = Intent(LaunchFragment.APP_UNREAD_COUNT_BROADCAST)
                intent.putExtra(LaunchFragment.UNREAD_COUNT_KEY, count.toInt())
                sendBroadcast(intent)
            }

            override fun urlClicked(url: String) {
                super.urlClicked(url)
                Log.i("UrlClicked", url)
            }
        })
    }

    private fun initUser(callback: (() -> Unit)? = null) {
        val user = preferences.getSelectedUser()
        if (user != null && user.isAllFieldsFilled()) {
            chatCenterUI?.authorize(
                ChatUser(user.userId!!, data = user.userData?.jsonStringToMap()),
                ChatAuth(
                    user.authorizationHeader,
                    user.xAuthSchemaHeader,
                    signature = user.signature
                )
            )
            callback?.invoke()
        }
    }

    private fun startAppCenter() {
        if (edna.chatcenter.demo.BuildConfig.DEBUG.not()) {
            System.getenv("APP_CENTER_KEY")?.let { appCenterKey ->
                AppCenter.start(
                    this,
                    appCenterKey,
                    Analytics::class.java,
                    Crashes::class.java
                )
            }
        }
    }
}

const val ednaMockScheme = "http"
const val ednaMockHost = "localhost"
const val ednaMockPort = 8080
const val ednaMockUrl = "$ednaMockScheme://$ednaMockHost:$ednaMockPort/"
const val ednaMockThreadsGateUrl = "ws://$ednaMockHost:$ednaMockPort/gate/socket"
const val ednaMockThreadsGateProviderUid = "TEST_93jLrtnipZsfbTddRfEfbyfEe5LKKhTl"
const val ednaMockAllowUntrustedSSLCertificate = true
