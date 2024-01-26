package im.threads.ui.utils

import android.content.Context
import android.util.TypedValue
import android.view.View

fun View?.visible() {
    this?.visibility = View.VISIBLE
}

fun showViews(vararg views: View) {
    views.forEach { it.visible() }
}

fun View?.invisible() {
    this?.visibility = View.INVISIBLE
}

fun hideViews(vararg views: View) {
    views.forEach { it.invisible() }
}

fun View?.gone() {
    this?.visibility = View.GONE
}

fun View?.isVisible(): Boolean {
    return this?.visibility == View.VISIBLE
}

fun View?.isNotVisible(): Boolean {
    return this == null || this.visibility != View.VISIBLE
}

fun goneViews(vararg views: View) {
    views.forEach { it.gone() }
}

fun View.getWidthInDp(): Float {
    return context.fromPxToDp(width.toFloat())
}

fun Context.dpToPx(dp: Int): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    dp.toFloat(),
    resources.displayMetrics
)
fun Context.fromPxToDp(px: Float): Float {
    val screenPixelDensity = resources.displayMetrics.density
    return px / screenPixelDensity
}
