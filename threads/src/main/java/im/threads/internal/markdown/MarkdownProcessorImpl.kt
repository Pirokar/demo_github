package im.threads.internal.markdown

import android.content.Context
import android.text.Spanned
import im.threads.ChatStyle
import im.threads.internal.Config
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme

// Constructor for test purposes
class MarkdownProcessorImpl(
    private var nullableContext: Context? = null,
    private var nullableChatStyle: ChatStyle?
) : MarkdownProcessor {
    private val context: Context by lazy { nullableContext ?: Config.instance.context }
    private val chatStyle: ChatStyle by lazy { nullableChatStyle ?: Config.instance.chatStyle }

    private val incomingProcessor: Markwon by lazy {
        configureParser(chatStyle.incomingMarkdownConfiguration)
    }

    private val outgoingProcessor: Markwon by lazy {
        configureParser(chatStyle.outgoingMarkdownConfiguration)
    }

    override fun parseClientMessage(text: String): Spanned {
        return outgoingProcessor.toMarkdown(text)
    }

    override fun parseOperatorMessage(text: String): Spanned {
        return incomingProcessor.toMarkdown(text)
    }

    private fun configureParser(markdownConfig: MarkdownConfig): Markwon {
        return Markwon.builder(context)
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    configureMessages(builder, markdownConfig)
                }
            }).build()
    }

    private fun configureMessages(builder: MarkwonTheme.Builder, config: MarkdownConfig) {
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
}
