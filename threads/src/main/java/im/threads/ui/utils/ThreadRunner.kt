package im.threads.ui.utils // ktlint-disable filename

import android.os.Handler
import android.os.Looper

fun Runnable.runOnUiThread() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        this.run()
    } else {
        Handler(Looper.getMainLooper()).post(this)
    }
}
