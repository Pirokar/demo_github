package im.threads.business.secureDatabase

import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultInfo
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageState
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase

interface DBHelper {

    fun cleanDatabase()
    fun getChatItems(offset: Int, limit: Int): List<ChatItem?>
    fun getChatItem(messageUuid: String?): ChatItem?
    fun putChatItems(items: List<ChatItem?>?)
    fun putChatItem(chatItem: ChatItem?): Boolean

    fun getAllFileDescriptions(): List<FileDescription?>?
    fun putFileDescriptions(fileDescriptions: List<FileDescription?>)
    fun updateFileDescription(fileDescription: FileDescription)

    fun updateChatItemByTimeStamp(chatItem: ChatItem)
    fun getLastConsultInfo(id: String): ConsultInfo?
    fun getUnsendUserPhrase(count: Int): List<UserPhrase?>?
    fun setUserPhraseStateByMessageId(uuid: String?, messageState: MessageState?)
    fun getLastConsultPhrase(): ConsultPhrase?

    fun setAllConsultMessagesWereRead(): Int
    fun setAllConsultMessagesWereReadWithThreadId(threadId: Long?): Int
    fun setMessageWasRead(uuid: String)
    fun getSurvey(sendingId: Long): Survey?
    fun setNotSentSurveyDisplayMessageToFalse(): Int
    fun setOldRequestResolveThreadDisplayMessageToFalse(): Int
    fun getMessagesCount(): Int
    fun getUnreadMessagesCount(): Int
    fun getUnreadMessagesUuid(): List<String?>?
}
