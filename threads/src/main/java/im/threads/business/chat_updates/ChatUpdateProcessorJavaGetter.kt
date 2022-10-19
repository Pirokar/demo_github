package im.threads.business.chat_updates // ktlint-disable package-name

import im.threads.business.serviceLocator.core.inject

/**
 * Класс, предоставляющий в режиме совместимости ChatUpdateProcessor для Java кода
 */
class ChatUpdateProcessorJavaGetter {
    val processor: ChatUpdateProcessor by inject()
}
