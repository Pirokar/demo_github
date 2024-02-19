package io.edna.threads.demo.appCode

import android.app.Application
import android.content.Intent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import im.threads.business.UserInfoBuilder
import im.threads.business.core.UnreadMessagesCountListener
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.LoggerRetentionPolicy
import im.threads.business.markdown.MarkdownConfig
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ChatCenterUI
import im.threads.ui.core.ThreadsLib
import im.threads.ui.uiStyle.settings.ChatSettings
import im.threads.ui.uiStyle.settings.ChatTheme
import im.threads.ui.uiStyle.settings.theme.ChatColors
import im.threads.ui.uiStyle.settings.theme.ChatImages
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.ServersProvider
import io.edna.threads.demo.appCode.business.appModule
import io.edna.threads.demo.appCode.push.HCMTokenRefresher
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class EdnaThreadsApplication : Application() {
    private var chatLightTheme: ChatTheme? = null
    private var chatDarkTheme: ChatTheme? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val asyncInit = false

    private val serversProvider: ServersProvider by inject()
    private val preferences: PreferencesProvider by inject()

    override fun onCreate() {
        super.onCreate()
        val lib = ChatCenterUI()

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
                initThreadsLib()
                initUser()
                applicationContext.sendBroadcast(
                    Intent(LaunchFragment.APP_INIT_THREADS_LIB_ACTION)
                )
            }
        } else {
            initThemes()
            initThreadsLib()
            initUser()
        }
    }

    private fun initThemes() {
        // Example of the new config
        chatLightTheme = ChatTheme(
            applicationContext,
            colors = ChatColors(
                main = R.color.light_main,
                consultSearchingProgress = R.color.light_main,
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
                consultSearchingProgress = R.color.dark_main,
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

    private fun initThreadsLib() {
        val loggerConfig = LoggerConfig.Builder(applicationContext)
            .logToFile()
            .dir(File(applicationContext.filesDir, "logs"))
            .retentionPolicy(LoggerRetentionPolicy.TOTAL_SIZE)
            .maxTotalSize(5242880)
            .build()

        val chatConfig = ConfigBuilder(applicationContext)
            .unreadMessagesCountListener(object : UnreadMessagesCountListener {
                override fun onUnreadMessagesCountChanged(count: Int) {
                    val intent = Intent(LaunchFragment.APP_UNREAD_COUNT_BROADCAST)
                    intent.putExtra(LaunchFragment.UNREAD_COUNT_KEY, count)
                    applicationContext.sendBroadcast(intent)
                }
            })
            .historyLoadingCount(50)
            .applyLightTheme(chatLightTheme)
            .applyDarkTheme(chatDarkTheme)
            .applyChatSettings(getMainChatTheme())
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .enableLogging(loggerConfig)

        serversProvider.getSelectedServer()?.let { server ->
            chatConfig.serverBaseUrl(server.serverBaseUrl)
            chatConfig.datastoreUrl(server.datastoreUrl)
            chatConfig.threadsGateUrl(server.threadsGateUrl)
            chatConfig.threadsGateProviderUid(server.threadsGateProviderUid)
            chatConfig.trustedSSLCertificates(server.trustedSSLCertificates)
            chatConfig.setNewChatCenterApi()

            if (server.allowUntrustedSSLCertificate) {
                chatConfig.allowUntrustedSSLCertificates()
            }
        }
        ThreadsLib.init(chatConfig)
        HCMTokenRefresher.requestToken(this)
    }

    private fun initUser() {
        val user = preferences.getSelectedUser()
        if (user != null && user.isAllFieldsFilled()) {
            ThreadsLib.getInstance().initUser(
                UserInfoBuilder(user.userId!!)
                    .setAuthData(user.authorizationHeader, user.xAuthSchemaHeader)
                    .setClientData(user.userData)
                    .setClientIdSignature(user.signature)
                    .setAppMarker(user.appMarker),
                false
            )
        }
    }

    private fun getMainChatTheme(): ChatSettings {
        val chatSettings = ChatSettings()

        val markdownConfig = MarkdownConfig()
        markdownConfig.isLinkUnderlined = true
        chatSettings
            .enableLinkPreview()
            .enableChatSubtitleShowConsultOrgUnit()
            .setIncomingMarkdownConfiguration(markdownConfig)
            .setOutgoingMarkdownConfiguration(markdownConfig)
            .enableVoiceMessages()
            .showChatBackButton()
            .enableLinkPreview()
        return chatSettings
    }
}

const val ednaMockScheme = "http"
const val ednaMockHost = "localhost"
const val ednaMockPort = 8080
const val ednaMockUrl = "$ednaMockScheme://$ednaMockHost:$ednaMockPort/"
const val ednaMockThreadsGateUrl = "ws://$ednaMockHost:$ednaMockPort/gate/socket"
const val ednaMockThreadsGateProviderUid = "TEST_93jLrtnipZsfbTddRfEfbyfEe5LKKhTl"
const val ednaMockAllowUntrustedSSLCertificate = true
