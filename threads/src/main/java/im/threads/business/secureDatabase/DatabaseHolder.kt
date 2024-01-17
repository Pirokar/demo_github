package im.threads.business.secureDatabase

import android.content.Context
import android.database.sqlite.SQLiteDiskIOException
import im.threads.R
import im.threads.business.annotation.OpenForTesting
import im.threads.business.extensions.mutableLazy
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultInfo
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.utils.Balloon
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OpenForTesting
class DatabaseHolder(private val context: Context) {
    private var myOpenHelper = mutableLazy { ThreadsDbHelper.getInstance(context) }

    fun cleanDatabase() = tryExecute {
        myOpenHelper.value.cleanDatabase()
    }

    fun closeInstance() = tryExecute {
        myOpenHelper.value.closeInstance()
    }

    fun getChatItems(offset: Int, limit: Int): List<ChatItem> {
        return tryExecute {
            val items = myOpenHelper.value.getChatItems(offset, limit).toMutableList()
            myOpenHelper.reset()
            val surveysWithQuestions = mutableListOf<Survey>()
            items.filterIsInstance<Survey>().forEach { survey ->
                (myOpenHelper.value.getChatItemByCorrelationId(survey.uuid) as? Survey)?.let { surveyWithQuestions ->
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

    fun getSendingChatItems(): List<UserPhrase> = tryExecute { myOpenHelper.value.getSendingChatItems() } ?: arrayListOf()

    fun getNotDeliveredChatItems(): List<UserPhrase> = tryExecute { myOpenHelper.value.getNotDeliveredChatItems() } ?: arrayListOf()

    fun getChatItemByCorrelationId(messageUuid: String?): ChatItem? =
        tryExecute { myOpenHelper.value.getChatItemByCorrelationId(messageUuid) }

    fun getChatItemByBackendMessageId(messageId: String?): ChatItem? =
        tryExecute { myOpenHelper.value.getChatItemByBackendMessageId(messageId) }

    fun putChatItems(items: List<ChatItem?>?) = tryExecute { myOpenHelper.value.putChatItems(items) }

    fun putChatItem(chatItem: ChatItem?): Boolean = tryExecute { myOpenHelper.value.putChatItem(chatItem) } ?: false

    // FileDescriptions
    fun getAllFileDescriptions(): Single<List<FileDescription?>?> =
        Single.fromCallable { tryExecute { myOpenHelper.value.getAllFileDescriptions() } }
            .subscribeOn(Schedulers.io())

    // UserPhrase
    fun updateFileDescription(fileDescription: FileDescription) {
        tryExecute { myOpenHelper.value.updateFileDescription(fileDescription) }
    }

    fun updateChatItemByTimeStamp(chatItem: ChatItem) {
        tryExecute { myOpenHelper.value.updateChatItemByTimeStamp(chatItem) }
    }

    fun getConsultInfo(id: String): ConsultInfo? = tryExecute { myOpenHelper.value.getLastConsultInfo(id) }

    fun getUnsentUserPhrase(count: Int): List<UserPhrase> = tryExecute { myOpenHelper.value.getUnsendUserPhrase(count) } ?: arrayListOf()

    fun setStateOfUserPhraseByCorrelationId(uuid: String?, messageStatus: MessageStatus?) {
        tryExecute { myOpenHelper.value.setUserPhraseStateByCorrelationId(uuid, messageStatus) }
    }

    fun setStateOfUserPhraseByBackendMessageId(messageId: String?, messageStatus: MessageStatus?) {
        tryExecute { myOpenHelper.value.setUserPhraseStateByBackendMessageId(messageId, messageStatus) }
    }

    fun setAllConsultMessagesWereRead(): Completable {
        return Completable.fromCallable { tryExecute { myOpenHelper.value.setAllConsultMessagesWereRead() } }
            .subscribeOn(Schedulers.io())
    }

    fun setAllConsultMessagesWereReadInThread(threadId: Long?): Completable {
        return Completable.fromCallable { tryExecute { myOpenHelper.value.setAllConsultMessagesWereReadWithThreadId(threadId) } }
            .subscribeOn(Schedulers.io())
    }

    fun setMessageWasRead(uuid: String?) {
        uuid?.let { tryExecute { myOpenHelper.value.setMessageWasRead(it) } }
    }

    fun saveSpeechMessageUpdate(speechMessageUpdate: SpeechMessageUpdate?) {
        tryExecute { myOpenHelper.value.speechMessageUpdated(speechMessageUpdate) }
    }

    fun setNotSentSurveyDisplayMessageToFalse(): Completable {
        return Completable.fromCallable { tryExecute { myOpenHelper.value.setNotSentSurveyDisplayMessageToFalse() } }
            .subscribeOn(Schedulers.io())
    }

    fun setOldRequestResolveThreadDisplayMessageToFalse(): Completable {
        return Completable.fromCallable { tryExecute { myOpenHelper.value.setOldRequestResolveThreadDisplayMessageToFalse() } }
            .subscribeOn(Schedulers.io())
    }

    fun getMessagesCount(): Int = tryExecute { myOpenHelper.value.getMessagesCount() } ?: 0

    fun getUnreadMessagesCount(): Int = tryExecute { myOpenHelper.value.getUnreadMessagesCount() } ?: 0

    fun getUnreadMessagesUuid(): List<String?> = tryExecute { myOpenHelper.value.getUnreadMessagesUuid() } ?: arrayListOf()

    fun setOrUpdateMessageId(correlationId: String?, backendMessageId: String?) {
        tryExecute { myOpenHelper.value.setOrUpdateMessageId(correlationId, backendMessageId) }
    }

    fun removeItem(correlationId: String?, messageId: String?) = tryExecute {
        myOpenHelper.value.removeItem(correlationId, messageId)
    }

    private fun checkAndUpdate() {
        val oldHelper = im.threads.business.database.ThreadsDbHelper(context)
        if (needMigrateToNewDB(oldHelper)) {
            putChatItems(oldHelper.getChatItems(0, -1))
            myOpenHelper.value.putFileDescriptions(oldHelper.allFileDescriptions)
            oldHelper.cleanDatabase()
        }
    }

    private fun needMigrateToNewDB(helper: im.threads.business.database.ThreadsDbHelper): Boolean {
        return try {
            helper.getChatItems(0, -1).isNotEmpty() || helper.allFileDescriptions.isNotEmpty()
        } catch (exc: SQLiteDiskIOException) {
            CoroutineScope(Dispatchers.Main).launch {
                Balloon.show(context, context.getString(R.string.ecc_not_enough_space))
            }
            false
        }
    }

    private fun <T> tryExecute(block: () -> T?): T? {
        return try {
            block()
        } catch (exc: Exception) {
            try {
                checkAndUpdate()
                block()
            } catch (cantOpenExc: android.database.sqlite.SQLiteCantOpenDatabaseException) {
                try {
                    myOpenHelper.reset()
                    block()
                } catch (anyExc: Exception) {
                    LoggerEdna.error("Processed error when reading database for block: \"${block.javaClass}\"", exc)
                    null
                }
            } catch (anyExc: Exception) {
                LoggerEdna.error("Processed error when reading database for block: \"${block.javaClass}\"", exc)
                null
            }
        }
    }
}
