package im.threads.ui.utils

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.TextView

class NoLongClickMovementMethod : LinkMovementMethod() {
    var longClickDelay = ViewConfiguration.getLongPressTimeout().toLong()
    var startTime: Long = 0

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            startTime = System.currentTimeMillis()
        } else if (event.action == MotionEvent.ACTION_UP) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - startTime >= longClickDelay) {
                return true
            }
        }

        return super.onTouchEvent(widget, buffer, event)
    }

    companion object {
        @JvmStatic
        private val linkMovementMethod = NoLongClickMovementMethod()

        @JvmStatic
        fun getInstance(): LinkMovementMethod = linkMovementMethod
    }
}
