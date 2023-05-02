package im.threads.business.serviceLocator.core

typealias Declaration<T> = () -> T

/**
 * Переводит декларацию инстанса объекта в хранимый объект сервис локатора
 */
fun <T : Any> Declaration<T>.toService(): ServiceType {
    val instance: T = this()
    return DefaultLocatorServiceType.createService(instance)
}
