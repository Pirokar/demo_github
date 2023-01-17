package im.threads.business.secureDatabase

import android.content.Context
import im.threads.business.annotation.OpenForTesting
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultInfo
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageState
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.UserPhrase
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@OpenForTesting
class DatabaseHolder(private val context: Context) {

    init { checkAndUpdate() }

    private val myOpenHelper = ThreadsDbHelper.getInstance(context)

    fun cleanDatabase() {
        myOpenHelper.cleanDatabase()
    }

    fun getChatItems(offset: Int, limit: Int): List<ChatItem> =
        myOpenHelper.getChatItems(offset, limit)

    fun getChatItem(messageUuid: String?): ChatItem? = myOpenHelper.getChatItem(messageUuid)

    fun putChatItems(items: List<ChatItem?>?) {
        myOpenHelper.putChatItems(items)
    }

    fun putChatItem(chatItem: ChatItem?): Boolean = myOpenHelper.putChatItem(chatItem)

    // FileDescriptions
    val allFileDescriptions: Single<List<FileDescription?>?>
        get() = Single.fromCallable { myOpenHelper.getAllFileDescriptions() }
            .subscribeOn(Schedulers.io())

    // UserPhrase
    fun updateFileDescription(fileDescription: FileDescription) {
        myOpenHelper.updateFileDescription(fileDescription)
    }

    fun updateChatItemByTimeStamp(chatItem: ChatItem) {
        myOpenHelper.updateChatItemByTimeStamp(chatItem)
    }

    fun getConsultInfo(id: String): ConsultInfo? = myOpenHelper.getLastConsultInfo(id)

    fun getUnsendUserPhrase(count: Int): List<UserPhrase> = myOpenHelper.getUnsendUserPhrase(count)

    // ConsultPhrase
    fun setStateOfUserPhraseByMessageId(uuid: String?, messageState: MessageState?) {
        myOpenHelper.setUserPhraseStateByMessageId(uuid, messageState)
    }

    val lastConsultPhrase: Single<ConsultPhrase?> =
        Single.fromCallable { myOpenHelper.getLastConsultPhrase() }
            .subscribeOn(Schedulers.io())

    fun setAllConsultMessagesWereRead(): Completable {
        return Completable.fromCallable { myOpenHelper.setAllConsultMessagesWereRead() }
            .subscribeOn(Schedulers.io())
    }

    fun setAllConsultMessagesWereReadInThread(threadId: Long?): Completable {
        return Completable.fromCallable { myOpenHelper.setAllConsultMessagesWereReadWithThreadId(threadId) }
            .subscribeOn(Schedulers.io())
    }

    fun setMessageWasRead(uuid: String?) {
        uuid?.let { myOpenHelper.setMessageWasRead(it) }
    }

    fun saveSpeechMessageUpdate(speechMessageUpdate: SpeechMessageUpdate?) {
        myOpenHelper.speechMessageUpdated(speechMessageUpdate)
    }

    fun setNotSentSurveyDisplayMessageToFalse(): Completable {
        return Completable.fromCallable { myOpenHelper.setNotSentSurveyDisplayMessageToFalse() }
            .subscribeOn(Schedulers.io())
    }

    fun setOldRequestResolveThreadDisplayMessageToFalse(): Completable {
        return Completable.fromCallable { myOpenHelper.setOldRequestResolveThreadDisplayMessageToFalse() }
            .subscribeOn(Schedulers.io())
    }

    fun getMessagesCount(): Int = myOpenHelper.getMessagesCount()

    fun getUnreadMessagesCount(): Int = myOpenHelper.getUnreadMessagesCount()

    fun getUnreadMessagesUuid(): List<String?> = myOpenHelper.getUnreadMessagesUuid()

    private fun checkAndUpdate() {
        val oldHelper = im.threads.business.database.ThreadsDbHelper(context)
        if (needMigrateToNewDB(oldHelper)) {
            putChatItems(oldHelper.getChatItems(0, -1))
            myOpenHelper.putFileDescriptions(oldHelper.allFileDescriptions)
            oldHelper.cleanDatabase()
        }
    }

    private fun needMigrateToNewDB(helper: im.threads.business.database.ThreadsDbHelper): Boolean {
        return helper.getChatItems(0, -1).size > 0 || helper.allFileDescriptions.size > 0
    }
}
