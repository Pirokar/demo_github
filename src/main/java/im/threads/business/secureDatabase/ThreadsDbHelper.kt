package im.threads.business.secureDatabase

import android.annotation.SuppressLint
import android.content.Context
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultInfo
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageStatus
import im.threads.business.models.SpeechMessageUpdate
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesCoreKeys
import im.threads.business.secureDatabase.table.FileDescriptionsTable
import im.threads.business.secureDatabase.table.MessagesTable
import im.threads.business.secureDatabase.table.QuestionsTable
import im.threads.business.secureDatabase.table.QuickRepliesTable
import im.threads.business.secureDatabase.table.QuotesTable
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import java.util.UUID

class ThreadsDbHelper private constructor(val context: Context, password: String) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        password,
        null,
        VERSION,
        0,
        null,
        null,
        true
    ),
    DBHelper {

    private var quotesTable: QuotesTable
    private var quickRepliesTable: QuickRepliesTable
    private var fileDescriptionTable: FileDescriptionsTable
    private var questionsTable: QuestionsTable
    private var messagesTable: MessagesTable

    init {
        loadLibrary()
        fileDescriptionTable = FileDescriptionsTable()
        questionsTable = QuestionsTable()
        quotesTable = QuotesTable(fileDescriptionTable)
        quickRepliesTable = QuickRepliesTable()
        messagesTable = MessagesTable(
            fileDescriptionTable,
            quotesTable,
            quickRepliesTable,
            questionsTable
        )
    }

    override fun onCreate(db: SQLiteDatabase) {
        messagesTable.createTable(db)
        quotesTable.createTable(db)
        quickRepliesTable.createTable(db)
        fileDescriptionTable.createTable(db)
        questionsTable.createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < VERSION) {
            messagesTable.upgradeTable(db, oldVersion, newVersion)
            quotesTable.upgradeTable(db, oldVersion, newVersion)
            quickRepliesTable.upgradeTable(db, oldVersion, newVersion)
            fileDescriptionTable.upgradeTable(db, oldVersion, newVersion)
            questionsTable.upgradeTable(db, oldVersion, newVersion)
            onCreate(db)
        }
    }

    override fun cleanDatabase() {
        fileDescriptionTable.cleanTable(this)
        messagesTable.cleanTable(this)
        quotesTable.cleanTable(this)
        quickRepliesTable.cleanTable(this)
        questionsTable.cleanTable(this)
    }

    override fun getChatItems(offset: Int, limit: Int): List<ChatItem> =
        messagesTable.getChatItems(this, offset, limit)

    override fun getSendingChatItems(): List<UserPhrase> =
        messagesTable.getSendingChatItems(this)

    override fun getNotDeliveredChatItems(): List<UserPhrase> =
        messagesTable.getNotDeliveredChatItems(this)

    override fun getChatItemByCorrelationId(messageUuid: String?): ChatItem? =
        messagesTable.getChatItemByCorrelationId(this, messageUuid)

    override fun getChatItemByBackendMessageId(messageId: String?): ChatItem? =
        messagesTable.getChatItemByBackendMessageId(this, messageId)

    override fun putChatItems(items: List<ChatItem?>?) {
        messagesTable.putChatItems(this, items)
    }

    override fun putChatItem(chatItem: ChatItem?): Boolean =
        messagesTable.putChatItem(this, chatItem)

    override fun getAllFileDescriptions(): List<FileDescription?> =
        fileDescriptionTable.getAllFileDescriptions(this)

    override fun putFileDescriptions(fileDescriptions: List<FileDescription?>) {
        fileDescriptionTable.putFileDescriptions(this, fileDescriptions)
    }

    override fun updateFileDescriptionByName(fileDescription: FileDescription) {
        fileDescriptionTable.updateFileDescriptionByName(this, fileDescription)
    }

    override fun updateFileDescriptionByUrl(fileDescription: FileDescription) {
        fileDescriptionTable.updateFileDescriptionByUrl(this, fileDescription)
    }

    override fun updateChatItemByTimeStamp(chatItem: ChatItem) {
        messagesTable.updateChatItemByTimeStamp(this, chatItem)
    }

    override fun getLastConsultInfo(id: String): ConsultInfo? =
        messagesTable.getLastConsultInfo(this, id)

    override fun getUnsendUserPhrase(count: Int): List<UserPhrase> =
        messagesTable.getUnsendUserPhrase(this, count)

    override fun setUserPhraseStateByCorrelationId(uuid: String?, messageStatus: MessageStatus?) {
        messagesTable.setUserPhraseStateByCorrelationId(this, uuid, messageStatus)
    }

    override fun setUserPhraseStateByBackendMessageId(messageId: String?, messageStatus: MessageStatus?) {
        messagesTable.setUserPhraseStateByBackendMessageId(this, messageId, messageStatus)
    }

    override fun getLastConsultPhrase(): ConsultPhrase? = messagesTable.getLastConsultPhrase(this)

    override fun setAllConsultMessagesWereRead(): Int {
        return messagesTable.setAllMessagesWereRead(this)
    }

    override fun setAllConsultMessagesWereReadWithThreadId(threadId: Long?): Int {
        return threadId?.let {
            messagesTable.setAllMessagesWereReadInThread(this, threadId)
        } ?: 0
    }

    override fun setMessageWasRead(uuid: String) {
        messagesTable.setMessageWasRead(this, uuid)
    }

    override fun getSurvey(sendingId: Long): Survey? = messagesTable.getSurvey(this, sendingId)

    override fun setNotSentSurveyDisplayMessageToFalse(): Int =
        messagesTable.setNotSentSurveyDisplayMessageToFalse(this)

    override fun setOldRequestResolveThreadDisplayMessageToFalse(): Int =
        messagesTable.setOldRequestResolveThreadDisplayMessageToFalse(this)

    override fun getMessagesCount(): Int = messagesTable.getMessagesCount(this)

    override fun getUnreadMessagesCount(): Int = messagesTable.getUnreadMessagesCount(this)

    override fun getUnreadMessagesUuid(): List<String?> = messagesTable.getUnreadMessagesUuid(this)

    override fun setOrUpdateMessageId(correlationId: String?, backendMessageId: String?) {
        messagesTable.setOrUpdateMessageId(this, correlationId, backendMessageId)
    }

    override fun removeItem(correlationId: String?, messageId: String?) {
        messagesTable.removeItem(this, correlationId, messageId)
    }

    fun speechMessageUpdated(speechMessageUpdate: SpeechMessageUpdate?) {
        messagesTable.speechMessageUpdated(this, speechMessageUpdate!!)
    }

    companion object {
        private const val DATABASE_NAME = "messages_secure.db"
        private const val VERSION = 4
        private var isLibraryLoaded = false

        @SuppressLint("StaticFieldLeak")
        private var dbInstance: ThreadsDbHelper? = null

        @Synchronized
        fun getInstance(context: Context): ThreadsDbHelper {
            val password = getDbPassword(context)
            if (dbInstance == null) {
                dbInstance = ThreadsDbHelper(context, password)
                if (!isDatabaseAlive(context)) {
                    dbInstance = ThreadsDbHelper(context, password)
                }
            }
            return dbInstance!!
        }

        @Synchronized
        internal fun recreateInstance(context: Context): ThreadsDbHelper {
            return if (dbInstance == null) {
                getInstance(context)
            } else {
                dbInstance?.close()
                dbInstance = null
                getInstance(context)
            }
        }

        private fun getDbPassword(context: Context): String {
            val preferences = Preferences(context)
            var securedPassword = preferences.get<String>(PreferencesCoreKeys.DATABASE_PASSWORD)
            if (securedPassword.isNullOrEmpty()) {
                securedPassword = UUID.randomUUID().toString()
                preferences.save(PreferencesCoreKeys.DATABASE_PASSWORD, securedPassword)
                context.deleteDatabase(DATABASE_NAME)
            }

            return securedPassword
        }

        private fun isDatabaseAlive(context: Context): Boolean {
            return try {
                val cursor = dbInstance?.readableDatabase?.rawQuery("SELECT * FROM ${QuotesTable.TABLE_QUOTE}")
                cursor != null
            } catch (exc: Exception) {
                LoggerEdna.error("Cannot read database. Database will be deleted", exc)
                context.deleteDatabase(DATABASE_NAME)
                false
            }
        }

        private fun loadLibrary() {
            if (!isLibraryLoaded) {
                System.loadLibrary("sqlcipher")
                isLibraryLoaded = true
            }
        }
    }
}
