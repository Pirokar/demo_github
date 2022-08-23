package im.threads.internal.model

import android.text.TextUtils
import androidx.core.util.ObjectsCompat
import im.threads.business.models.ChatItem
import im.threads.internal.utils.FileUtils.isImage
import im.threads.internal.utils.FileUtils.isVoiceMessage
import java.util.UUID

class UserPhrase constructor(
    override var id: String?,
    // This this a mfms messageId required for read status updates
    var providerId: String?,
    val providerIds: List<String>?,
    override val phraseText: String?,
    override val quote: Quote?,
    override var timeStamp: Long,
    override var fileDescription: FileDescription?,
    var sentState: MessageState,
    override val threadId: Long?
) : ChatPhrase {
    @JvmField
    var ogUrl: String? = null
    var isCopy = false
    var campaignMessage: CampaignMessage? = null

    // для поиска сообщений в чате
    override var found = false

    constructor(
        uuid: String?,
        providerId: String?,
        phrase: String?,
        mQuote: Quote?,
        phraseTimeStamp: Long,
        fileDescription: FileDescription?,
        threadId: Long?
    ) : this(
        uuid,
        providerId,
        null,
        phrase,
        mQuote,
        phraseTimeStamp,
        fileDescription,
        MessageState.STATE_SENDING,
        threadId
    )

    constructor(
        phrase: String?,
        mQuote: Quote?,
        phraseTimeStamp: Long,
        fileDescription: FileDescription?,
        threadId: Long?
    ) : this(
        UUID.randomUUID().toString(), "tempProviderId: " + UUID.randomUUID().toString(), null,
        phrase, mQuote, phraseTimeStamp, fileDescription, MessageState.STATE_SENDING, threadId
    )

    val isOnlyImage: Boolean
        get() = (
            TextUtils.isEmpty(phraseText) &&
                quote == null && isImage(fileDescription)
            )
    val isOnlyVoiceMessage: Boolean
        get() = (
            TextUtils.isEmpty(phraseText) &&
                quote == null && isVoiceMessage(fileDescription)
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

    fun hasSameContent(userPhrase: UserPhrase?): Boolean {
        if (userPhrase == null) {
            return false
        }
        var hasSameContent = (
            ObjectsCompat.equals(id, userPhrase.id) &&
                ObjectsCompat.equals(phraseText, userPhrase.phraseText) &&
                ObjectsCompat.equals(providerId, userPhrase.providerId) &&
                ObjectsCompat.equals(timeStamp, userPhrase.timeStamp) &&
                ObjectsCompat.equals(sentState, userPhrase.sentState)
            )
        if (fileDescription != null) {
            hasSameContent =
                hasSameContent && fileDescription!!.hasSameContent(userPhrase.fileDescription)
        }
        if (quote != null) {
            hasSameContent = hasSameContent && quote.hasSameContent(userPhrase.quote)
        }
        return hasSameContent
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
            ObjectsCompat.equals(providerId, that.providerId) &&
            ObjectsCompat.equals(providerIds, that.providerIds) &&
            ObjectsCompat.equals(phraseText, that.phraseText) && sentState == that.sentState &&
            ObjectsCompat.equals(quote, that.quote) &&
            ObjectsCompat.equals(fileDescription, that.fileDescription) &&
            ObjectsCompat.equals(ogUrl, that.ogUrl) &&
            ObjectsCompat.equals(threadId, that.threadId)
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(
            id,
            providerId,
            providerIds,
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
