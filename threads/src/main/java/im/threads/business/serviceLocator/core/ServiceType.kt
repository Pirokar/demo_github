package im.threads.business.serviceLocator.core

import kotlin.reflect.KClass

interface ServiceType {
    val type: KClass<*>
    val instance: Any
}
