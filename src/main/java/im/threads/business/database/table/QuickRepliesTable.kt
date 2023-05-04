package im.threads.business.database.table

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import im.threads.business.models.QuickReply

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
        sqlHelper.writableDatabase.execSQL("delete from $TABLE_QUICK_REPLIES")
    }

    fun getQuickReplies(sqlHelper: SQLiteOpenHelper, messageUUID: String?): List<QuickReply> {
        val items: MutableList<QuickReply> = ArrayList()
        val query = "select * from $TABLE_QUICK_REPLIES where $COLUMN_QUICK_REPLIES_MESSAGE_UUID = ?"
        sqlHelper.writableDatabase.rawQuery(query, arrayOf(messageUUID)).use { c ->
            if (c.count == 0) {
                return items
            }
            c.moveToFirst()
            while (!c.isAfterLast) {
                val quickReply = QuickReply()
                quickReply.id = cGetInt(
                    c,
                    COLUMN_QUICK_REPLIES_SERVER_ID
                )
                quickReply.type = cGetString(
                    c,
                    COLUMN_QUICK_REPLIES_TYPE
                )
                quickReply.text = cGetString(
                    c,
                    COLUMN_QUICK_REPLIES_TEXT
                )
                quickReply.imageUrl = cGetString(
                    c,
                    COLUMN_QUICK_REPLIES_IMAGE_URL
                )
                quickReply.url = cGetString(
                    c,
                    COLUMN_QUICK_REPLIES_URL
                )
                items.add(quickReply)
                c.moveToNext()
            }
            return items
        }
    }

    companion object {
        private const val TABLE_QUICK_REPLIES = "TABLE_QUICK_REPLIES"
        private const val COLUMN_QUICK_REPLIES_ID = "COLUMN_ID"
        private const val COLUMN_QUICK_REPLIES_SERVER_ID = "COLUMN_SERVER_ID"
        private const val COLUMN_QUICK_REPLIES_MESSAGE_UUID = "COLUMN_MESSAGE_UUID"
        private const val COLUMN_QUICK_REPLIES_TYPE = "COLUMN_TYPE"
        private const val COLUMN_QUICK_REPLIES_TEXT = "COLUMN_TEXT"
        private const val COLUMN_QUICK_REPLIES_IMAGE_URL = "COLUMN_IMAGE_URL"
        private const val COLUMN_QUICK_REPLIES_URL = "COLUMN_URL"
    }
}
