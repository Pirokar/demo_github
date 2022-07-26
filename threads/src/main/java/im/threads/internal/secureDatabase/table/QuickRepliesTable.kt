package im.threads.internal.secureDatabase.table

import android.content.ContentValues
import im.threads.internal.domain.logger.LoggerEdna
import im.threads.internal.model.QuickReply
import im.threads.internal.secureDatabase.ThreadsDbHelper.Companion.DB_PASSWORD
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

class QuickRepliesTable : Table() {
    override fun createTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE " + TABLE_QUICK_REPLIES + "(" +
                COLUMN_QUICK_REPLIES_ID + " integer primary key autoincrement, " +
                COLUMN_QUICK_REPLIES_SERVER_ID + " integer, " +
                COLUMN_QUICK_REPLIES_MESSAGE_UUID + " string, " +
                COLUMN_QUICK_REPLIES_TYPE + " text, " +
                COLUMN_QUICK_REPLIES_TEXT + " text, " +
                COLUMN_QUICK_REPLIES_IMAGE_URL + " text, " +
                COLUMN_QUICK_REPLIES_URL + " text " +
                ")"
        )
    }

    override fun upgradeTable(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUICK_REPLIES")
    }

    override fun cleanTable(sqlHelper: SQLiteOpenHelper) {
        sqlHelper.getWritableDatabase(DB_PASSWORD).execSQL("delete from $TABLE_QUICK_REPLIES")
    }

    fun getQuickReplies(sqlHelper: SQLiteOpenHelper, messageUUID: String): List<QuickReply> {
        val items: MutableList<QuickReply> = ArrayList()
        val query =
            "select * from $TABLE_QUICK_REPLIES where $COLUMN_QUICK_REPLIES_MESSAGE_UUID = ?"
        sqlHelper.getWritableDatabase(DB_PASSWORD).rawQuery(
            query,
            arrayOf(messageUUID)
        ).use { c ->
            if (c.count == 0) {
                return items
            }
            c.moveToFirst()
            while (!c.isAfterLast) {
                val quickReply = QuickReply()
                quickReply.id = cursorGetInt(c, COLUMN_QUICK_REPLIES_SERVER_ID)
                quickReply.type = cursorGetString(c, COLUMN_QUICK_REPLIES_TYPE)
                quickReply.text = cursorGetString(c, COLUMN_QUICK_REPLIES_TEXT)
                quickReply.imageUrl = cursorGetString(c, COLUMN_QUICK_REPLIES_IMAGE_URL)
                quickReply.url = cursorGetString(c, COLUMN_QUICK_REPLIES_URL)
                items.add(quickReply)
                c.moveToNext()
            }
            return items
        }
    }

    fun putQuickReplies(
        sqlHelper: SQLiteOpenHelper,
        messageUUID: String,
        quickReplies: List<QuickReply>
    ) {
        try {
            sqlHelper.getWritableDatabase(DB_PASSWORD).beginTransaction()
            val deleteQuickRepliesSql = (
                "delete from " + TABLE_QUICK_REPLIES +
                    " where " + COLUMN_QUICK_REPLIES_MESSAGE_UUID + " = ? "
                )
            sqlHelper.getWritableDatabase(DB_PASSWORD).execSQL(
                deleteQuickRepliesSql,
                arrayOf(messageUUID)
            )
            for (item: QuickReply in quickReplies) {
                putQuickReply(sqlHelper, messageUUID, item)
            }
            sqlHelper.getWritableDatabase(DB_PASSWORD).setTransactionSuccessful()
        } catch (e: Exception) {
            LoggerEdna.e("putQuickReplies", e)
        } finally {
            sqlHelper.getWritableDatabase(DB_PASSWORD).endTransaction()
        }
    }

    private fun putQuickReply(
        sqlHelper: SQLiteOpenHelper,
        messageUUID: String,
        quickReply: QuickReply
    ) {
        val quickReplyCv = ContentValues()
        quickReplyCv.put(COLUMN_QUICK_REPLIES_SERVER_ID, quickReply.id)
        quickReplyCv.put(COLUMN_QUICK_REPLIES_MESSAGE_UUID, messageUUID)
        quickReplyCv.put(COLUMN_QUICK_REPLIES_TYPE, quickReply.type)
        quickReplyCv.put(COLUMN_QUICK_REPLIES_TEXT, quickReply.text)
        quickReplyCv.put(COLUMN_QUICK_REPLIES_IMAGE_URL, quickReply.imageUrl)
        quickReplyCv.put(COLUMN_QUICK_REPLIES_URL, quickReply.url)
        sqlHelper.getWritableDatabase(DB_PASSWORD).insert(TABLE_QUICK_REPLIES, null, quickReplyCv)
    }
}

private const val TABLE_QUICK_REPLIES = "TABLE_QUICK_REPLIES"
private const val COLUMN_QUICK_REPLIES_ID = "COLUMN_ID"
private const val COLUMN_QUICK_REPLIES_SERVER_ID = "COLUMN_SERVER_ID"
private const val COLUMN_QUICK_REPLIES_MESSAGE_UUID = "COLUMN_MESSAGE_UUID"
private const val COLUMN_QUICK_REPLIES_TYPE = "COLUMN_TYPE"
private const val COLUMN_QUICK_REPLIES_TEXT = "COLUMN_TEXT"
private const val COLUMN_QUICK_REPLIES_IMAGE_URL = "COLUMN_IMAGE_URL"
private const val COLUMN_QUICK_REPLIES_URL = "COLUMN_URL"
