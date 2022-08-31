package im.threads.internal.model

import im.threads.business.models.ChatItem

class NoChatItem(override val timeStamp: Long = 0L, override val threadId: Long? = 0L) : ChatItem {
    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return false
    }
}
