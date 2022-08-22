package im.threads.business.models

interface ChatItem {
    val timeStamp: Long
    fun isTheSameItem(otherItem: ChatItem?): Boolean
    val threadId: Long?
}
