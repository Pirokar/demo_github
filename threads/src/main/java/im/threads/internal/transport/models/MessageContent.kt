package im.threads.internal.transport.models

import im.threads.internal.model.QuickReply
import im.threads.internal.model.Settings
import java.util.Date

data class MessageContent(
    val uuid: String? = null,
    val text: String? = null,
    val formattedText: String? = null,
    val receivedDate: Date? = null,
    val threadId: Long? = null,
    val operator: Operator? = null,
    val providerIds: List<String>? = null,
    val attachments: List<Attachment>? = null,
    val quotes: List<Quote>? = null,
    val quickReplies: List<QuickReply>? = null,
    val settings: Settings? = null,
    val speechStatus: String? = null
)
