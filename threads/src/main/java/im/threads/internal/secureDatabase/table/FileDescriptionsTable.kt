package im.threads.internal.secureDatabase.table

import android.content.ContentValues
import im.threads.internal.model.AttachmentStateEnum
import im.threads.internal.model.ErrorStateEnum
import im.threads.internal.model.FileDescription
import im.threads.internal.secureDatabase.ThreadsDbHelper.Companion.DB_PASSWORD
import im.threads.internal.utils.FileUtils.safeParse
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import java.util.ArrayList
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
        sqlHelper.getWritableDatabase(DB_PASSWORD).execSQL("delete from $TABLE_FILE_DESCRIPTION")
    }

    fun getFileDescription(sqlHelper: SQLiteOpenHelper, messageUuid: String?): FileDescription? {
        if (messageUuid.isNullOrEmpty()) {
            return null
        }
        val query = String.format(
            Locale.US,
            "select * from %s where %s = ?",
            TABLE_FILE_DESCRIPTION,
            COLUMN_FD_MESSAGE_UUID_EXT
        )
        sqlHelper.getWritableDatabase(DB_PASSWORD).rawQuery(query, arrayOf(messageUuid)).use { c ->
            if (!c.moveToFirst()) {
                return null
            }
            val fd = FileDescription(
                cursorGetString(c, COLUMN_FD_FROM),
                safeParse(cursorGetString(c, COLUMN_FD_PATH)),
                cursorGetLong(c, COLUMN_FD_SIZE),
                cursorGetLong(c, COLUMN_FD_TIMESTAMP)
            )
            fd.downloadProgress = cursorGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS)
            fd.downloadPath = cursorGetString(c, COLUMN_FD_URL)
            fd.incomingName = cursorGetString(c, COLUMN_FD_FILENAME)
            fd.mimeType = cursorGetString(c, COLUMN_FD_MIME_TYPE)
            fd.state = AttachmentStateEnum.READY
            cursorGetString(c, COLUMN_FD_ATTACHMENT_STATE)?.let {
                fd.state = AttachmentStateEnum.attachmentStateEnumFromString(it)
            }
            fd.errorCode = ErrorStateEnum.ANY
            cursorGetString(c, COLUMN_FD_ERROR_CODE)?.let {
                fd.errorCode = ErrorStateEnum.errorStateStateEnumFromString(it)
            }
            fd.errorMessage = cursorGetString(c, COLUMN_FD_ERROR_MESSAGE)
            fd.mimeType = cursorGetString(c, COLUMN_FD_MIME_TYPE)
            return fd
        }
    }

    fun putFileDescription(
        sqlHelper: SQLiteOpenHelper,
        fileDescription: FileDescription,
        fdMessageUuid: String,
        isFromQuote: Boolean
    ) {
        val cv = ContentValues()
        cv.put(COLUMN_FD_MESSAGE_UUID_EXT, fdMessageUuid)
        cv.put(COLUMN_FD_FROM, fileDescription.from)
        if (fileDescription.fileUri != null) {
            cv.put(COLUMN_FD_PATH, fileDescription.fileUri.toString())
        }
        cv.put(COLUMN_FD_URL, fileDescription.downloadPath)
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.timeStamp)
        cv.put(COLUMN_FD_SIZE, fileDescription.size)
        cv.put(COLUMN_FD_IS_FROM_QUOTE, isFromQuote)
        cv.put(COLUMN_FD_FILENAME, fileDescription.incomingName)
        cv.put(COLUMN_FD_MIME_TYPE, fileDescription.mimeType)
        cv.put(COLUMN_FD_ATTACHMENT_STATE, fileDescription.state.state)
        cv.put(COLUMN_FD_ERROR_CODE, fileDescription.errorCode.state)
        cv.put(COLUMN_FD_ERROR_MESSAGE, fileDescription.errorMessage)
        val sql = (
            "select " + COLUMN_FD_MESSAGE_UUID_EXT + " and " + COLUMN_FD_PATH +
                " from " + TABLE_FILE_DESCRIPTION +
                " where " + COLUMN_FD_MESSAGE_UUID_EXT + " = ?"
            )
        val selectionArgs = arrayOf(fdMessageUuid)
        sqlHelper.getWritableDatabase(DB_PASSWORD).rawQuery(sql, selectionArgs).use { c ->
            val existsInDb: Boolean = c.count > 0
            if (existsInDb) {
                c.moveToFirst()
                val localPath: String? = cursorGetString(c, COLUMN_FD_PATH)
                if (localPath.isNullOrEmpty()) {
                    cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.downloadProgress)
                }
                sqlHelper.getWritableDatabase(DB_PASSWORD)
                    .update(
                        TABLE_FILE_DESCRIPTION,
                        cv,
                        "$COLUMN_FD_MESSAGE_UUID_EXT = ? ",
                        arrayOf(fdMessageUuid)
                    )
            } else {
                cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.downloadProgress)
                sqlHelper.getWritableDatabase(DB_PASSWORD)
                    .insert(TABLE_FILE_DESCRIPTION, null, cv)
            }
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
        sqlHelper.getWritableDatabase(DB_PASSWORD).rawQuery(query, arrayOf()).use { c ->
            if (!c.moveToFirst()) {
                return list
            }
            c.moveToFirst()
            while (!c.isAfterLast) {
                val fd = FileDescription(
                    cursorGetString(c, COLUMN_FD_FROM),
                    safeParse(cursorGetString(c, COLUMN_FD_PATH)),
                    cursorGetLong(c, COLUMN_FD_SIZE),
                    cursorGetLong(c, COLUMN_FD_TIMESTAMP)
                )
                fd.downloadProgress = cursorGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS)
                fd.incomingName = cursorGetString(c, COLUMN_FD_FILENAME)
                fd.mimeType = cursorGetString(c, COLUMN_FD_MIME_TYPE)
                fd.downloadPath = cursorGetString(c, COLUMN_FD_URL)
                fd.state = AttachmentStateEnum.READY
                cursorGetString(c, COLUMN_FD_ATTACHMENT_STATE)?.let {
                    fd.state = AttachmentStateEnum.attachmentStateEnumFromString(it)
                }
                fd.errorCode = ErrorStateEnum.ANY
                cursorGetString(c, COLUMN_FD_ERROR_CODE)?.let {
                    fd.errorCode = ErrorStateEnum.errorStateStateEnumFromString(it)
                }
                fd.errorMessage = cursorGetString(c, COLUMN_FD_ERROR_MESSAGE)
                list.add(fd)
                c.moveToNext()
            }
            return list
        }
    }

    fun putFileDescriptions(sqlHelper: SQLiteOpenHelper, fileDescriptions: List<FileDescription?>) {
        fileDescriptions.forEach {
            it?.let {
                val cv = ContentValues()
                cv.put(COLUMN_FD_FROM, it.from)
                cv.put(
                    COLUMN_FD_PATH,
                    if (it.fileUri != null) it.fileUri.toString() else null
                )
                cv.put(COLUMN_FD_URL, it.downloadPath)
                cv.put(COLUMN_FD_TIMESTAMP, it.timeStamp)
                cv.put(COLUMN_FD_SIZE, it.size)
                cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, it.downloadProgress)
                cv.put(COLUMN_FD_FILENAME, it.incomingName)
                cv.put(COLUMN_FD_MIME_TYPE, it.mimeType)
                cv.put(COLUMN_FD_ATTACHMENT_STATE, it.state.state)
                cv.put(COLUMN_FD_ERROR_CODE, it.errorCode.state)
                cv.put(COLUMN_FD_ERROR_MESSAGE, it.errorMessage)
                sqlHelper.getWritableDatabase(DB_PASSWORD).update(
                    TABLE_FILE_DESCRIPTION,
                    cv,
                    (
                        "" + COLUMN_FD_FILENAME +
                            " like ? and " + COLUMN_FD_URL + " like ?"
                        ),
                    arrayOf(it.incomingName, it.downloadPath)
                )
            }
        }
    }

    fun updateFileDescriptionByName(sqlHelper: SQLiteOpenHelper, fileDescription: FileDescription) {
        val cv = getCvFromFileDescription(fileDescription)
        sqlHelper.getWritableDatabase(DB_PASSWORD).update(
            TABLE_FILE_DESCRIPTION,
            cv,
            ("$COLUMN_FD_FILENAME like ? and $COLUMN_FD_URL like ?"),
            arrayOf(fileDescription.incomingName, fileDescription.downloadPath)
        )
    }

    fun updateFileDescriptionByUrl(sqlHelper: SQLiteOpenHelper, fileDescription: FileDescription) {
        val cv = getCvFromFileDescription(fileDescription)
        sqlHelper.getWritableDatabase(DB_PASSWORD).update(
            TABLE_FILE_DESCRIPTION,
            cv,
            ("$COLUMN_FD_URL = ?"),
            arrayOf(fileDescription.originalPath)
        )
    }

    private fun getCvFromFileDescription(fileDescription: FileDescription): ContentValues {
        return ContentValues().apply {
            put(COLUMN_FD_FROM, fileDescription.from)
            put(
                COLUMN_FD_PATH,
                if (fileDescription.fileUri != null) fileDescription.fileUri.toString() else null
            )
            put(COLUMN_FD_URL, fileDescription.downloadPath)
            put(COLUMN_FD_TIMESTAMP, fileDescription.timeStamp)
            put(COLUMN_FD_SIZE, fileDescription.size)
            put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.downloadProgress)
            put(COLUMN_FD_FILENAME, fileDescription.incomingName)
            put(COLUMN_FD_MIME_TYPE, fileDescription.mimeType)
            put(COLUMN_FD_ATTACHMENT_STATE, fileDescription.state.state)
            put(COLUMN_FD_ERROR_CODE, fileDescription.errorCode.state)
            put(COLUMN_FD_ERROR_MESSAGE, fileDescription.errorMessage)
        }
    }
}

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
