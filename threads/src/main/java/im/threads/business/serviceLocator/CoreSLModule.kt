package im.threads.business.serviceLocator

import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.core.ContextHolder
import im.threads.business.formatters.JsonFormatter
import im.threads.business.imageLoading.ImageLoaderOkHttpProvider
import im.threads.business.preferences.Preferences
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.module
import im.threads.business.state.ChatState
import im.threads.business.transport.AuthHeadersProvider
import im.threads.business.transport.AuthInterceptor
import im.threads.business.transport.HistoryLoader
import im.threads.business.transport.MessageParser
import im.threads.business.transport.OutgoingMessageCreator
import im.threads.business.transport.threadsGate.ThreadsGateMessageParser
import im.threads.business.utils.AppInfo
import im.threads.business.utils.ClientUseCase
import im.threads.business.utils.ConsultWriter
import im.threads.business.utils.DemoModeProvider
import im.threads.business.utils.DeviceInfo
import im.threads.business.utils.FileProvider
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
    factory { OutgoingMessageCreator(get(), get(), get(), get()) }
    factory { ClientUseCase(get()) }
    factory { AuthInterceptor(get(), get(), get()) }
    factory { ConsultWriter(get()) }
    factory { ChatUpdateProcessor() }
    factory<NetworkInteractor> { NetworkInteractorImpl() }
    factory { HistoryLoader(get(), get()) }
    factory { JsonFormatter() }
    factory { MessageParser() }
    factory { ThreadsGateMessageParser(get()) }
    factory { AppInfo() }
    factory { DeviceInfo() }
    factory { FileProvider() }
    factory { ChatState(get()) }
    factory { DemoModeProvider(get()) }
}
