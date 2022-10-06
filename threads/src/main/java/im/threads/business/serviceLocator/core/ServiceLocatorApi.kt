package im.threads.business.serviceLocator.core

fun getServiceLocator() = LocatorContext.getLocator()

inline fun <reified T : Any> get(): T {
    val service = getServiceLocator().resolveInstance(T::class)
    return service.instance as T
}

inline fun <reified T : Any> inject(): Lazy<T> = lazy { get() }

fun module(block: LocatorModule.() -> Unit) = LocatorModule().apply(block)
