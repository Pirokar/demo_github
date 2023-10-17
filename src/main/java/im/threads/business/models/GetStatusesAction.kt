package im.threads.business.models

import androidx.core.util.ObjectsCompat

open class GetStatusesAction(val messageId: List<String>) : ChatItem {

    override var timeStamp: Long = 0
    override var threadId: Long? = null

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is GetStatusesAction
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as GetStatusesAction
        return ObjectsCompat.equals(messageId, that.messageId)
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(messageId)
    }
}
