package im.threads.internal.markdown

import android.content.Context
import android.text.Spanned
import im.threads.internal.Config
import im.threads.internal.utils.UrlUtils
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.linkify.LinkifyPlugin

// Constructor for test purposes
class MarkwonMarkdownProcessor(
    nullableContext: Context? = null,
    incomingMarkdownConfig: MarkdownConfig? = null,
    outgoingMarkdownConfig: MarkdownConfig? = null
) : MarkdownProcessor {
    private val context: Context by lazy { nullableContext ?: Config.instance.context }
    private val incomingMarkdownConfiguration: MarkdownConfig by lazy {
        incomingMarkdownConfig ?: Config.instance.chatStyle.incomingMarkdownConfiguration
    }
    private val outgoingMarkdownConfiguration: MarkdownConfig by lazy {
        outgoingMarkdownConfig ?: Config.instance.chatStyle.outgoingMarkdownConfiguration
    }

    private val incomingProcessor: Markwon by lazy {
        configureParser(incomingMarkdownConfiguration)
    }

    private val outgoingProcessor: Markwon by lazy {
        configureParser(outgoingMarkdownConfiguration)
    }

    override fun parseClientMessage(text: String): Spanned {
        return outgoingProcessor.toMarkdown(text)
    }

    override fun parseOperatorMessage(text: String): Spanned {
        return incomingProcessor.toMarkdown(text)
    }

    private fun configureParser(markdownConfig: MarkdownConfig): Markwon {
        return Markwon.builder(context)
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    configureMessagesView(builder, markdownConfig)
                }

                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    super.configureConfiguration(builder)
                    configureLinks(builder)
                }
            })
            .build()
    }

    private fun configureMessagesView(builder: MarkwonTheme.Builder, config: MarkdownConfig) {
        config.linkColor?.let { builder.linkColor(it) }
        builder.isLinkUnderlined(config.isLinkUnderlined)
        config.blockMargin?.let { builder.blockMargin(it) }
        config.blockQuoteWidth?.let { builder.blockQuoteWidth(it) }
        config.blockQuoteColor?.let { builder.blockQuoteColor(it) }
        config.listItemColor?.let { builder.listItemColor(it) }
        config.bulletListItemStrokeWidth?.let { builder.bulletListItemStrokeWidth(it) }
        config.bulletWidth?.let { builder.bulletWidth(it) }
        config.codeTextColor?.let { builder.codeTextColor(it) }
        config.codeBackgroundColor?.let { builder.codeBackgroundColor(it) }
        config.codeBlockTextColor?.let { builder.codeBlockTextColor(it) }
        config.codeBlockBackgroundColor?.let { builder.codeBlockBackgroundColor(it) }
        config.codeTypeface?.let { builder.codeTypeface(it) }
        config.codeBlockTypeface?.let { builder.codeBlockTypeface(it) }
        config.codeTextSize?.let { builder.codeTextSize(it) }
        config.codeBlockTextSize?.let { builder.codeBlockTextSize(it) }
        config.headingBreakHeight?.let { builder.headingBreakHeight(it) }
        config.headingBreakColor?.let { builder.headingBreakColor(it) }
        config.headingTypeface?.let { builder.headingTypeface(it) }
        config.headingTextSizeMultipliers?.let { builder.headingTextSizeMultipliers(it) }
        config.thematicBreakColor?.let { builder.thematicBreakColor(it) }
        config.thematicBreakHeight?.let { builder.thematicBreakHeight(it) }
    }

    private fun configureLinks(builder: MarkwonConfiguration.Builder) {
        builder.linkResolver { view, link ->
            val deepLink = UrlUtils.extractDeepLink(link)
            val url = UrlUtils.extractLink(link)
            when {
                url != null -> {
                    UrlUtils.openUrl(view.context, url)
                }
                deepLink != null -> {
                    UrlUtils.openUrl(view.context, deepLink)
                }
            }
        }
    }
}
