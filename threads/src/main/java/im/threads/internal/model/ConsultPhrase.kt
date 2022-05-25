package im.threads.internal.model

import android.text.TextUtils
import androidx.core.util.ObjectsCompat
import im.threads.internal.formatters.SpeechStatus
import im.threads.internal.opengraph.OGData
import im.threads.internal.utils.FileUtils.isImage
import im.threads.internal.utils.FileUtils.isVoiceMessage

class ConsultPhrase constructor(
    override var id: String?, // This this a mfms messageId required for read status updates
    val providerId: String?,
    val providerIds: List<String>,
    override var fileDescription: FileDescription?,
    override val quote: Quote?,
    val consultName: String?,
    override val phraseText: String?,
    val formattedPhrase: String?,
    val date: Long,
    consultId: String?,
    avatarPath: String?,
    var isRead: Boolean,
    val status: String?,
    val sex: Boolean,
    override val threadId: Long?,
    val quickReplies: List<QuickReply>?,
    val isBlockInput: Boolean,
    val speechStatus: SpeechStatus
) : ConsultChatPhrase(avatarPath, consultId), ChatPhrase {
    var ogData: OGData? = null
    var ogUrl: String? = null
    var isAvatarVisible = true

    // для поиска сообщений в чате
    override var found = false

    val isOnlyImage: Boolean
        get() = (
            TextUtils.isEmpty(phraseText) &&
                quote == null && isImage(fileDescription)
            )
    val isOnlyDoc: Boolean
        get() = (
            TextUtils.isEmpty(phraseText) &&
                !isImage(fileDescription) &&
                !isVoiceMessage(fileDescription)
            )
    val isVoiceMessage: Boolean
        get() = (
            speechStatus !== SpeechStatus.UNKNOWN ||
                isVoiceMessage(fileDescription)
            )

    override val timeStamp
        get() = date

    fun hasSameContent(consultPhrase: ConsultPhrase?): Boolean {
        if (consultPhrase == null) {
            return false
        }
        var hasSameContent = (
            ObjectsCompat.equals(id, consultPhrase.id) &&
                ObjectsCompat.equals(phraseText, consultPhrase.phraseText) &&
                ObjectsCompat.equals(formattedPhrase, consultPhrase.formattedPhrase) &&
                ObjectsCompat.equals(providerId, consultPhrase.providerId) &&
                ObjectsCompat.equals(date, consultPhrase.date) &&
                ObjectsCompat.equals(isRead, consultPhrase.isRead) &&
                ObjectsCompat.equals(avatarPath, consultPhrase.avatarPath) &&
                ObjectsCompat.equals(consultId, consultPhrase.consultId) &&
                ObjectsCompat.equals(consultName, consultPhrase.consultName) &&
                ObjectsCompat.equals(sex, consultPhrase.sex) &&
                ObjectsCompat.equals(status, consultPhrase.status) &&
                ObjectsCompat.equals(threadId, consultPhrase.threadId) &&
                ObjectsCompat.equals(isBlockInput, consultPhrase.isBlockInput)
            )
        hasSameContent =
            hasSameContent && fileDescription?.hasSameContent(consultPhrase.fileDescription) ?: consultPhrase.fileDescription == null
        if (quote != null) {
            hasSameContent = hasSameContent && quote.hasSameContent(consultPhrase.quote)
        }
        return hasSameContent
    }

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return if (otherItem is ConsultPhrase) {
            ObjectsCompat.equals(id, otherItem.id)
        } else false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ConsultPhrase
        return sex == that.sex && date == that.date && isAvatarVisible == that.isAvatarVisible && isRead == that.isRead && found == that.found &&
            ObjectsCompat.equals(id, that.id) &&
            ObjectsCompat.equals(providerId, that.providerId) &&
            ObjectsCompat.equals(providerIds, that.providerIds) &&
            ObjectsCompat.equals(phraseText, that.phraseText) &&
            ObjectsCompat.equals(formattedPhrase, that.formattedPhrase) &&
            ObjectsCompat.equals(consultName, that.consultName) &&
            ObjectsCompat.equals(quote, that.quote) &&
            ObjectsCompat.equals(fileDescription, that.fileDescription) &&
            ObjectsCompat.equals(status, that.status) &&
            ObjectsCompat.equals(quickReplies, that.quickReplies) &&
            ObjectsCompat.equals(isBlockInput, that.isBlockInput) &&
            ObjectsCompat.equals(ogData, that.ogData) &&
            ObjectsCompat.equals(ogUrl, that.ogUrl) &&
            ObjectsCompat.equals(threadId, that.threadId)
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(
            id,
            providerId,
            providerIds,
            sex,
            date,
            phraseText,
            formattedPhrase,
            consultName,
            isAvatarVisible,
            quote,
            fileDescription,
            isRead,
            status,
            quickReplies,
            isBlockInput,
            found,
            ogData,
            ogUrl,
            threadId
        )
    }
}
