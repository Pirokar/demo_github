package im.threads.business.serviceLocator.core

typealias Declaration<T> = () -> T

fun <T : Any> Declaration<T>.toService(): ServiceType {
    val instance: T = this()
    return DefaultLocatorServiceType.createService(instance)
}
