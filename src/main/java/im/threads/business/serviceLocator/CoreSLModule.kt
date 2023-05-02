package im.threads.business.serviceLocator

import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.core.ContextHolder
import im.threads.business.formatters.JsonFormatter
import im.threads.business.imageLoading.ImageLoaderOkHttpProvider
import im.threads.business.preferences.Preferences
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.module
import im.threads.business.transport.AuthHeadersProvider
import im.threads.business.transport.AuthInterceptor
import im.threads.business.transport.HistoryLoader
import im.threads.business.transport.OutgoingMessageCreator
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.ConsultWriter
import im.threads.business.utils.internet.NetworkInteractor
import im.threads.business.utils.internet.NetworkInteractorImpl

/**
 * Модуль зависимостей сервис локатора уровня core (business logic)
 */
val coreSLModule = module {
    factory { ContextHolder.context }
    factory { Preferences(get()) }
    factory { DatabaseHolder(get()) }
    factory { AuthHeadersProvider() }
    factory { ImageLoaderOkHttpProvider(get(), get()) }
    factory { OutgoingMessageCreator(get()) }
    factory { ClientUseCase(get()) }
    factory { AuthInterceptor(get(), get()) }
    factory { ConsultWriter(get()) }
    factory { ChatUpdateProcessor() }
    factory<NetworkInteractor> { NetworkInteractorImpl() }
    factory { HistoryLoader(get()) }
    factory { JsonFormatter() }
}
