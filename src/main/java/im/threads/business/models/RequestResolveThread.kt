package im.threads.business.models

import androidx.core.util.ObjectsCompat

class RequestResolveThread(
    val uuid: String?,
    val hideAfter: Long,
    override val timeStamp: Long,
    override val threadId: Long,
    val isRead: Boolean
) : ChatItem {

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return if (otherItem is RequestResolveThread) {
            ObjectsCompat.equals(uuid, otherItem.uuid)
        } else {
            false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RequestResolveThread
        return ObjectsCompat.equals(uuid, that.uuid) && timeStamp == that.timeStamp &&
            ObjectsCompat.equals(hideAfter, that.hideAfter) &&
            ObjectsCompat.equals(threadId, that.threadId)
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(uuid, hideAfter, timeStamp, threadId)
    }
}
