package im.threads.business.serviceLocator.core

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Хранит объекты внутри map и предоставляет их по запросу
 */
class ServiceLocator {
    private val serviceTypeMap: MutableMap<KClass<*>, ServiceType> = ConcurrentHashMap()

    /**
     * Предоставляет инстанс объекта
     * @param clz класс объекта, который необходимо предоставить
     */
    fun <T : Any> getService(clz: KClass<T>): ServiceType {
        val value = serviceTypeMap[clz]
        if (value == null) {
            serviceTypeMap.values.toList().forEach {
                if (clz.isInstance(it.instance)) {
                    addService(DefaultLocatorServiceType(clz, it.instance))
                    return it
                }
            }
        }
        return value ?: error("Unable to find definition of $clz")
    }

    private fun addService(serviceType: ServiceType) {
        serviceTypeMap[serviceType.type] = serviceType
    }

    /**
     * Регистрирует модули для использования
     * @param modules список модулей для регистрации
     */
    fun loadModules(modules: List<LocatorModule>) {
        modules.forEach(::registerModule)
    }

    private fun registerModule(module: LocatorModule) {
        module.declarationRegistry.forEach {
            addService(it.value.toService())
        }
    }
}