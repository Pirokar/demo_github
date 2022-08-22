package im.threads.internal.secureDatabase

import android.content.Context
import im.threads.business.models.ChatItem
import im.threads.internal.model.ConsultInfo
import im.threads.internal.model.ConsultPhrase
import im.threads.internal.model.FileDescription
import im.threads.internal.model.MessageState
import im.threads.internal.model.SpeechMessageUpdate
import im.threads.internal.model.Survey
import im.threads.internal.model.UserPhrase
import im.threads.internal.secureDatabase.table.FileDescriptionsTable
import im.threads.internal.secureDatabase.table.MessagesTable
import im.threads.internal.secureDatabase.table.QuestionsTable
import im.threads.internal.secureDatabase.table.QuickRepliesTable
import im.threads.internal.secureDatabase.table.QuotesTable
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

class ThreadsDbHelper(val context: Context) :
    SQLiteOpenHelper(context, "messages_secure.db", null, VERSION),
    DBHelper {

    private var quotesTable: QuotesTable
    private var quickRepliesTable: QuickRepliesTable
    private var fileDescriptionTable: FileDescriptionsTable
    private var questionsTable: QuestionsTable
    private var messagesTable: MessagesTable

    init {
        SQLiteDatabase.loadLibs(context)
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
        private const val VERSION = 2
        const val DB_PASSWORD = "password"
    }
}
