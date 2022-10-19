package im.threads.business.serviceLocator.core

import kotlin.reflect.KClass

/**
 * Инстанс объекта в сервис локаторе
 * @param type тип хранимого объекта
 * @param instance инстанс хранимого объекта
 */
class DefaultLocatorServiceType(
    override val type: KClass<*>,
    override val instance: Any
) : ServiceType {
    companion object {
        /**
         * Создает сервис в локаторе для передаваемого инстанса объекта
         * @param instance инстанс объекта, для которого необходимо создать сервис в локаторе
         */
        fun createService(instance: Any) = DefaultLocatorServiceType(instance::class, instance)
    }
}
