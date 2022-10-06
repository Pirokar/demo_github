package im.threads.business.serviceLocator.core

import im.threads.business.logger.LoggerEdna
import kotlin.reflect.KClass

class Locator {
    private val registry = ServiceLocator()
    lateinit var declarations: Map<KClass<*>, Declaration<Any>>

    fun loadModules(modules: List<LocatorModule>) {
        declarations = modules.declarationRegistry
        LoggerEdna.info("Registry Size === ${modules.declarationRegistry.size}")
        registry.loadModules(modules)
    }

    fun resolveInstance(type: KClass<*>) = registry.getService(type)
}
