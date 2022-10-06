package im.threads.business.serviceLocator.core

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class LocatorModule {
    val declarationRegistry: MutableMap<KClass<*>, Declaration<Any>> = ConcurrentHashMap()

    inline fun <reified T : Any> factory(noinline declaration: Declaration<T>) {
        declarationRegistry[T::class] = declaration
    }

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

    operator fun plus(module: LocatorModule) = listOf(module, this)
}

operator fun List<LocatorModule>.plus(module: LocatorModule) = this + listOf(module)

val List<LocatorModule>.declarationRegistry: Map<KClass<*>, Declaration<Any>>
    get() = this.fold(this[0].declarationRegistry) { acc, module -> (acc + module.declarationRegistry) as MutableMap<KClass<*>, Declaration<Any>> }
