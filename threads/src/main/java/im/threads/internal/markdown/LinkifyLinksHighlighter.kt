package im.threads.internal.markdown

import android.text.Spannable
import android.text.SpannableString
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.TextView

class LinkifyLinksHighlighter : LinksHighlighter {
    override fun highlightAllTypeOfLinks(textView: TextView, isUnderlined: Boolean) {
        Linkify.addLinks(
            textView,
            Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS
        )
        if (!isUnderlined) {
            stripUnderlines(textView)
        }
        textView.text = textView.text.trim()
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
