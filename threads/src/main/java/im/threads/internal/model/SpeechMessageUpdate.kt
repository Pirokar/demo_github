package im.threads.internal.model

import im.threads.internal.formatters.SpeechStatus

data class SpeechMessageUpdate constructor(
    val uuid: String,
    val speechStatus: SpeechStatus,
    val fileDescription: FileDescription
) : ChatItem {
    override fun getTimeStamp(): Long {
        return 0
    }

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return otherItem is SpeechMessageUpdate
    }

    override fun getThreadId(): Long? {
        return null
    }
}
