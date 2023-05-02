package im.threads.business.core

import android.annotation.SuppressLint
import android.content.Context

/**
 * Only application context allowed here
 */
@SuppressLint("StaticFieldLeak")
object ContextHolder {
    lateinit var context: Context
}
