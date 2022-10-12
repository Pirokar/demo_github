package im.threads.business.serviceLocator.core

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object LocatorContext {
    lateinit var context: Context
    private val locator = Locator()

    fun modules(vararg modules: LocatorModule) {
        locator.loadModules(modules.toList())
    }

    fun getLocator() = locator
}

fun startEdnaLocator(context: Context, block: LocatorContext.() -> Unit) {
    LocatorContext.context = context
    LocatorContext.apply(block)
}
