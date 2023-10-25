package im.threads.business.models

import androidx.core.util.ObjectsCompat

class SimpleSystemMessage(
    val uuid: String?,
    private val type: String?,
    override val timeStamp: Long,
    private val text: String?,
    override val threadId: Long
) : ChatItem, SystemMessage {

    override fun getType(): String {
        return type ?: ""
    }

    override fun getText(): String {
        return text ?: ""
    }

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return if (otherItem is SimpleSystemMessage) {
            ObjectsCompat.equals(uuid, otherItem.uuid)
        } else {
            false
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as SimpleSystemMessage
        return timeStamp == that.timeStamp &&
            ObjectsCompat.equals(uuid, that.uuid) &&
            ObjectsCompat.equals(type, that.type) &&
            ObjectsCompat.equals(text, that.text) &&
            ObjectsCompat.equals(threadId, that.threadId)
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(uuid, type, timeStamp, text, threadId)
    }
}
