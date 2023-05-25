package io.edna.threads.demo.integrationCode

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.LoggerRetentionPolicy
import im.threads.business.markdown.MarkdownConfig
import im.threads.ui.ChatStyle
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import im.threads.ui.newInterface.settings.ChatTheme
import im.threads.ui.newInterface.settings.theme.ChatColors
import im.threads.ui.newInterface.settings.theme.ChatImages
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
                main = R.color.alt_green,
                consultSearchingProgress = R.color.alt_green,
                bodyIconsTint = R.color.alt_green,
                title = R.color.alt_threads_chat_toolbar_text,
                incomingText = R.color.alt_green,
                incomingTimeText = R.color.alt_green,
                outgoingText = R.color.alt_white,
                incomingBubble = R.color.alt_white,
                outgoingBubble = R.color.alt_green,
                toolbarText = R.color.white_color,
                voiceBtnBackgroundColor = R.color.alt_green,
                outgoingImageTimeBackgroundColor = R.color.light_outgoing_time_text,
                messageSendingStatus = R.color.alt_white,
                messageSentStatus = R.color.alt_white,
                messageDeliveredStatus = R.color.alt_white,
                messageReadStatus = R.color.alt_white,
                messageFailedStatus = R.color.alt_white,
                messageHighlightingColor = R.color.light_highlighting,
                incomingLink = R.color.light_links,
                outgoingLink = R.color.light_links,
                toolbar = R.color.alt_green,
                statusBar = R.color.light_statusbar,
                menuItem = R.color.alt_green
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
                main = R.color.alt_green,
                consultSearchingProgress = R.color.dark_toolbar,
                bodyIconsTint = R.color.dark_toolbar,
                chatBackground = R.color.dark_chat_background,
                title = R.color.alt_threads_chat_toolbar_text,
                incomingText = R.color.alt_green,
                incomingTimeText = R.color.alt_green,
                outgoingText = R.color.white_color_fa,
                incomingBubble = R.color.alt_white,
                outgoingBubble = R.color.dark_outgoing_bubble,
                toolbarText = R.color.white_color,
                voiceBtnBackgroundColor = R.color.dark_toolbar,
                outgoingImageTimeBackgroundColor = R.color.dark_outgoing_image_time_background,
                outgoingTimeText = R.color.dark_outgoing_time_text,
                messageSendingStatus = R.color.dark_icons,
                messageSentStatus = R.color.dark_icons,
                messageDeliveredStatus = R.color.dark_icons,
                messageReadStatus = R.color.dark_icons,
                messageFailedStatus = R.color.dark_icons,
                messageHighlightingColor = R.color.dark_highlighting,
                incomingLink = R.color.dark_links,
                outgoingLink = R.color.dark_links,
                statusBar = R.color.alt_threads_chat_status_bar,
                toolbarContextMenu = R.color.alt_threads_chat_context_menu,
                menuItem = R.color.alt_threads_chat_toolbar_menu_item_black,
                searchHintColor = R.color.alt_threads_chat_toolbar_hint,
                systemMessage = R.color.dark_system_text
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
            .applyChatStyle(getMainChatTheme())
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

    private fun getMainChatTheme(): ChatStyle {
        val chatStyle = ChatStyle()
            .setScrollChatToEndIfUserTyping(false)

        val markdownConfig = MarkdownConfig()
        markdownConfig.isLinkUnderlined = true
        chatStyle
            .setChatSubtitleShowConsultOrgUnit(true)
            .setIncomingMarkdownConfiguration(markdownConfig)
            .setOutgoingMarkdownConfiguration(markdownConfig)
            .setVisibleChatTitleShadow(R.bool.alt_threads_chat_title_shadow_is_visible)
            .setShowConsultSearching(true)
            .setVoiceMessageEnabled(true)
            .showChatBackButton(true)
            .setIngoingPadding(
                R.dimen.alt_greenBubbleIncomingPaddingLeft,
                R.dimen.alt_greenBubbleIncomingPaddingTop,
                R.dimen.alt_greenBubbleIncomingPaddingRight,
                R.dimen.alt_greenBubbleIncomingPaddingBottom
            )
            .setIncomingImageBordersSize(
                R.dimen.alt_incomingImageLeftBorderSize,
                R.dimen.alt_incomingImageTopBorderSize,
                R.dimen.alt_incomingImageRightBorderSize,
                R.dimen.alt_incomingImageBottomBorderSize
            )
            .setOutgoingImageBordersSize(
                R.dimen.alt_outgoingImageLeftBorderSize,
                R.dimen.alt_outgoingImageTopBorderSize,
                R.dimen.alt_outgoingImageRightBorderSize,
                R.dimen.alt_outgoingImageBottomBorderSize
            )

        return chatStyle
    }
}

private const val LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf"
private const val LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf"
private const val LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf"
