package im.threads.business.secureDatabase.table

import android.annotation.SuppressLint
import android.database.Cursor
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

abstract class Table {

    abstract fun createTable(db: SQLiteDatabase)
    abstract fun upgradeTable(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    abstract fun cleanTable(sqlHelper: SQLiteOpenHelper)

    companion object {
        @SuppressLint("Range")
        fun isCursorNull(c: Cursor, columnName: String) = c.isNull(c.getColumnIndex(columnName))

        fun cursorGetBool(c: Cursor, columnName: String): Boolean {
            return cursorGetInt(c, columnName) == 1
        }

        @SuppressLint("Range")
        fun cursorGetString(c: Cursor, columnName: String): String? {
            return if (isCursorNull(c, columnName)) null else c.getString(c.getColumnIndex(columnName))
        }

        @SuppressLint("Range")
        fun cursorGetLong(c: Cursor, columnName: String): Long {
            return c.getLong(c.getColumnIndex(columnName))
        }

        @SuppressLint("Range")
        fun cursorGetInt(c: Cursor, columnName: String): Int {
            return if (isCursorNull(c, columnName)) 0 else c.getInt(c.getColumnIndex(columnName))
        }
    }
}
