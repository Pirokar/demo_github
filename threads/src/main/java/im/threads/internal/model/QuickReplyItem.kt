package im.threads.internal.model

import im.threads.business.models.ChatItem
import im.threads.business.models.QuickReply

class QuickReplyItem(val items: List<QuickReply>, override val timeStamp: Long) :
    ChatItem {

    override val threadId: Long?
        get() = null

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is QuickReplyItem
    }
}
