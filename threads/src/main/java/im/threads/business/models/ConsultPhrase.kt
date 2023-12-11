package im.threads.business.models

import androidx.core.util.ObjectsCompat
import im.threads.business.formatters.SpeechStatus
import im.threads.business.models.enums.ModificationStateEnum
import im.threads.business.ogParser.OGData
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.FileUtils.isVoiceMessage

class ConsultPhrase constructor(
    override var id: String?, // This this a mfms messageId required for read status updates
    override var fileDescription: FileDescription?,
    val modified: ModificationStateEnum,
    override var quote: Quote?,
    var consultName: String?,
    override val phraseText: String?,
    val formattedPhrase: String?,
    var date: Long,
    consultId: String?,
    avatarPath: String?,
    var read: Boolean,
    val status: String?,
    val sex: Boolean,
    override val threadId: Long?,
    val quickReplies: List<QuickReply>?,
    val isBlockInput: Boolean?,
    val speechStatus: SpeechStatus,
    val role: ConsultRole,
    val isPersonalOffer: Boolean = false
) : ConsultChatPhrase(avatarPath, consultId), ChatPhrase {
    var ogData: OGData? = null
    var ogUrl: String? = null
    var isAvatarVisible = true
    var errorMock: Boolean? = null

    // для поиска сообщений в чате
    override var found = false

    val isOnlyImage: Boolean
        get() = (phraseText.isNullOrEmpty() && quote == null && isImage(fileDescription))

    val isOnlyDoc: Boolean
        get() = (
            phraseText.isNullOrEmpty() &&
                formattedPhrase.isNullOrEmpty() &&
                fileDescription != null &&
                !isImage(fileDescription) &&
                !isVoiceMessage(fileDescription)
            )

    val isVoiceMessage: Boolean
        get() = (speechStatus != SpeechStatus.NO_SPEECH_STATUS || isVoiceMessage(fileDescription))

    override val timeStamp
        get() = date

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        return if (otherItem is ConsultPhrase) {
            ObjectsCompat.equals(id, otherItem.id)
        } else {
            false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ConsultPhrase
        return sex == that.sex && date == that.date && isAvatarVisible == that.isAvatarVisible && read == that.read && found == that.found &&
            ObjectsCompat.equals(id, that.id) &&
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
            sex,
            date,
            phraseText,
            formattedPhrase,
            consultName,
            isAvatarVisible,
            quote,
            fileDescription,
            read,
            status,
            quickReplies,
            isBlockInput,
            found,
            ogData,
            ogUrl,
            threadId
        )
    }

    fun copy() = ConsultPhrase(
        id, fileDescription, modified, quote, consultName, phraseText, formattedPhrase, date, consultId,
        avatarPath, read, status, sex, threadId, quickReplies, isBlockInput, speechStatus, role
    ).also {
        it.ogData = ogData
        it.ogUrl = ogUrl
        it.isAvatarVisible = isAvatarVisible
        it.found = found
    }
}
