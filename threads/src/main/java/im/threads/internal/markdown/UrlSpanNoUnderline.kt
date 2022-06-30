package im.threads.internal.markdown

import android.text.TextPaint
import android.text.style.URLSpan

class UrlSpanNoUnderline(url: String?) : URLSpan(url) {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}
