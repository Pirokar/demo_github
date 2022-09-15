package im.threads.ui.markdown

import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.URLSpan
import android.text.util.Linkify
import android.text.util.Linkify.EMAIL_ADDRESSES
import android.text.util.Linkify.PHONE_NUMBERS
import android.text.util.Linkify.WEB_URLS
import android.widget.TextView
import im.threads.business.utils.UrlUtils

class LinkifyLinksHighlighter : LinksHighlighter {
    override fun highlightAllTypeOfLinks(textView: TextView, isUnderlined: Boolean) {
        highlightUsualLinks(textView)
        if (!isUnderlined) {
            stripUnderlines(textView)
        }
    }

    override fun highlightAllTypeOfLinks(
        textView: TextView,
        url: String?,
        isUnderlined: Boolean
    ) {
        val scheme = if (url != null) Uri.parse(url).scheme else null

        highlightUsualLinks(textView)
        Linkify.addLinks(textView, UrlUtils.WEB_URL, scheme)

        if (!isUnderlined) {
            stripUnderlines(textView)
        }
    }

    private fun highlightUsualLinks(textView: TextView) {
        Linkify.addLinks(
            textView,
            WEB_URLS or EMAIL_ADDRESSES or PHONE_NUMBERS
        )
    }

    private fun stripUnderlines(textView: TextView) {
        val spannable: Spannable = SpannableString(textView.text)
        val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
        for (span in spans) {
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            val newSpan = UrlSpanNoUnderline(span.url)
            spannable.setSpan(newSpan, start, end, 0)
        }
        textView.text = spannable
    }
}
