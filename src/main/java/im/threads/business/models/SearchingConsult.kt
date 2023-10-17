package im.threads.business.models

import androidx.core.util.ObjectsCompat
import java.util.Calendar

class SearchingConsult : ChatItem {
    override var timeStamp: Long = 0

    init {
        val c = Calendar.getInstance()
        c[Calendar.HOUR_OF_DAY] = 23
        c[Calendar.MINUTE] = 59
        c[Calendar.SECOND] = 59
        timeStamp = c.timeInMillis
    }

    fun setDate(date: Long) {
        timeStamp = date
    }

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is SearchingConsult
    }

    override val threadId: Long?
        get() = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as SearchingConsult
        return timeStamp == that.timeStamp
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(timeStamp)
    }

    override fun toString(): String {
        return "SearchingConsult{" +
            "date=" + timeStamp +
            '}'
    }
}
