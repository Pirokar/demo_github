package im.threads.business.transport.threadsGate.responses

import im.threads.business.models.MessageStatus

data class Status(var correlationId: String?, val messageId: String? = null, val status: MessageStatus)
