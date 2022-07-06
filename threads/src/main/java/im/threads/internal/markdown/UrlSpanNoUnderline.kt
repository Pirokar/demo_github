package im.threads.internal.markdown

import android.text.TextPaint
import android.text.style.URLSpan

/**
 * Убирает подчеркивания для url
 * @param url необходим для базового класса
 */
class UrlSpanNoUnderline(url: String?) : URLSpan(url) {
    override fun updateDrawState(drawState: TextPaint) {
        super.updateDrawState(drawState)
        drawState.isUnderlineText = false
    }
}
