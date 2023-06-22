package im.threads.business.transport.threadsGate

import com.google.gson.JsonObject
import im.threads.business.models.ChatItem
import im.threads.business.transport.MessageParser
import im.threads.business.transport.threadsGate.responses.BaseMessage

internal class ThreadsGateMessageParser(private val messageParser: MessageParser) {

    fun getType(message: BaseMessage): String {
        val content = message.content ?: JsonObject()
        return messageParser.getType(content)
    }

    fun format(message: BaseMessage): ChatItem? {
        return messageParser.format(
            message.sentAt?.time ?: 0L,
            message.notification,
            message.content
        )
    }
}
