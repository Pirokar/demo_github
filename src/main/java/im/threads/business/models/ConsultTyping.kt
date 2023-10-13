package im.threads.business.models

import androidx.core.util.ObjectsCompat

class ConsultTyping(consultId: String?, override var timeStamp: Long, avatarPath: String?) :
    ConsultChatPhrase(avatarPath, consultId), ChatItem {
    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is ConsultTyping
    }

    override val modified = null
    override val threadId: Long?
        get() = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ConsultTyping
        return timeStamp == that.timeStamp
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(timeStamp)
    }
}
