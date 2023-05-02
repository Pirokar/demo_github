package im.threads.ui.serviceLocator

import im.threads.business.serviceLocator.core.module
import im.threads.ui.ChatCenterPushMessageHelper
import im.threads.ui.styles.StyleUseCase

/**
 * Модуль зависимостей сервис локатора уровня UI
 */
val uiSLModule = module {
    factory { ChatCenterPushMessageHelper() }
    factory { StyleUseCase(get(), get()) }
}
