package im.threads.business.models

import androidx.core.util.ObjectsCompat
import im.threads.business.utils.DateHelper

class MessageFromHistory : ChatItem {
    var uuid: String? = null
    var clientId: String? = null
    override var threadId: Long? = null
    var operator: Operator? = null
    var client: Client? = null
    var receivedDate: String? = null
    var channel: Channel? = null
    var read = false
    var formattedText: String? = null
    var text: String? = null
    var speechText: String? = null
    var attachments: List<Attachment>? = null
    var quickReplies: List<QuickReply>? = null
    val settings: Settings? = null
    var quotes: List<MessageFromHistory>? = null
    var type: String? = null
    var isDisplay = false
    val speechStatus: String? = null

    // SURVEY
    val hideAfter: Long? = null

    // SURVEY ANSWERED
    var sendingId: Long? = null
    var questionId: Long? = null
    var rate: Int? = null
    var scale: Int? = null
    var isSimple = false
    var errorMock: Boolean? = null
    override val timeStamp: Long
        get() = DateHelper.getMessageTimestampFromDateString(receivedDate)

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return if (otherItem is MessageFromHistory) {
            ObjectsCompat.equals(uuid, otherItem.uuid)
        } else {
            false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as MessageFromHistory
        return read == that.read && isDisplay == that.isDisplay && isSimple == that.isSimple &&
            ObjectsCompat.equals(uuid, that.uuid) &&
            ObjectsCompat.equals(clientId, that.clientId) &&
            ObjectsCompat.equals(threadId, that.threadId) &&
            ObjectsCompat.equals(operator, that.operator) &&
            ObjectsCompat.equals(client, that.client) &&
            ObjectsCompat.equals(receivedDate, that.receivedDate) &&
            ObjectsCompat.equals(channel, that.channel) &&
            ObjectsCompat.equals(formattedText, that.formattedText) &&
            ObjectsCompat.equals(text, that.text) &&
            ObjectsCompat.equals(attachments, that.attachments) &&
            ObjectsCompat.equals(quickReplies, that.quickReplies) &&
            ObjectsCompat.equals(quotes, that.quotes) &&
            ObjectsCompat.equals(type, that.type) &&
            ObjectsCompat.equals(hideAfter, that.hideAfter) &&
            ObjectsCompat.equals(sendingId, that.sendingId) &&
            ObjectsCompat.equals(questionId, that.questionId) &&
            ObjectsCompat.equals(rate, that.rate) &&
            ObjectsCompat.equals(scale, that.scale)
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(
            uuid,
            clientId,
            threadId,
            operator,
            client,
            receivedDate,
            channel,
            read,
            formattedText,
            text,
            attachments,
            quickReplies,
            quotes,
            type,
            isDisplay,
            hideAfter,
            sendingId,
            questionId,
            rate,
            scale,
            isSimple
        )
    }
}
