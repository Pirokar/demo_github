package im.threads.business.secureDatabase.table

import android.content.ContentValues
import im.threads.business.models.Quote
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import java.util.Locale

class QuotesTable(private val fileDescriptionsTable: FileDescriptionsTable) : Table() {

    override fun createTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE " + TABLE_QUOTE + "(" +
                COLUMN_QUOTE_UUID + " text," +
                COLUMN_QUOTE_FROM + " text, " +
                COLUMN_QUOTE_BODY + " text, " +
                COLUMN_QUOTE_TIMESTAMP + " integer, " +
                COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " integer)" // message id
        )
    }

    override fun upgradeTable(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUOTE")
    }

    override fun cleanTable(sqlHelper: SQLiteOpenHelper) {
        sqlHelper.writableDatabase.execSQL("delete from $TABLE_QUOTE")
    }

    fun putQuote(sqlHelper: SQLiteOpenHelper, quotedByMessageUuid: String, quote: Quote?) {
        quote?.let {
            val cv = ContentValues()
            cv.clear()
            cv.put(COLUMN_QUOTE_UUID, quote.uuid)
            cv.put(COLUMN_QUOTED_BY_MESSAGE_UUID_EXT, quotedByMessageUuid)
            cv.put(COLUMN_QUOTE_FROM, quote.phraseOwnerTitle)
            cv.put(COLUMN_QUOTE_BODY, quote.text)
            cv.put(COLUMN_QUOTE_TIMESTAMP, quote.timeStamp)
            sqlHelper.writableDatabase.rawQuery(
                (
                    "select " + COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " from " + TABLE_QUOTE +
                        " where " + COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " = ?"
                    ),
                arrayOf(quotedByMessageUuid)
            ).use { c ->
                val existsInDb: Boolean = c.count > 0
                if (existsInDb) {
                    sqlHelper.writableDatabase
                        .update(
                            TABLE_QUOTE,
                            cv,
                            "$COLUMN_QUOTED_BY_MESSAGE_UUID_EXT = ? ",
                            arrayOf(quotedByMessageUuid)
                        )
                } else {
                    sqlHelper.writableDatabase
                        .insert(TABLE_QUOTE, null, cv)
                }
                it.fileDescription?.let {
                    quote.uuid?.let { uuid ->
                        fileDescriptionsTable.putFileDescription(sqlHelper, it, uuid, true)
                    }
                }
            }
        }
    }

    fun getQuote(sqlHelper: SQLiteOpenHelper, quotedByMessageUuid: String?): Quote? {
        if (quotedByMessageUuid.isNullOrEmpty()) {
            return null
        }
        val query = String.format(
            Locale.US,
            "select * from %s where %s = ?",
            TABLE_QUOTE,
            COLUMN_QUOTED_BY_MESSAGE_UUID_EXT
        )
        sqlHelper.writableDatabase.rawQuery(query, arrayOf(quotedByMessageUuid))
            .use { c ->
                if (c.moveToFirst()) {
                    return Quote(
                        cursorGetString(c, COLUMN_QUOTE_UUID),
                        cursorGetString(c, COLUMN_QUOTE_FROM),
                        cursorGetString(c, COLUMN_QUOTE_BODY),
                        fileDescriptionsTable.getFileDescription(
                            sqlHelper,
                            cursorGetString(c, COLUMN_QUOTE_UUID)
                        ),
                        cursorGetLong(c, COLUMN_QUOTE_TIMESTAMP)
                    )
                }
            }
        return null
    }

    fun getQuotes(sqlHelper: SQLiteOpenHelper): List<Quote> {
        val query = String.format(
            Locale.US,
            "select * from %s order by %s desc",
            TABLE_QUOTE,
            COLUMN_QUOTE_TIMESTAMP
        )
        val list: MutableList<Quote> = ArrayList()
        sqlHelper.writableDatabase.rawQuery(query, arrayOf()).use { c ->
            if (!c.moveToFirst()) {
                return list
            }
            c.moveToFirst()
            while (!c.isAfterLast) {
                list.add(
                    Quote(
                        cursorGetString(c, COLUMN_QUOTE_UUID),
                        cursorGetString(c, COLUMN_QUOTE_FROM),
                        cursorGetString(c, COLUMN_QUOTE_BODY),
                        fileDescriptionsTable.getFileDescription(
                            sqlHelper,
                            cursorGetString(c, COLUMN_QUOTE_UUID)
                        ),
                        cursorGetLong(c, COLUMN_QUOTE_TIMESTAMP)
                    )
                )
                c.moveToNext()
            }
            return list
        }
    }
}

private const val TABLE_QUOTE = "TABLE_QUOTE"
private const val COLUMN_QUOTE_UUID = "COLUMN_QUOTE_UUID"
private const val COLUMN_QUOTE_FROM = "COLUMN_QUOTE_HEADER"
private const val COLUMN_QUOTE_BODY = "COLUMN_QUOTE_BODY"
private const val COLUMN_QUOTE_TIMESTAMP = "COLUMN_QUOTE_TIMESTAMP"
private const val COLUMN_QUOTED_BY_MESSAGE_UUID_EXT = "COLUMN_QUOTED_BY_MESSAGE_UUID_EXT"
