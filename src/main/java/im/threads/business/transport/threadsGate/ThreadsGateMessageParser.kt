package im.threads.business.transport.threadsGate

import im.threads.business.models.ChatItem
import im.threads.business.transport.MessageParser
import im.threads.business.transport.threadsGate.responses.BaseMessage

internal object ThreadsGateMessageParser {
    fun getType(message: BaseMessage): String {
        return MessageParser.getType(message.content)
    }

    fun format(message: BaseMessage): ChatItem? {
        return MessageParser.format(
            message.messageId,
            message.sentAt?.time ?: 0L,
            message.notification,
            message.content
        )
    }
}