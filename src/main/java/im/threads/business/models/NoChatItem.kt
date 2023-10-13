package im.threads.business.models

import im.threads.business.models.enums.ModificationStateEnum

class NoChatItem(override val timeStamp: Long = 0L, override val threadId: Long? = 0L) : ChatItem {
    override val modified = ModificationStateEnum.ANY
    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return false
    }
}
