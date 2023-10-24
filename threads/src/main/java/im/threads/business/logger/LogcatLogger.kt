package im.threads.business.logger

import android.util.Log

/**
 * Реализация логирования в logcat через стандартный java log.
 */
internal class LogcatLogger {
    fun v(tag: String?, log: String) {
        Log.v(tag, log)
    }

    fun d(tag: String?, log: String) {
        Log.d(tag, log)
    }

    fun i(tag: String?, log: String) {
        Log.i(tag, log)
    }

    fun w(tag: String?, log: String) {
        Log.w(tag, log)
    }

    fun e(tag: String?, log: String) {
        Log.e(tag, log)
    }

    fun e(tag: String?, log: String, tr: Throwable?) {
        Log.e(tag, log, tr)
    }
}
