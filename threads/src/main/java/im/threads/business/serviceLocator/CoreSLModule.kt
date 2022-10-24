package im.threads.business.serviceLocator

import im.threads.business.chat_updates.ChatUpdateProcessor
import im.threads.business.core.ContextHolder
import im.threads.business.preferences.Preferences
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.module
import im.threads.business.transport.AuthInterceptor
import im.threads.business.transport.OutgoingMessageCreator
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.ConsultWriter

/**
 * Модуль зависимостей сервис локатора уровня core (business logic)
 */
val coreSLModule = module {
    factory { ContextHolder.context }
    factory { Preferences(get()) }
    factory { DatabaseHolder(get()) }
    factory { OutgoingMessageCreator(get()) }
    factory { ClientUseCase(get()) }
    factory { AuthInterceptor(get()) }
    factory { ConsultWriter(get()) }
    factory { ChatUpdateProcessor() }
}
