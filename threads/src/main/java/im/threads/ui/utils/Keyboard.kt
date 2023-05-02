package im.threads.ui.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.hideKeyboard(delayMills: Long) {
    Handler(Looper.getMainLooper()).postDelayed({
        hideKeyboard()
    }, delayMills)
}

fun View.showKeyboard() {
    this.requestFocus()
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(this, 0)
}

fun View.showKeyboard(delayMills: Long) {
    Handler(Looper.getMainLooper()).postDelayed({
        showKeyboard()
    }, delayMills)
}
