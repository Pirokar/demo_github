package im.threads.internal.model

interface ChatItem {
    val timeStamp: Long
    fun isTheSameItem(otherItem: ChatItem?): Boolean
    val threadId: Long?
}
