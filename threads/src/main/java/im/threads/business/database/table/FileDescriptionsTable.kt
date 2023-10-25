package im.threads.business.database.table

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import im.threads.business.models.FileDescription
import im.threads.business.models.enums.AttachmentStateEnum.Companion.fromString
import im.threads.business.models.enums.ErrorStateEnum.Companion.errorStateEnumFromString
import im.threads.business.utils.FileUtils.safeParse
import java.util.Locale

class FileDescriptionsTable : Table() {
    override fun createTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE " + TABLE_FILE_DESCRIPTION + " ( " +
                COLUMN_FD_FROM + " text, " +
                COLUMN_FD_PATH + " text, " +
                COLUMN_FD_TIMESTAMP + " integer, " +
                COLUMN_FD_MESSAGE_UUID_EXT + " integer, " +
                COLUMN_FD_URL + " text, " +
                COLUMN_FD_SIZE + " integer, " +
                COLUMN_FD_IS_FROM_QUOTE + " integer, " +
                COLUMN_FD_FILENAME + " text," +
                COLUMN_FD_MIME_TYPE + " text," +
                COLUMN_FD_DOWNLOAD_PROGRESS + " integer, " +
                COLUMN_FD_ATTACHMENT_STATE + " text, " +
                COLUMN_FD_ERROR_CODE + " text, " +
                COLUMN_FD_ERROR_MESSAGE + " text )"
        )
    }

    override fun upgradeTable(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FILE_DESCRIPTION")
    }

    override fun cleanTable(sqlHelper: SQLiteOpenHelper) {
        sqlHelper.writableDatabase.execSQL("delete from $TABLE_FILE_DESCRIPTION")
    }

    fun getFileDescription(sqlHelper: SQLiteOpenHelper, messageUuid: String?): FileDescription? {
        if (TextUtils.isEmpty(messageUuid)) {
            return null
        }
        val query = String.format(
            Locale.US,
            "select * from %s where %s = ?",
            TABLE_FILE_DESCRIPTION,
            COLUMN_FD_MESSAGE_UUID_EXT
        )
        sqlHelper.writableDatabase.rawQuery(query, arrayOf(messageUuid)).use { c ->
            if (!c.moveToFirst()) {
                return null
            }
            val fd = FileDescription(
                cGetString(c, COLUMN_FD_FROM),
                safeParse(cGetString(c, COLUMN_FD_PATH)),
                cGetLong(c, COLUMN_FD_SIZE),
                cGetLong(c, COLUMN_FD_TIMESTAMP)
            )
            fd.downloadProgress = cGetInt(
                c,
                COLUMN_FD_DOWNLOAD_PROGRESS
            )
            fd.downloadPath = cGetString(
                c,
                COLUMN_FD_URL
            )
            fd.incomingName = cGetString(
                c,
                COLUMN_FD_FILENAME
            )
            fd.mimeType = cGetString(
                c,
                COLUMN_FD_MIME_TYPE
            )
            fd.state = fromString(
                cGetString(
                    c,
                    COLUMN_FD_ATTACHMENT_STATE
                )!!
            )
            fd.errorCode = errorStateEnumFromString(
                cGetString(
                    c,
                    COLUMN_FD_ERROR_CODE
                )!!
            )
            fd.errorMessage = cGetString(
                c,
                COLUMN_FD_ERROR_MESSAGE
            )
            fd.mimeType = cGetString(
                c,
                COLUMN_FD_MIME_TYPE
            )
            return fd
        }
    }

    fun getAllFileDescriptions(sqlHelper: SQLiteOpenHelper): List<FileDescription> {
        val query = String.format(
            Locale.US,
            "select * from %s order by %s desc",
            TABLE_FILE_DESCRIPTION,
            COLUMN_FD_TIMESTAMP
        )
        val list: MutableList<FileDescription> = ArrayList()
        sqlHelper.writableDatabase.rawQuery(query, arrayOf()).use { c ->
            if (!c.moveToFirst()) {
                return list
            }
            c.moveToFirst()
            while (!c.isAfterLast) {
                val fd = FileDescription(
                    cGetString(c, COLUMN_FD_FROM),
                    safeParse(cGetString(c, COLUMN_FD_PATH)),
                    cGetLong(c, COLUMN_FD_SIZE),
                    cGetLong(c, COLUMN_FD_TIMESTAMP)
                )
                fd.downloadProgress = cGetInt(
                    c,
                    COLUMN_FD_DOWNLOAD_PROGRESS
                )
                fd.incomingName = cGetString(
                    c,
                    COLUMN_FD_FILENAME
                )
                fd.mimeType = cGetString(
                    c,
                    COLUMN_FD_MIME_TYPE
                )
                fd.downloadPath = cGetString(
                    c,
                    COLUMN_FD_URL
                )
                fd.state = fromString(
                    cGetString(
                        c,
                        COLUMN_FD_ATTACHMENT_STATE
                    )!!
                )
                fd.errorCode = errorStateEnumFromString(
                    cGetString(
                        c,
                        COLUMN_FD_ERROR_CODE
                    )!!
                )
                fd.errorMessage = cGetString(
                    c,
                    COLUMN_FD_ERROR_MESSAGE
                )
                list.add(fd)
                c.moveToNext()
            }
            return list
        }
    }

    companion object {
        private const val TABLE_FILE_DESCRIPTION = "TABLE_FILE_DESCRIPTION"
        private const val COLUMN_FD_FROM = "COLUMN_FD_FROM"
        private const val COLUMN_FD_PATH = "COLUMN_FD_PATH"
        private const val COLUMN_FD_URL = "COLUMN_URL"
        private const val COLUMN_FD_DOWNLOAD_PROGRESS = "COLUMN_FD_DOWNLOAD_PROGRESS"
        private const val COLUMN_FD_TIMESTAMP = "COLUMN_FD_TIMESTAMP"
        private const val COLUMN_FD_SIZE = "COLUMN_FD_SIZE"
        private const val COLUMN_FD_IS_FROM_QUOTE = "COLUMN_FD_IS_FROM_QUOTE"
        private const val COLUMN_FD_FILENAME = "COLUMN_FD_FILENAME"
        private const val COLUMN_FD_MIME_TYPE = "COLUMN_FD_MIME_TYPE"
        private const val COLUMN_FD_MESSAGE_UUID_EXT = "COLUMN_FD_MESSAGE_UUID_EXT"
        private const val COLUMN_FD_ATTACHMENT_STATE = "ATTACHMENT_STATE"
        private const val COLUMN_FD_ERROR_CODE = "ERROR_CODE"
        private const val COLUMN_FD_ERROR_MESSAGE = "ERROR_MESSAGE"
    }
}
