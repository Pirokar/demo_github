package im.threads.business.models

import android.content.Context
import androidx.core.util.ObjectsCompat
import im.threads.R

class UnreadMessages(override val timeStamp: Long, var count: Int) : ChatItem {

    fun getMessage(context: Context): String {
        return context.resources.getQuantityString(R.plurals.ecc_unread_messages, count, count)
    }

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is UnreadMessages
    }

    override val modified = null
    override val threadId: Long?
        get() = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as UnreadMessages
        return timeStamp == that.timeStamp && count == that.count
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(timeStamp, count)
    }
}
