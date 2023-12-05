package im.threads.business.secureDatabase

import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultInfo
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase

interface DBHelper {

    fun cleanDatabase()
    fun closeInstance()
    fun getChatItems(offset: Int, limit: Int): List<ChatItem?>
    fun getChatItemByCorrelationId(messageUuid: String?): ChatItem?
    fun getChatItemByBackendMessageId(messageId: String?): ChatItem?
    fun getSendingChatItems(): List<UserPhrase?>
    fun getNotDeliveredChatItems(): List<UserPhrase?>
    fun putChatItems(items: List<ChatItem?>?)
    fun putChatItem(chatItem: ChatItem?): Boolean

    fun getAllFileDescriptions(): List<FileDescription?>?
    fun putFileDescriptions(fileDescriptions: List<FileDescription?>)
    fun updateFileDescription(fileDescription: FileDescription)

    fun updateChatItemByTimeStamp(chatItem: ChatItem)
    fun getLastConsultInfo(id: String): ConsultInfo?
    fun getUnsendUserPhrase(count: Int): List<UserPhrase?>?
    fun setUserPhraseStateByCorrelationId(uuid: String?, messageStatus: MessageStatus?)
    fun setUserPhraseStateByBackendMessageId(messageId: String?, messageStatus: MessageStatus?)
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
    fun setOrUpdateMessageId(correlationId: String?, backendMessageId: String?)
    fun removeItem(correlationId: String?, messageId: String?)
}
