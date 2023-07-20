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
import im.threads.business.models.Survey
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

    fun getChatItems(offset: Int, limit: Int): List<ChatItem> {
        return tryExecute {
            val items = myOpenHelper.getChatItems(offset, limit).toMutableList()
            val surveysWithQuestions = mutableListOf<Survey>()
            items.filterIsInstance<Survey>().forEach { survey ->
                (myOpenHelper.getChatItemByCorrelationId(survey.uuid) as? Survey)?.let { surveyWithQuestions ->
                    surveysWithQuestions.add(surveyWithQuestions)
                }
            }
            surveysWithQuestions.forEach { surveyWithQuestions ->
                val indexOfSurvey = items.indexOfFirst { it is Survey && it.sendingId == surveyWithQuestions.sendingId }
                if (indexOfSurvey >= 0) {
                    items[indexOfSurvey] = surveyWithQuestions
                }
            }
            items
        } ?: arrayListOf()
    }

    fun getSendingChatItems(): List<UserPhrase> = tryExecute { myOpenHelper.getSendingChatItems() } ?: arrayListOf()

    fun getNotDeliveredChatItems(): List<UserPhrase> = tryExecute { myOpenHelper.getNotDeliveredChatItems() } ?: arrayListOf()

    fun getChatItemByCorrelationId(messageUuid: String?): ChatItem? =
        tryExecute { myOpenHelper.getChatItemByCorrelationId(messageUuid) }

    fun getChatItemByBackendMessageId(messageId: String?): ChatItem? =
        tryExecute { myOpenHelper.getChatItemByBackendMessageId(messageId) }

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

    fun setStateOfUserPhraseByCorrelationId(uuid: String?, messageStatus: MessageStatus?) {
        tryExecute { myOpenHelper.setUserPhraseStateByCorrelationId(uuid, messageStatus) }
    }

    fun setStateOfUserPhraseByBackendMessageId(messageId: String?, messageStatus: MessageStatus?) {
        tryExecute { myOpenHelper.setUserPhraseStateByBackendMessageId(messageId, messageStatus) }
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

    fun setOrUpdateMessageId(correlationId: String?, backendMessageId: String?) {
        tryExecute { myOpenHelper.setOrUpdateMessageId(correlationId, backendMessageId) }
    }

    fun removeItem(correlationId: String?, messageId: String?) = tryExecute {
        myOpenHelper.removeItem(correlationId, messageId)
    }

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