package im.threads.internal.model

class QuickReplyItem(val items: List<QuickReply>, override val timeStamp: Long) : ChatItem {

    override val threadId: Long?
        get() = null

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is QuickReplyItem
    }
}
