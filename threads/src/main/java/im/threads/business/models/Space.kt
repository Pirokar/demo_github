package im.threads.business.models

import androidx.core.util.ObjectsCompat

class Space(val height: Int, override val timeStamp: Long) : ChatItem {

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is Space
    }

    override val threadId: Long?
        get() = null

    override fun toString(): String {
        return "Space{" +
            "height=" + height +
            ", timeStamp=" + timeStamp +
            '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val space = other as Space
        return height == space.height &&
            timeStamp == space.timeStamp
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(height, timeStamp)
    }
}
