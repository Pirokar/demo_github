package im.threads.business.serviceLocator.core

import android.annotation.SuppressLint

@SuppressLint("StaticFieldLeak")
object LocatorContext {
    private val locator = Locator()

    fun modules(vararg modules: LocatorModule) {
        locator.loadModules(modules.toList())
    }

    fun getLocator() = locator
}

fun startEdnaLocator(block: LocatorContext.() -> Unit) {
    LocatorContext.apply(block)
}
