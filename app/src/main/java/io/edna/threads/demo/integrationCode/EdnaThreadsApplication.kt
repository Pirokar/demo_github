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

        val colors = ChatColors(
            main = R.color.alt_green,
            chatBackground = R.color.alt_threads_attachments_background,
            title = R.color.alt_black,
            incomingText = R.color.alt_green,
            incomingTimeText = R.color.alt_green,
            outgoingText = R.color.alt_white,
            incomingBubble = R.color.alt_white,
            outgoingBubble = R.color.alt_green,
            toolbarText = R.color.white_color
        )
        val images = ChatImages(
            backBtn = R.drawable.alt_ic_arrow_back_24dp
        )
        val chatTheme = ChatTheme(this, colors, images).apply {
            components.inputTextField = components.inputTextField.copy(
                textColor = R.color.alt_blue,
                hintColor = R.color.alt_blue_transparent
            )
            /*flows.chatFlow.navigationBar.backButton = IconButtonChatComponent(
                this@ThreadsDemoApplication,
                colors,
                images
            )[IconButtonEnum.REPLY]*/
        }

        val loggerConfig = LoggerConfig.Builder(this)
            .logToFile()
            .dir(File(this.filesDir, "logs"))
            .retentionPolicy(LoggerRetentionPolicy.TOTAL_SIZE)
            .maxTotalSize(5242880)
            .build()

        val configBuilder = ConfigBuilder(this)
            .surveyCompletionDelay(2000)
            .historyLoadingCount(50)
            .applyLightTheme(chatTheme)
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

    private fun getLightChatTheme() = getMainChatTheme().apply {
        setConsultSearchingProgressColor(R.color.light_toolbar)
        setChatBodyIconsTint(R.color.light_toolbar)
        setChatTitleStyle(
            R.string.app_name,
            R.string.demo_alt_threads_operator_subtitle,
            R.color.light_toolbar,
            R.color.alt_threads_chat_context_menu,
            R.color.alt_threads_chat_toolbar_text,
            R.color.light_statusbar,
            R.bool.alt_threads_chat_is_light_status_bar,
            R.color.light_toolbar,
            R.color.alt_threads_chat_toolbar_hint,
            true
        )
        setOutgoingMessageBubbleColor(R.color.light_outgoing_bubble)
        setScrollDownButtonIcon(R.drawable.alt_threads_scroll_down_icon_light)
        setRecordButtonBackgroundColor(R.color.light_toolbar)
        setOutgoingMessageTextColor(R.color.black_color)
        setOutgoingImageTimeBackgroundColor(R.color.light_outgoing_image_time_background)
        setOutgoingMessageTimeColor(R.color.light_outgoing_time_text)
        setMessageSendingResources(null, R.color.light_icons)
        setMessageSentResources(null, R.color.light_icons)
        setMessageDeliveredResources(null, R.color.light_icons)
        setMessageReadResources(null, R.color.light_icons)
        setMessageFailedResources(null, R.color.light_icons)
        setChatHighlightingColor(R.color.light_highlighting)
        setIncomingMessageLinkColor(R.color.light_links)
        setOutgoingMessageLinkColor(R.color.light_links)
    }

    private fun getDarkChatTheme() = getMainChatTheme().apply {
        setConsultSearchingProgressColor(R.color.dark_toolbar)
        setChatBodyIconsTint(R.color.dark_toolbar)
        setChatTitleStyle(
            R.string.demo_alt_threads_contact_center,
            R.string.demo_alt_threads_operator_subtitle,
            R.color.dark_toolbar,
            R.color.alt_threads_chat_context_menu,
            R.color.alt_threads_chat_toolbar_text,
            R.color.alt_threads_chat_status_bar,
            R.bool.alt_threads_chat_is_light_status_bar,
            R.color.alt_threads_chat_toolbar_menu_item_black,
            R.color.alt_threads_chat_toolbar_hint,
            true
        )
        setOutgoingMessageBubbleColor(R.color.dark_outgoing_bubble)
        setScrollDownButtonIcon(R.drawable.alt_threads_scroll_down_icon_black)
        setRecordButtonBackgroundColor(R.color.dark_toolbar)
        setOutgoingMessageTextColor(R.color.white_color_fa)
        setOutgoingImageTimeBackgroundColor(R.color.dark_outgoing_image_time_background)
        setOutgoingMessageTimeColor(R.color.dark_outgoing_time_text)
        setMessageSendingResources(null, R.color.dark_icons)
        setMessageSentResources(null, R.color.dark_icons)
        setMessageDeliveredResources(null, R.color.dark_icons)
        setMessageReadResources(null, R.color.dark_icons)
        setMessageFailedResources(null, R.color.dark_icons)
        setChatHighlightingColor(R.color.dark_highlighting)
        setIncomingMessageLinkColor(R.color.dark_links)
        setOutgoingMessageLinkColor(R.color.dark_links)
        setChatBackgroundColor(R.color.dark_chat_background)
        setSystemMessageStyle(
            null,
            null,
            R.color.dark_system_text,
            null,
            null,
            R.color.dark_links
        )
        setSurveyStyle(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            R.color.dark_system_text,
            R.color.dark_system_text
        )
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
