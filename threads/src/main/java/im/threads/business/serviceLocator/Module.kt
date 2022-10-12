package im.threads.business.serviceLocator

import im.threads.business.preferences.Preferences
import im.threads.business.serviceLocator.core.LocatorContext
import im.threads.business.serviceLocator.core.module
import im.threads.business.transport.AuthInterceptor
import im.threads.business.transport.OutgoingMessageCreator
import im.threads.business.utils.ClientInteractor

val mainSLModule = module {
    factory { LocatorContext.context }
    factory { Preferences(get()) }
}
val supplementarySLModule = module {
    factory { OutgoingMessageCreator(get()) }
    factory { ClientInteractor(get()) }
    factory { AuthInterceptor(get()) }
}
