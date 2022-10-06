package im.threads.business.serviceLocator.core

import kotlin.reflect.KClass

class DefaultLocatorServiceType(
    override val type: KClass<*>,
    override val instance: Any
) : ServiceType {
    companion object {
        fun createService(instance: Any) = DefaultLocatorServiceType(instance::class, instance)
    }
}
