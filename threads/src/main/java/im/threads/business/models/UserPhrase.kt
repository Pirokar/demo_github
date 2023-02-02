package im.threads.business.models

import android.text.TextUtils
import androidx.core.util.ObjectsCompat
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.FileUtils.isVoiceMessage
import java.util.UUID

class UserPhrase constructor(
    // This this a mfms messageId required for read status updates
    override var id: String?,
    override val phraseText: String?,
    override val quote: Quote?,
    override var timeStamp: Long,
    override var fileDescription: FileDescription?,
    var sentState: MessageStatus,
    override val threadId: Long?
) : ChatPhrase {
    @JvmField
    var ogUrl: String? = null
    var isCopy = false
    var isRead = false
    var campaignMessage: CampaignMessage? = null
    var backendMessageId: String? = null

    // для поиска сообщений в чате
    override var found = false

    constructor(
        uuid: String?,
        phrase: String?,
        mQuote: Quote?,
        phraseTimeStamp: Long,
        fileDescription: FileDescription?,
        threadId: Long?
    ) : this(
        uuid,
        phrase,
        mQuote,
        phraseTimeStamp,
        fileDescription,
        MessageStatus.SENDING,
        threadId
    )

    constructor(
        phrase: String?,
        mQuote: Quote?,
        phraseTimeStamp: Long,
        fileDescription: FileDescription?,
        threadId: Long?
    ) : this(
        UUID.randomUUID().toString(),
        phrase,
        mQuote,
        phraseTimeStamp,
        fileDescription,
        MessageStatus.SENDING,
        threadId
    )

    val isOnlyImage: Boolean
        get() = (
            TextUtils.isEmpty(phraseText) &&
                quote == null && isImage(fileDescription)
            )
    val isOnlyDoc: Boolean
        get() = (
            TextUtils.isEmpty(phraseText) &&
                quote == null &&
                !isImage(fileDescription) &&
                !isVoiceMessage(fileDescription)
            )

    fun hasFile() = fileDescription != null || (quote?.fileDescription != null)

    override fun toString(): String {
        return "UserPhrase{phrase='$phraseText'}".trimIndent()
    }

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return if (otherItem is UserPhrase) {
            ObjectsCompat.equals(id, otherItem.id)
        } else false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as UserPhrase
        return timeStamp == that.timeStamp && isCopy == that.isCopy && found == that.found &&
            ObjectsCompat.equals(id, that.id) &&
            ObjectsCompat.equals(phraseText, that.phraseText) && sentState == that.sentState &&
            ObjectsCompat.equals(quote, that.quote) &&
            ObjectsCompat.equals(fileDescription, that.fileDescription) &&
            ObjectsCompat.equals(ogUrl, that.ogUrl) &&
            ObjectsCompat.equals(threadId, that.threadId)
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(
            id,
            phraseText,
            sentState,
            quote,
            timeStamp,
            fileDescription,
            isCopy,
            found,
            ogUrl,
            threadId
        )
    }
}
