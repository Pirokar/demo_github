package im.threads.business.serviceLocator.core

import im.threads.business.logger.LoggerEdna
import kotlin.reflect.KClass

/**
 * Главный класс сервис локатора. Обеспечивает доступ к сервис локатору. Хранит модули
 * и предоставляет их сервис локатору.
 */
class Locator {
    private val registry = ServiceLocator()
    lateinit var declarations: Map<KClass<*>, Declaration<Any>>

    /**
     * Загружает список модулей.
     * @param modules список модулей
     */
    fun loadModules(modules: List<LocatorModule>) {
        declarations = modules.declarationRegistry
        LoggerEdna.info("Registry Size === ${modules.declarationRegistry.size}")
        registry.loadModules(modules)
    }

    /**
     * Запрашивает инстанс объекта по его классу
     * @param type тип объекта
     */
    fun resolveInstance(type: KClass<*>) = registry.getService(type)
}
