package im.threads.business.serviceLocator.core

import android.annotation.SuppressLint

/**
 * Входная точка для сервис локатора.
 */
@SuppressLint("StaticFieldLeak")
object LocatorContext {
    private val locator = Locator()

    /**
     * Загружает список модулей в сервис локатор
     * @param modules список модулей
     */
    fun modules(vararg modules: LocatorModule) {
        locator.loadModules(modules.toList())
    }

    /**
     * Возвращает сервис локатор
     */
    fun getLocator() = locator
}

/**
 * Запускает сервис локатор и создает зависимости
 * @param block принимает список модулей в виде "modules { module1, module2, ... }
 */
fun startEdnaLocator(block: LocatorContext.() -> Unit) {
    LocatorContext.apply(block)
}
