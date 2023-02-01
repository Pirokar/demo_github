package im.threads.business.transport.threadsGate.responses

import im.threads.business.models.MessageStatus

data class Status(val messageId: String, val status: MessageStatus)
