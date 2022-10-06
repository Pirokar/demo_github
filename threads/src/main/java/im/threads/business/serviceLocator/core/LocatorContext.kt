package im.threads.business.serviceLocator.core

object LocatorContext {
    private val locator = Locator()

    fun modules(modules: List<LocatorModule>) {
        locator.loadModules(modules)
    }

    fun getLocator() = locator
}

fun startEdnaLocator(block: LocatorContext.() -> Unit) = LocatorContext.apply(block)

