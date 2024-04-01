package io.edna.threads.demo.appCode

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import im.threads.business.config.ChatAuth
import im.threads.business.config.ChatUser
import im.threads.business.config.transport.ChatNetworkConfig
import im.threads.business.config.transport.ChatSSLCertificate
import im.threads.business.config.transport.ChatTransportConfig
import im.threads.business.config.transport.HTTPConfig
import im.threads.business.config.transport.SSLPinningConfig
import im.threads.business.config.transport.WSConfig
import im.threads.business.core.ChatCenterUIListener
import im.threads.business.extensions.jsonStringToMap
import im.threads.business.logger.ChatLoggerConfig
import im.threads.business.models.enums.ChatApiVersion
import im.threads.ui.ChatConfig
import im.threads.ui.core.ChatCenterUI
import im.threads.ui.uiStyle.settings.ChatTheme
import im.threads.ui.uiStyle.settings.theme.ChatColors
import im.threads.ui.uiStyle.settings.theme.ChatImages
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.ServersProvider
import io.edna.threads.demo.appCode.business.appModule
import io.edna.threads.demo.appCode.models.ServerConfig
import io.edna.threads.demo.appCode.push.HCMTokenRefresher
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EdnaThreadsApplication : Application() {
    private lateinit var chatLightTheme: ChatTheme
    private lateinit var chatDarkTheme: ChatTheme
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val asyncInit = false

    private val serversProvider: ServersProvider by inject()
    private val preferences: PreferencesProvider by inject()

    var chatCenterUI: ChatCenterUI? = null

    override fun onCreate() {
        super.onCreate()
        startAppCenter()

        startKoin {
            androidContext(this@EdnaThreadsApplication)
            modules(appModule)
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = !BuildConfig.DEBUG

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
    }

    private fun initThemes() {
        // Example of the new config
        chatLightTheme = ChatTheme(
            applicationContext,
            colors = ChatColors(
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
            ),
            images = ChatImages(
                backBtn = R.drawable.alt_ic_arrow_back_24dp,
                scrollDownButtonIcon = R.drawable.alt_threads_scroll_down_icon_light
            )
        ).apply {
            /*
            components.inputTextField = components.inputTextField.copy(
                textColor = R.color.alt_blue,
                hintColor = R.color.alt_blue_transparent
            )
            flows.chatFlow.navigationBar.backButton = IconButtonChatComponent(
                this@ThreadsDemoApplication,
                colors,
                images
            )[IconButtonEnum.REPLY]*/
        }

        chatDarkTheme = ChatTheme(
            applicationContext,
            colors = ChatColors(
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
            ),
            images = ChatImages(
                backBtn = R.drawable.alt_ic_arrow_back_24dp,
                scrollDownButtonIcon = R.drawable.alt_threads_scroll_down_icon_black
            )
        )
    }

    fun initChatCenterUI(
        serverConfig: ServerConfig? = null,
        apiVersion: ChatApiVersion = ChatApiVersion.defaultApiVersionEnum
    ) {
        val loggerConfig = ChatLoggerConfig(
            applicationContext,
            logFileSize = 50
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
            chatSubtitleEnabled = true,
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

        HCMTokenRefresher.requestToken(this)
    }

    private fun initUser() {
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
        }
    }

    private fun startAppCenter() {
        if (BuildConfig.DEBUG.not()) {
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
