package im.threads.internal.model

import im.threads.business.models.ChatItem
import im.threads.internal.formatters.SpeechStatus

data class SpeechMessageUpdate constructor(
    val uuid: String,
    val speechStatus: SpeechStatus,
    val fileDescription: FileDescription
) : ChatItem {

    override val timeStamp: Long
        get() = 0

    override val threadId: Long?
        get() = null

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is SpeechMessageUpdate
    }
}
