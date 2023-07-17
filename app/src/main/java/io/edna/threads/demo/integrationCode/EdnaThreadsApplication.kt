package io.edna.threads.demo.integrationCode

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.LoggerRetentionPolicy
import im.threads.business.markdown.MarkdownConfig
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import im.threads.ui.uiStyle.settings.ChatSettings
import im.threads.ui.uiStyle.settings.ChatTheme
import im.threads.ui.uiStyle.settings.theme.ChatColors
import im.threads.ui.uiStyle.settings.theme.ChatImages
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.ServersProvider
import io.edna.threads.demo.appCode.business.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class EdnaThreadsApplication : Application() {
    private val serversProvider: ServersProvider by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@EdnaThreadsApplication)
            modules(appModule)
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        // Example of the new config
        val chatLightTheme = ChatTheme(
            this,
            colors = ChatColors(
                main = R.color.light_main,
                consultSearchingProgress = R.color.light_main,
                bodyIconsTint = R.color.light_main,
                title = R.color.alt_threads_chat_toolbar_text,
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

        val chatDarkTheme = ChatTheme(
            this,
            colors = ChatColors(
                main = R.color.dark_main,
                consultSearchingProgress = R.color.dark_main,
                bodyIconsTint = R.color.dark_main,
                chatBackground = R.color.dark_chat_background,
                title = R.color.alt_threads_chat_toolbar_text,
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

        val loggerConfig = LoggerConfig.Builder(this)
            .logToFile()
            .dir(File(this.filesDir, "logs"))
            .retentionPolicy(LoggerRetentionPolicy.TOTAL_SIZE)
            .maxTotalSize(5242880)
            .build()

        val configBuilder = ConfigBuilder(this)
            .surveyCompletionDelay(2000)
            .historyLoadingCount(50)
            .applyLightTheme(chatLightTheme)
            .applyDarkTheme(chatDarkTheme)
            .applyChatSettings(getMainChatTheme())
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .enableLogging(loggerConfig)

        serversProvider.getSelectedServer()?.let { server ->
            configBuilder.serverBaseUrl(server.serverBaseUrl)
            configBuilder.datastoreUrl(server.datastoreUrl)
            configBuilder.threadsGateUrl(server.threadsGateUrl)
            configBuilder.threadsGateProviderUid(server.threadsGateProviderUid)
            configBuilder.trustedSSLCertificates(server.trustedSSLCertificates)
            configBuilder.allowUntrustedSSLCertificates(server.allowUntrustedSSLCertificate)
            configBuilder.setNewChatCenterApi()
        }

        ThreadsLib.init(configBuilder)
    }

    private fun getMainChatTheme(): ChatSettings {
        val chatSettings = ChatSettings()
            .setScrollChatToEndIfUserTyping(false)

        val markdownConfig = MarkdownConfig()
        markdownConfig.isLinkUnderlined = true
        chatSettings
            .setChatSubtitleShowConsultOrgUnit(true)
            .setIncomingMarkdownConfiguration(markdownConfig)
            .setOutgoingMarkdownConfiguration(markdownConfig)
            .setVisibleChatTitleShadow(R.bool.alt_threads_chat_title_shadow_is_visible)
            .setShowConsultSearching(true)
            .setVoiceMessageEnabled(true)
            .showChatBackButton(true)
        return chatSettings
    }
}

private const val LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf"
private const val LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf"
private const val LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf"
