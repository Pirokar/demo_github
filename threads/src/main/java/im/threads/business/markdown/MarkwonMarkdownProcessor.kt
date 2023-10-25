package im.threads.business.markdown

import android.content.Context
import android.text.Spanned
import android.text.util.Linkify
import im.threads.business.utils.UrlUtils
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.image.ImagesPlugin

// Constructor for test purposes
class MarkwonMarkdownProcessor(
    private val context: Context,
    incomingMarkdownConfig: MarkdownConfig,
    outgoingMarkdownConfig: MarkdownConfig,
    private val highlightEmails: Boolean = true
) : MarkdownProcessor {
    private val incomingProcessor: Markwon
    private val outgoingProcessor: Markwon

    init {
        incomingProcessor = configureParser(incomingMarkdownConfig)
        outgoingProcessor = configureParser(outgoingMarkdownConfig)
    }

    override fun parseClientMessage(text: String): Spanned {
        return outgoingProcessor.toMarkdown(text)
    }

    override fun parseOperatorMessage(text: String): Spanned {
        return incomingProcessor.toMarkdown(text)
    }

    private fun configureParser(markdownConfig: MarkdownConfig): Markwon {
        val builder = Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    configureMessagesView(builder, markdownConfig)
                }

                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    super.configureConfiguration(builder)
                    configureLinks(builder)
                }
            })
        if (highlightEmails) {
            builder.usePlugin(LinkifyPlugin.create())
        } else {
            builder.usePlugin(LinkifyPlugin.create(Linkify.PHONE_NUMBERS or Linkify.WEB_URLS))
        }

        return builder.build()
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
            (UrlUtils.extractLink(link)?.link ?: UrlUtils.extractDeepLink(link))?.let {
                UrlUtils.openUrl(view.context, it)
            }
        }
    }
}