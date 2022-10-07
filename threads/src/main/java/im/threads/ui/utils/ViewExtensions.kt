package im.threads.ui.utils

import android.view.View

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun showViews(vararg views: View) {
    views.forEach { it.visible() }
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun hideViews(vararg views: View) {
    views.forEach { it.invisible() }
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun goneViews(vararg views: View) {
    views.forEach { it.gone() }
}
