package im.threads.business.secureDatabase

import android.content.Context
import android.database.sqlite.SQLiteDiskIOException
import im.threads.R
import im.threads.business.annotation.OpenForTesting
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultInfo
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.UserPhrase
import im.threads.business.utils.Balloon
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@OpenForTesting
class DatabaseHolder(private val context: Context) {

    init {
        checkAndUpdate()
    }

    private var myOpenHelper = ThreadsDbHelper.getInstance(context)

    fun cleanDatabase() = tryExecute { myOpenHelper.cleanDatabase() }

    fun getChatItems(offset: Int, limit: Int): List<ChatItem> = tryExecute { myOpenHelper.getChatItems(offset, limit) } ?: arrayListOf()

    fun getSendingChatItems(): List<UserPhrase> = tryExecute { myOpenHelper.getSendingChatItems() } ?: arrayListOf()

    fun getChatItem(messageUuid: String?): ChatItem? = tryExecute { myOpenHelper.getChatItem(messageUuid) }

    fun putChatItems(items: List<ChatItem?>?) = tryExecute { myOpenHelper.putChatItems(items) }

    fun putChatItem(chatItem: ChatItem?): Boolean = tryExecute { myOpenHelper.putChatItem(chatItem) } ?: false

    // FileDescriptions
    val allFileDescriptions: Single<List<FileDescription?>?>
        get() = Single.fromCallable { tryExecute { myOpenHelper.getAllFileDescriptions() } }
            .subscribeOn(Schedulers.io())

    // UserPhrase
    fun updateFileDescription(fileDescription: FileDescription) {
        tryExecute { myOpenHelper.updateFileDescription(fileDescription) }
    }

    fun updateChatItemByTimeStamp(chatItem: ChatItem) {
        tryExecute { myOpenHelper.updateChatItemByTimeStamp(chatItem) }
    }

    fun getConsultInfo(id: String): ConsultInfo? = tryExecute { myOpenHelper.getLastConsultInfo(id) }

    fun getUnsendUserPhrase(count: Int): List<UserPhrase> = tryExecute { myOpenHelper.getUnsendUserPhrase(count) } ?: arrayListOf()

    // ConsultPhrase
    fun setStateOfUserPhraseByMessageId(uuid: String?, messageStatus: MessageStatus?) {
        tryExecute { myOpenHelper.setUserPhraseStateByMessageId(uuid, messageStatus) }
    }

    val lastConsultPhrase: Single<ConsultPhrase?> =
        Single.fromCallable { tryExecute { myOpenHelper.getLastConsultPhrase() } }
            .subscribeOn(Schedulers.io())

    fun setAllConsultMessagesWereRead(): Completable {
        return Completable.fromCallable { tryExecute { myOpenHelper.setAllConsultMessagesWereRead() } }
            .subscribeOn(Schedulers.io())
    }

    fun setAllConsultMessagesWereReadInThread(threadId: Long?): Completable {
        return Completable.fromCallable { tryExecute { myOpenHelper.setAllConsultMessagesWereReadWithThreadId(threadId) } }
            .subscribeOn(Schedulers.io())
    }

    fun setMessageWasRead(uuid: String?) {
        uuid?.let { tryExecute { myOpenHelper.setMessageWasRead(it) } }
    }

    fun saveSpeechMessageUpdate(speechMessageUpdate: SpeechMessageUpdate?) {
        tryExecute { myOpenHelper.speechMessageUpdated(speechMessageUpdate) }
    }

    fun setNotSentSurveyDisplayMessageToFalse(): Completable {
        return Completable.fromCallable { tryExecute { myOpenHelper.setNotSentSurveyDisplayMessageToFalse() } }
            .subscribeOn(Schedulers.io())
    }

    fun setOldRequestResolveThreadDisplayMessageToFalse(): Completable {
        return Completable.fromCallable { tryExecute { myOpenHelper.setOldRequestResolveThreadDisplayMessageToFalse() } }
            .subscribeOn(Schedulers.io())
    }

    fun getMessagesCount(): Int = tryExecute { myOpenHelper.getMessagesCount() } ?: 0

    fun getUnreadMessagesCount(): Int = tryExecute { myOpenHelper.getUnreadMessagesCount() } ?: 0

    fun getUnreadMessagesUuid(): List<String?> = tryExecute { myOpenHelper.getUnreadMessagesUuid() } ?: arrayListOf()

    private fun checkIsDatabaseCorrupted() {
        myOpenHelper = ThreadsDbHelper.getInstance(context)
    }

    private fun checkAndUpdate() {
        val oldHelper = im.threads.business.database.ThreadsDbHelper(context)
        if (needMigrateToNewDB(oldHelper)) {
            putChatItems(oldHelper.getChatItems(0, -1))
            myOpenHelper.putFileDescriptions(oldHelper.allFileDescriptions)
            oldHelper.cleanDatabase()
        }
    }

    private fun needMigrateToNewDB(helper: im.threads.business.database.ThreadsDbHelper): Boolean {
        return try {
            helper.getChatItems(0, -1).size > 0 || helper.allFileDescriptions.size > 0
        } catch (exc: SQLiteDiskIOException) {
            Balloon.show(context, context.getString(R.string.ecc_not_enough_space))
            false
        }
    }

    private fun <T> tryExecute(block: () -> T?): T? {
        return try {
            block()
        } catch (exc: Exception) {
            checkIsDatabaseCorrupted()
            block()
        }
    }
}
