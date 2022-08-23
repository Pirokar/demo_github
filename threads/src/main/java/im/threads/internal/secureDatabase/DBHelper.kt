package im.threads.internal.secureDatabase

import im.threads.business.models.ChatItem
import im.threads.internal.model.ConsultInfo
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.model.FileDescription
import im.threads.internal.model.MessageState
import im.threads.internal.model.Survey
import im.threads.internal.model.UserPhrase

interface DBHelper {

    fun cleanDatabase()
    fun getChatItems(offset: Int, limit: Int): List<ChatItem?>
    fun getChatItem(messageUuid: String?): ChatItem?
    fun putChatItems(items: List<ChatItem?>?)
    fun putChatItem(chatItem: ChatItem?): Boolean

    fun getAllFileDescriptions(): List<FileDescription?>?
    fun putFileDescriptions(fileDescriptions: List<FileDescription?>)
    fun updateFileDescription(fileDescription: FileDescription)

    fun getLastConsultInfo(id: String): ConsultInfo?
    fun getUnsendUserPhrase(count: Int): List<UserPhrase?>?
    fun setUserPhraseStateByProviderId(providerId: String?, messageState: MessageState?)
    fun getLastConsultPhrase(): ConsultPhrase?

    fun setAllConsultMessagesWereRead(): Int
    fun setAllConsultMessagesWereReadWithThreadId(threadId: Long?): Int
    fun setMessageWasRead(providerId: String)
    fun getSurvey(sendingId: Long): Survey?
    fun setNotSentSurveyDisplayMessageToFalse(): Int
    fun setOldRequestResolveThreadDisplayMessageToFalse(): Int
    fun getMessagesCount(): Int
    fun getUnreadMessagesCount(): Int
    fun getUnreadMessagesUuid(): List<String?>?
}
