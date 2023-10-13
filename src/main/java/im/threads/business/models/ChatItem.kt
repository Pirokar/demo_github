package im.threads.business.models

import im.threads.business.models.enums.ModificationStateEnum

interface ChatItem {
    val timeStamp: Long
    fun isTheSameItem(otherItem: ChatItem?): Boolean
    val threadId: Long?
    val modified: ModificationStateEnum?
}
