package im.threads.business.serviceLocator.core

import kotlin.reflect.KClass

/**
 * Хранимый объект в сервис локаторе
 */
interface ServiceType {
    /**
     * Тип объекта
     */
    val type: KClass<*>

    /**
     * Хранимый объект
     */
    val instance: Any
}
