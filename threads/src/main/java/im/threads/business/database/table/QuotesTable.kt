package im.threads.business.database.table

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import im.threads.business.models.Quote
import im.threads.business.models.enums.ModificationStateEnum
import java.util.Locale

class QuotesTable(private val fileDescriptionsTable: FileDescriptionsTable) : Table() {
    override fun createTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE " + TABLE_QUOTE + "(" +
                COLUMN_QUOTE_UUID + " text," +
                COLUMN_QUOTE_FROM + " text, " +
                COLUMN_QUOTE_BODY + " text, " +
                COLUMN_QUOTE_TIMESTAMP + " integer, " +
                COLUMN_QUOTE_IS_PERSONAL_OFFER + " integer, " +
                COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " integer)" // message id
        )
    }

    override fun upgradeTable(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUOTE")
    }

    override fun cleanTable(sqlHelper: SQLiteOpenHelper) {
        sqlHelper.writableDatabase.execSQL("delete from $TABLE_QUOTE")
    }

    fun getQuote(sqlHelper: SQLiteOpenHelper, quotedByMessageUuid: String?): Quote? {
        if (TextUtils.isEmpty(quotedByMessageUuid)) {
            return null
        }
        val query = String.format(
            Locale.US,
            "select * from %s where %s = ?",
            TABLE_QUOTE,
            COLUMN_QUOTED_BY_MESSAGE_UUID_EXT
        )
        sqlHelper.writableDatabase.rawQuery(query, arrayOf(quotedByMessageUuid)).use { c ->
            if (c.moveToFirst()) {
                return Quote(
                    cGetString(c, COLUMN_QUOTE_UUID),
                    cGetString(c, COLUMN_QUOTE_FROM),
                    cGetString(c, COLUMN_QUOTE_BODY),
                    fileDescriptionsTable.getFileDescription(
                        sqlHelper,
                        cGetString(c, COLUMN_QUOTE_UUID)
                    ),
                    cGetLong(c, COLUMN_QUOTE_TIMESTAMP),
                    cGetBool(c, COLUMN_QUOTE_IS_PERSONAL_OFFER),
                    ModificationStateEnum.fromString(
                        cGetString(
                            c,
                            COLUMN_MODIFICATION_STATE
                        )
                    )
                )
            }
        }
        return null
    }

    companion object {
        private const val TABLE_QUOTE = "TABLE_QUOTE"
        private const val COLUMN_QUOTE_UUID = "COLUMN_QUOTE_UUID"
        private const val COLUMN_QUOTE_FROM = "COLUMN_QUOTE_HEADER"
        private const val COLUMN_QUOTE_BODY = "COLUMN_QUOTE_BODY"
        private const val COLUMN_QUOTE_TIMESTAMP = "COLUMN_QUOTE_TIMESTAMP"
        private const val COLUMN_QUOTE_IS_PERSONAL_OFFER = "COLUMN_QUOTE_IS_PERSONAL_OFFER"
        private const val COLUMN_QUOTED_BY_MESSAGE_UUID_EXT = "COLUMN_QUOTED_BY_MESSAGE_UUID_EXT"
        private const val COLUMN_MODIFICATION_STATE = "COLUMN_MODIFICATION_STATE"
    }
}
