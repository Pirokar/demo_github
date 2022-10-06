package im.threads.business.serviceLocator.core

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class ServiceLocator {
    private val serviceTypeMap: MutableMap<KClass<*>, ServiceType> = ConcurrentHashMap()

    fun <T : Any> getService(clz: KClass<T>): ServiceType {
        return serviceTypeMap[clz] ?: error("Unable to find definition of $clz")
    }

    private fun addService(serviceType: ServiceType) {
        serviceTypeMap[serviceType.type] = serviceType
    }

    fun loadModules(modules: List<LocatorModule>) {
        modules.forEach(::registerModule)
    }

    private fun registerModule(module: LocatorModule) {
        module.declarationRegistry.forEach {
            addService(it.value.toService())
        }
    }
}
