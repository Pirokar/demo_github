package im.threads.ui.utils

import android.annotation.TargetApi
import android.graphics.Typeface
import android.os.Build
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.text.style.TypefaceSpan

@TargetApi(Build.VERSION_CODES.P)
fun typefaceSpanCompatV28(typeface: Typeface) = TypefaceSpan(typeface)

class TypefaceSpanEdna(private val typeface: Typeface?) : MetricAffectingSpan() {
    override fun updateDrawState(paint: TextPaint) {
        paint.typeface = typeface
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.typeface = typeface
    }
}
