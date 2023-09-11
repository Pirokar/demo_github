package im.threads.business.models

import im.threads.business.models.enums.AttachmentStateEnum

interface ChatItem {
    val timeStamp: Long
    fun isTheSameItem(otherItem: ChatItem?): Boolean
    val threadId: Long?

    fun attachmentState(): AttachmentStateEnum {
        return (this as? ConsultPhrase)?.fileDescription?.state
            ?: (this as? UserPhrase)?.fileDescription?.state
            ?: AttachmentStateEnum.ANY
    }
}
