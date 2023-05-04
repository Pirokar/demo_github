package im.threads.business.database.table

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

abstract class Table {
    abstract fun createTable(db: SQLiteDatabase)
    abstract fun upgradeTable(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    abstract fun cleanTable(sqlHelper: SQLiteOpenHelper)

    companion object {
        fun cIsNull(c: Cursor, columnName: String?): Boolean {
            val index = c.getColumnIndex(columnName)
            return index < 0 || c.isNull(index)
        }

        fun cGetBool(c: Cursor, columnName: String?): Boolean {
            return cGetInt(c, columnName) == 1
        }

        fun cGetString(c: Cursor, columnName: String?): String? {
            val index = c.getColumnIndex(columnName)
            return if (index < 0 || cIsNull(c, columnName)) {
                null
            } else {
                c.getString(index)
            }
        }

        fun cGetLong(c: Cursor, columnName: String?): Long {
            val index = c.getColumnIndex(columnName)
            return if (index < 0) {
                0
            } else {
                c.getLong(index)
            }
        }

        fun cGetInt(c: Cursor, columnName: String?): Int {
            val index = c.getColumnIndex(columnName)
            return if (index < 0 || cIsNull(c, columnName)) {
                0
            } else {
                c.getInt(index)
            }
        }
    }
}
