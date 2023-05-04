package im.threads.business.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import im.threads.business.database.table.FileDescriptionsTable
import im.threads.business.database.table.MessagesTable
import im.threads.business.database.table.QuestionsTable
import im.threads.business.database.table.QuickRepliesTable
import im.threads.business.database.table.QuotesTable
import im.threads.business.models.ChatItem
import im.threads.business.models.FileDescription

/**
 * обертка для БД
 */
class ThreadsDbHelper(context: Context?) : SQLiteOpenHelper(context, "messages.db", null, VERSION), DBHelper {
    private lateinit var quotesTable: QuotesTable
    private lateinit var quickRepliesTable: QuickRepliesTable
    private lateinit var fileDescriptionTable: FileDescriptionsTable
    private lateinit var questionsTable: QuestionsTable
    private lateinit var messagesTable: MessagesTable
    private var isTablesPrepared = false

    init {
        initTables()
    }

    override fun onCreate(db: SQLiteDatabase) {
        initTables()
        messagesTable.createTable(db)
        quotesTable.createTable(db)
        quickRepliesTable.createTable(db)
        fileDescriptionTable.createTable(db)
        questionsTable.createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        initTables()
        if (oldVersion < VERSION) {
            messagesTable.upgradeTable(db, oldVersion, newVersion)
            quotesTable.upgradeTable(db, oldVersion, newVersion)
            quickRepliesTable.upgradeTable(db, oldVersion, newVersion)
            fileDescriptionTable.upgradeTable(db, oldVersion, newVersion)
            questionsTable.upgradeTable(db, oldVersion, newVersion)
            onCreate(db)
        }
        // VERSION 6 - quotes table uuid added, column names changed
        // dropping data with old file paths starting with "file://" prefix
    }

    override fun cleanDatabase() {
        fileDescriptionTable.cleanTable(this)
        messagesTable.cleanTable(this)
        quotesTable.cleanTable(this)
        quickRepliesTable.cleanTable(this)
        questionsTable.cleanTable(this)
    }

    private fun initTables() {
        if (!isTablesPrepared) {
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
            isTablesPrepared = true
        }
    }

    override fun getChatItems(offset: Int, limit: Int): List<ChatItem> {
        return messagesTable.getChatItems(this, offset, limit)
    }

    override val allFileDescriptions: List<FileDescription>
        get() = fileDescriptionTable.getAllFileDescriptions(this)

    companion object {
        private const val VERSION = 16
    }
}
