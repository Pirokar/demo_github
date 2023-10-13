package im.threads.business.models

import androidx.core.util.ObjectsCompat
import im.threads.business.models.enums.ModificationStateEnum

class DateRow(override val timeStamp: Long) : ChatItem, MediaAndFileItem {
    override fun toString(): String {
        return "DateRow{" +
            "date=" + timeStamp +
            '}'
    }

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is DateRow
    }

    override val modified = ModificationStateEnum.ANY
    override val threadId: Long?
        get() = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val dateRow = other as DateRow
        return timeStamp == dateRow.timeStamp
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(timeStamp)
    }
}
