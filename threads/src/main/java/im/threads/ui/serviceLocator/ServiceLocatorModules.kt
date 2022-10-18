package im.threads.ui.serviceLocator

import im.threads.business.serviceLocator.core.module
import im.threads.ui.ChatCenterPushMessageHelper
import im.threads.ui.styles.StyleUseCase

val uiSLModule = module {
    factory { ChatCenterPushMessageHelper() }
    factory { StyleUseCase(get()) }
}
