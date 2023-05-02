package im.threads.business.serviceLocator.core

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Модуль сервис локатора. Предоставляет и создает хранимые зависимости
 */
class LocatorModule {
    val declarationRegistry: MutableMap<KClass<*>, Declaration<Any>> = ConcurrentHashMap()

    /**
     * Сохраняет объект в сервис локатор
     * @param declaration декаларация объекта, сохраняемая в сервис локатор
     */
    inline fun <reified T : Any> factory(noinline declaration: Declaration<T>) {
        declarationRegistry[T::class] = declaration
    }

    /**
     * Предоставляет хранимый объект по его типу
     */
    inline fun <reified T : Any> get(): T {
        val declaration = declarationRegistry[T::class]
        var instance = declaration?.invoke()
        if (instance == null) {
            val locator = LocatorContext.getLocator()
            instance = locator.declarations[T::class]?.invoke()
                ?: error("Unable to find declaration of type ${T::class.qualifiedName}")
        }
        return instance as T
    }
}

/**
 * Map с хранимыми объектами в сервис локаторе
 */
val List<LocatorModule>.declarationRegistry: Map<KClass<*>, Declaration<Any>>
    get() = this.fold(this[0].declarationRegistry) { acc, module -> (acc + module.declarationRegistry) as MutableMap<KClass<*>, Declaration<Any>> }
