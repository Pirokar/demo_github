package im.threads.business.utils.messenger

import im.threads.business.models.ChatItem
import im.threads.business.models.UserPhrase
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface Messenger {
    val resendStream: PublishSubject<String>

    fun sendMessage(userPhrase: UserPhrase)
    fun downloadMessagesTillEnd(): Single<List<ChatItem>>
    fun saveMessages(chatItems: List<ChatItem>)
    fun queueMessageSending(userPhrase: UserPhrase)
    fun proceedSendingQueue(chatItem: UserPhrase)
    fun addMsgToResendQueue(userPhrase: UserPhrase)
    fun forceResend(userPhrase: UserPhrase)
    fun resendMessages()
    fun removeUserMessageFromQueue(userPhrase: UserPhrase)
    fun clearSendQueue()
    fun recreateUnsentMessagesWith(phrases: List<UserPhrase>)
    fun onViewStart()
    fun onViewStop()
    fun onViewDestroy()
}
