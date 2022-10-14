package im.threads.business.chat_updates // ktlint-disable package-name

import im.threads.business.serviceLocator.core.inject

class ChatUpdateProcessorJavaGetter {
    val processor: ChatUpdateProcessor by inject()
}
