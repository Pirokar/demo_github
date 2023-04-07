package io.edna.threads.demo.integrationCode

import android.app.Application
import android.content.Context
import im.threads.business.logger.LoggerConfig
import im.threads.business.logger.LoggerRetentionPolicy
import im.threads.business.markdown.MarkdownConfig
import im.threads.ui.ChatStyle
import im.threads.ui.config.ConfigBuilder
import im.threads.ui.core.ThreadsLib
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File

class EdnaThreadsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@EdnaThreadsApplication)
            modules(appModule)
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
            .isDebugLoggingEnabled(true)
            .showAttachmentsButton()
            .enableLogging(loggerConfig)
            .disableSSLPinning() as ConfigBuilder

        getTransportConfig(this).apply {
            configBuilder.serverBaseUrl(baseUrl)
                .datastoreUrl(datastoreUrl)
                .threadsGateUrl(threadsGateUrl)
                .threadsGateProviderUid(threadsGateProviderUid)

            if (isNewChatCenterApi) {
                configBuilder.setNewChatCenterApi()
            }
        }

        ThreadsLib.init(configBuilder)
        ThreadsLib.getInstance().applyChatStyle(getChatTheme())
    }

    private fun getTransportConfig(ctx: Context?): TransportConfig {
        val baseUrl = "https://mobile4.dev.flex.mfms.ru"
        val datastoreUrl = "https://mobile4.dev.flex.mfms.ru"
        val threadsGateUrl = "ws://mobile4.dev.flex.mfms.ru/gate/socket"
        val threadsGateProviderUid = "MOBILE4_HwZ9QhTihb2d8U3I17dBHy1NB9vA9XVkMz65"
        val isNewChatCenterApi = true
        return TransportConfig(
            baseUrl = baseUrl,
            datastoreUrl = datastoreUrl,
            threadsGateUrl = threadsGateUrl,
            threadsGateProviderUid = threadsGateProviderUid,
            isNewChatCenterApi
        )
    }

    private fun getChatTheme(): ChatStyle {
        val chatStyle = ChatStyle()
            .setDefaultFontBold(LATO_BOLD_FONT_PATH)
            .setDefaultFontLight(LATO_LIGHT_FONT_PATH)
            .setDefaultFontRegular(LATO_REGULAR_FONT_PATH)
            .setUseExternalCameraApp(true)
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
            .setConsultSearchingProgressColor(R.color.toolbar_color)
            .setChatBodyIconsTint(R.color.toolbar_color)
            .setChatTitleStyle(
                R.string.demo_alt_threads_contact_center,
                R.string.demo_alt_threads_operator_subtitle,
                R.color.alt_threads_chat_toolbar,
                R.color.alt_threads_chat_context_menu,
                R.color.alt_threads_chat_toolbar_text,
                R.color.alt_threads_chat_status_bar,
                R.bool.alt_threads_chat_is_light_status_bar,
                R.color.alt_threads_chat_toolbar_menu_item,
                R.color.alt_threads_chat_toolbar_hint,
                false
            )
            .setOutgoingMessageBubbleColor(R.color.alt_threads_outgoing_bubble_color)
            .setRecordButtonBackgroundColor(R.color.toolbar_color)
            .setScrollDownButtonIcon(R.drawable.alt_threads_scroll_down_icon)
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
            .setIncomingImageMask(R.drawable.alt_thread_incoming_image_mask)
            .setOutgoingImageMask(R.drawable.alt_thread_outgoing_image_mask)
            .setOutgoingBubbleMask(R.drawable.alt_thread_outgoing_bubble)
            .setIncomingBubbleMask(R.drawable.alt_thread_incoming_bubble)

        return chatStyle
    }
}

private const val LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf"
private const val LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf"
private const val LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf"
