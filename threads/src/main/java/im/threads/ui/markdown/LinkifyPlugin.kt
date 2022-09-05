package im.threads.ui.markdown

import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import android.text.util.Linkify
import androidx.annotation.IntDef
import androidx.core.text.util.LinkifyCompat
import im.threads.internal.utils.UrlUtils
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.CorePlugin.OnTextAddedListener
import io.noties.markwon.core.CoreProps
import org.commonmark.node.Link

open class LinkifyPlugin internal constructor(
    @param:LinkifyMask private val mask: Int,
    private val useCompat: Boolean
) : AbstractMarkwonPlugin() {
    @IntDef(flag = true, value = [Linkify.EMAIL_ADDRESSES, Linkify.PHONE_NUMBERS, Linkify.WEB_URLS])
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class LinkifyMask

    override fun configure(registry: MarkwonPlugin.Registry) {
        registry.require(CorePlugin::class.java) { corePlugin ->
            val listener: LinkifyTextAddedListener = if (useCompat) {
                LinkifyCompatTextAddedListener(mask)
            } else {
                LinkifyTextAddedListener(mask)
            }
            corePlugin.addOnTextAddedListener(listener)
        }
    }

    private open inner class LinkifyTextAddedListener(private val mask: Int) :
        OnTextAddedListener {
        override fun onTextAdded(visitor: MarkwonVisitor, text: String, start: Int) {
            val spanFactory = visitor.configuration().spansFactory().get(
                Link::class.java
            ) ?: return

            val builder = SpannableStringBuilder(text)
            if (addLinks(builder, mask)) {
                val spans = builder.getSpans(0, builder.length, URLSpan::class.java)
                if (spans != null && spans.isNotEmpty()) {
                    val renderProps = visitor.renderProps()
                    val spannableBuilder = visitor.builder()
                    for (span in spans) {
                        CoreProps.LINK_DESTINATION[renderProps] = span.url
                        SpannableBuilder.setSpans(
                            spannableBuilder,
                            spanFactory.getSpans(visitor.configuration(), renderProps),
                            start + builder.getSpanStart(span),
                            start + builder.getSpanEnd(span)
                        )
                    }
                }
            }
        }

        protected open fun addLinks(text: Spannable, @LinkifyMask mask: Int): Boolean {
            return addAllLinks(text, mask, false)
        }
    }

    private inner class LinkifyCompatTextAddedListener internal constructor(mask: Int) : LinkifyTextAddedListener(mask) {
        override fun addLinks(text: Spannable, @LinkifyMask mask: Int): Boolean {
            return addAllLinks(text, mask, true)
        }
    }

    private fun addAllLinks(text: Spannable, @LinkifyMask mask: Int, isCompat: Boolean): Boolean {
        if (isCompat) {
            LinkifyCompat.addLinks(text, mask)
        } else {
            Linkify.addLinks(text, mask)
        }
        UrlUtils.extractLink(text.toString())?.let { extractedLink ->
            if (!extractedLink.isEmail && extractedLink.link != null) {
                val scheme = Uri.parse(extractedLink.link).scheme
                if (isCompat) {
                    LinkifyCompat.addLinks(text, UrlUtils.WEB_URL, scheme, null, null)
                } else {
                    Linkify.addLinks(text, UrlUtils.WEB_URL, scheme, null, null)
                }
            } else {
                null
            }
        }

        return true
    }

    companion object {
        /**
         * @param useCompat Если true, использует [LinkifyCompat] для управления ссылками.
         * Имейте ввиду, что [LinkifyCompat] зависит от androidx.core:core,
         * зависимость должна присутствовать в приложении.
         */
        @JvmOverloads
        fun create(useCompat: Boolean = false): LinkifyPlugin {
            return create(
                Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS or Linkify.WEB_URLS,
                useCompat
            )
        }

        fun create(@LinkifyMask mask: Int): LinkifyPlugin {
            return LinkifyPlugin(mask, false)
        }

        /**
         * @param useCompat Если true, использует [LinkifyCompat] для управления ссылками.
         * Имейте ввиду, что [LinkifyCompat] зависит от androidx.core:core,
         * зависимость должна присутствовать в приложении.
         */
        fun create(@LinkifyMask mask: Int, useCompat: Boolean): LinkifyPlugin {
            return LinkifyPlugin(mask, useCompat)
        }
    }
}
