package im.threads.business.models

import im.threads.business.formatters.SpeechStatus

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
