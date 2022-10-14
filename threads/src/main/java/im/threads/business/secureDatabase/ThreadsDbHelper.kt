package im.threads.business.secureDatabase

import android.content.Context
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultInfo
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.FileDescription
import im.threads.business.models.MessageState
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

    override fun getChatItem(messageUuid: String?): ChatItem? =
        messagesTable.getChatItem(this, messageUuid)

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

    override fun updateFileDescription(fileDescription: FileDescription) {
        fileDescriptionTable.updateFileDescriptionByName(this, fileDescription)
    }

    override fun getLastConsultInfo(id: String): ConsultInfo? =
        messagesTable.getLastConsultInfo(this, id)

    override fun getUnsendUserPhrase(count: Int): List<UserPhrase> =
        messagesTable.getUnsendUserPhrase(this, count)

    override fun setUserPhraseStateByProviderId(providerId: String?, messageState: MessageState?) {
        messagesTable.setUserPhraseStateByProviderId(this, providerId, messageState)
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

    override fun setMessageWasRead(providerId: String) {
        messagesTable.setMessageWasRead(this, providerId)
    }

    override fun getSurvey(sendingId: Long): Survey? = messagesTable.getSurvey(this, sendingId)

    override fun setNotSentSurveyDisplayMessageToFalse(): Int =
        messagesTable.setNotSentSurveyDisplayMessageToFalse(this)

    override fun setOldRequestResolveThreadDisplayMessageToFalse(): Int =
        messagesTable.setOldRequestResolveThreadDisplayMessageToFalse(this)

    override fun getMessagesCount(): Int = messagesTable.getMessagesCount(this)

    override fun getUnreadMessagesCount(): Int = messagesTable.getUnreadMessagesCount(this)

    override fun getUnreadMessagesUuid(): List<String?> = messagesTable.getUnreadMessagesUuid(this)

    fun speechMessageUpdated(speechMessageUpdate: SpeechMessageUpdate?) {
        messagesTable.speechMessageUpdated(this, speechMessageUpdate!!)
    }

    companion object {
        private const val DATABASE_NAME = "messages_secure.db"
        private const val VERSION = 2
        private var isLibraryLoaded = false
        private const val oldPassword = "password"
        const val DB_PASSWORD = "CdgF9rEjzaes8G"

        fun getInstance(context: Context): ThreadsDbHelper {
            migratePassword(context)
            return ThreadsDbHelper(context, DB_PASSWORD)
        }

        private fun migratePassword(context: Context) {
            val preferences = Preferences(context)
            if (preferences.get<Boolean>(PreferencesCoreKeys.IS_DATABASE_PASSWORD_MIGRATED) != true) {
                val oldDatabase = ThreadsDbHelper(context, oldPassword)
                oldDatabase.writableDatabase.rawQuery("PRAGMA rekey = '$DB_PASSWORD'")
                oldDatabase.close()
                preferences.save(PreferencesCoreKeys.IS_DATABASE_PASSWORD_MIGRATED, true)
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
