package im.threads.business.serviceLocator.core

fun getServiceLocator() = LocatorContext.getLocator()

/**
 * Предоставляет инстанс объекта по его типу немедленно.
 * Используется для предоставления зависимостей в модулях
 */
inline fun <reified T : Any> get(): T {
    val service = getServiceLocator().resolveInstance(T::class)
    return service.instance as T
}

/**
 * Предоставляет инстанс объекта по его типу при первом обращении к объекту.
 * Нужно использовать именно этот метод везде, кроме предоставления зависимостей в модулях.
 */
inline fun <reified T : Any> inject(): Lazy<T> = lazy { get() }

/**
 * Создает модуль из представленных зависимостей. Например: "module { factory { ContextHolder.context } }"
 * @param block список factory зависимостей
 */
fun module(block: LocatorModule.() -> Unit) = LocatorModule().apply(block)
