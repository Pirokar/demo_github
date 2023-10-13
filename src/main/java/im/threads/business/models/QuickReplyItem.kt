package im.threads.business.models

class QuickReplyItem(val items: List<QuickReply>, override val timeStamp: Long) :
    ChatItem {

    override val modified = null
    override val threadId: Long?
        get() = null

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is QuickReplyItem
    }
}
