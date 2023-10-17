package im.threads.business.database.table

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import im.threads.business.formatters.SpeechStatus.Companion.fromString
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultConnectionMessage
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.ConsultRole
import im.threads.business.models.MessageStatus.Companion.fromOrdinal
import im.threads.business.models.RequestResolveThread
import im.threads.business.models.SimpleSystemMessage
import im.threads.business.models.Survey
import im.threads.business.models.UserPhrase
import im.threads.business.models.enums.ModificationStateEnum
import java.util.Locale

class MessagesTable(
    private val fileDescriptionTable: FileDescriptionsTable,
    private val quotesTable: QuotesTable,
    private val quickRepliesTable: QuickRepliesTable,
    private val questionsTable: QuestionsTable
) : Table() {
    override fun createTable(db: SQLiteDatabase) {
        db.execSQL(
            String.format(
                Locale.US,
                "create table %s " + // messages table
                    "( %s integer primary key autoincrement," + // id column
                    " %s integer, " + // timestamp
                    " %s text, " + // phrase
                    " %s text, " + // COLUMN_FORMATTED_PHRASE
                    " %s integer, " + // item type
                    " %s text, " + // name
                    " %s text, " + // avatar path
                    " %s text, " + // message id
                    " %s integer, " + // sex
                    " %s integer," + // message sent state
                    "%s text," + // consultid
                    "%s text," + // COLUMN_CONSULT_STATUS
                    "%s text" + // COLUMN_CONSULT_TITLE
                    ", " + COLUMN_CONSULT_ORG_UNIT + " text," + // COLUMN_CONSULT_ORG_UNIT
                    "%s text," + // connection type
                    "%s integer " + // isRead
                    ", " + COLUMN_DISPLAY_MESSAGE + " integer" +
                    ", " + COLUMN_SURVEY_SENDING_ID + " integer" +
                    ", " + COLUMN_SURVEY_HIDE_AFTER + " integer" +
                    ", " + COLUMN_THREAD_ID + " integer" +
                    ", " + COLUMN_BLOCK_INPUT + " integer" +
                    ", " + COLUMN_SPEECH_STATUS + " text" +
                    ", " + COLUMN_MODIFICATION_STATE + " text" +
                    ")",
                TABLE_MESSAGES,
                COLUMN_TABLE_ID,
                COLUMN_TIMESTAMP,
                COLUMN_PHRASE,
                COLUMN_FORMATTED_PHRASE,
                COLUMN_MESSAGE_TYPE,
                COLUMN_NAME,
                COLUMN_AVATAR_PATH,
                COLUMN_MESSAGE_UUID,
                COLUMN_SEX,
                COLUMN_MESSAGE_SEND_STATE,
                COLUMN_CONSULT_ID,
                COLUMN_CONSULT_STATUS,
                COLUMN_CONSULT_TITLE,
                COLUMN_CONNECTION_TYPE,
                COLUMN_IS_READ
            )
        )
    }

    override fun upgradeTable(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COLUMN_DISPLAY_MESSAGE INTEGER DEFAULT 0")
        }
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        }
    }

    override fun cleanTable(sqlHelper: SQLiteOpenHelper) {
        sqlHelper.writableDatabase.execSQL("delete from $TABLE_MESSAGES")
    }

    fun getChatItems(sqlHelper: SQLiteOpenHelper, offset: Int, limit: Int): List<ChatItem> {
        val items: MutableList<ChatItem> = ArrayList()
        val query = String.format(
            Locale.US,
            "select * from (select * from %s order by %s desc limit %s offset %s) order by %s asc",
            TABLE_MESSAGES,
            COLUMN_TIMESTAMP,
            limit,
            offset,
            COLUMN_TIMESTAMP
        )
        sqlHelper.writableDatabase.rawQuery(query, null).use { c ->
            if (c.count == 0) {
                return items
            }
            c.moveToFirst()
            while (!c.isAfterLast) {
                val chatItem = getChatItem(sqlHelper, c)
                if (chatItem != null) {
                    items.add(chatItem)
                }
                c.moveToNext()
            }
        }
        return items
    }

    private fun getChatItem(sqlHelper: SQLiteOpenHelper, c: Cursor): ChatItem? {
        when (cGetInt(c, COLUMN_MESSAGE_TYPE)) {
            MessageType.CONSULT_CONNECTED.ordinal -> {
                return ConsultConnectionMessage(
                    cGetString(c, COLUMN_MESSAGE_UUID),
                    cGetString(c, COLUMN_CONSULT_ID),
                    cGetString(c, COLUMN_CONNECTION_TYPE),
                    cGetString(c, COLUMN_NAME),
                    cGetBool(c, COLUMN_SEX),
                    cGetLong(c, COLUMN_TIMESTAMP),
                    cGetString(c, COLUMN_AVATAR_PATH),
                    cGetString(c, COLUMN_CONSULT_STATUS),
                    cGetString(c, COLUMN_CONSULT_TITLE),
                    cGetString(c, COLUMN_CONSULT_ORG_UNIT),
                    cGetBool(c, COLUMN_DISPLAY_MESSAGE),
                    cGetString(c, COLUMN_PHRASE),
                    cGetLong(c, COLUMN_THREAD_ID)
                )
            }
            MessageType.SYSTEM_MESSAGE.ordinal -> {
                return SimpleSystemMessage(
                    cGetString(c, COLUMN_MESSAGE_UUID),
                    cGetString(c, COLUMN_CONNECTION_TYPE),
                    cGetLong(c, COLUMN_TIMESTAMP),
                    cGetString(c, COLUMN_PHRASE),
                    cGetLong(c, COLUMN_THREAD_ID)
                )
            }
            MessageType.CONSULT_PHRASE.ordinal -> {
                return getConsultPhrase(sqlHelper, c)
            }
            MessageType.USER_PHRASE.ordinal -> {
                return getUserPhrase(sqlHelper, c)
            }
            MessageType.SURVEY.ordinal -> {
                return getSurvey(sqlHelper, c)
            }
            MessageType.REQUEST_RESOLVE_THREAD.ordinal -> {
                return getRequestResolveThread(c)
            }
            else -> return null
        }
    }

    private fun getConsultPhrase(sqlHelper: SQLiteOpenHelper, c: Cursor): ConsultPhrase {
        return ConsultPhrase(
            cGetString(c, COLUMN_MESSAGE_UUID),
            fileDescriptionTable.getFileDescription(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
            ModificationStateEnum.fromString(cGetString(c, COLUMN_MODIFICATION_STATE)),
            quotesTable.getQuote(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
            cGetString(c, COLUMN_NAME),
            cGetString(c, COLUMN_PHRASE),
            cGetString(c, COLUMN_FORMATTED_PHRASE),
            cGetLong(c, COLUMN_TIMESTAMP),
            cGetString(c, COLUMN_CONSULT_ID),
            cGetString(c, COLUMN_AVATAR_PATH),
            cGetBool(c, COLUMN_IS_READ),
            cGetString(c, COLUMN_CONSULT_STATUS),
            cGetBool(c, COLUMN_SEX),
            cGetLong(c, COLUMN_THREAD_ID),
            quickRepliesTable.getQuickReplies(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
            cGetBool(c, COLUMN_BLOCK_INPUT),
            fromString(cGetString(c, COLUMN_SPEECH_STATUS)),
            ConsultRole.consultRoleFromString(cGetString(c, COLUMN_ROLE))
        )
    }

    private fun getUserPhrase(sqlHelper: SQLiteOpenHelper, c: Cursor): UserPhrase {
        return UserPhrase(
            cGetString(c, COLUMN_MESSAGE_UUID),
            cGetString(c, COLUMN_PHRASE),
            quotesTable.getQuote(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
            cGetLong(c, COLUMN_TIMESTAMP),
            fileDescriptionTable.getFileDescription(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
            fromOrdinal(cGetInt(c, COLUMN_MESSAGE_SEND_STATE)),
            cGetLong(c, COLUMN_THREAD_ID)
        )
    }

    private fun getSurvey(sqlHelper: SQLiteOpenHelper, c: Cursor): Survey {
        val surveySendingId = cGetLong(c, COLUMN_SURVEY_SENDING_ID)
        val survey = Survey(
            cGetString(c, COLUMN_MESSAGE_UUID),
            surveySendingId,
            cGetLong(c, COLUMN_SURVEY_HIDE_AFTER),
            cGetLong(c, COLUMN_TIMESTAMP),
            fromOrdinal(cGetInt(c, COLUMN_MESSAGE_SEND_STATE)),
            cGetBool(c, COLUMN_IS_READ),
            cGetBool(c, COLUMN_DISPLAY_MESSAGE)
        )
        survey.questions = ArrayList(listOfNotNull(questionsTable.getQuestion(sqlHelper, surveySendingId)))
        return survey
    }

    private fun getRequestResolveThread(c: Cursor): RequestResolveThread? {
        val requestResolveThread = RequestResolveThread(
            cGetString(c, COLUMN_MESSAGE_UUID),
            cGetLong(c, COLUMN_SURVEY_HIDE_AFTER),
            cGetLong(c, COLUMN_TIMESTAMP),
            cGetLong(c, COLUMN_THREAD_ID),
            cGetBool(c, COLUMN_IS_READ)
        )
        return if (!cGetBool(
                c,
                COLUMN_DISPLAY_MESSAGE
            )
        ) {
            null
        } else {
            requestResolveThread
        }
    }

    private enum class MessageType {
        UNKNOWN, CONSULT_CONNECTED, SYSTEM_MESSAGE, CONSULT_PHRASE, USER_PHRASE, SURVEY, REQUEST_RESOLVE_THREAD
    }

    companion object {
        private const val TABLE_MESSAGES = "TABLE_MESSAGES"
        private const val COLUMN_TABLE_ID = "TABLE_ID"
        private const val COLUMN_MESSAGE_UUID = "COLUMN_MESSAGE_UUID"
        private const val COLUMN_MODIFICATION_STATE = "COLUMN_MODIFICATION_STATE"
        private const val COLUMN_TIMESTAMP = "COLUMN_TIMESTAMP"
        private const val COLUMN_PHRASE = "COLUMN_PHRASE"
        private const val COLUMN_FORMATTED_PHRASE = "COLUMN_FORMATTED_PHRASE"
        private const val COLUMN_MESSAGE_TYPE = "COLUMN_MESSAGE_TYPE"
        private const val COLUMN_NAME = "COLUMN_NAME"
        private const val COLUMN_AVATAR_PATH = "COLUMN_AVATAR_PATH"
        private const val COLUMN_MESSAGE_SEND_STATE = "COLUMN_MESSAGE_SEND_STATE"
        private const val COLUMN_SEX = "COLUMN_SEX"
        private const val COLUMN_CONSULT_ID = "COLUMN_CONSULT_ID"
        private const val COLUMN_CONSULT_STATUS = "COLUMN_CONSULT_STATUS"
        private const val COLUMN_CONSULT_TITLE = "COLUMN_CONSULT_TITLE"
        private const val COLUMN_CONSULT_ORG_UNIT = "COLUMN_CONSULT_ORG_UNIT"
        private const val COLUMN_CONNECTION_TYPE = "COLUMN_CONNECTION_TYPE"
        private const val COLUMN_IS_READ = "COLUMN_IS_READ"
        private const val COLUMN_DISPLAY_MESSAGE = "COLUMN_DISPLAY_MESSAGE"
        private const val COLUMN_SURVEY_SENDING_ID = "COLUMN_SURVEY_SENDING_ID"
        private const val COLUMN_SURVEY_HIDE_AFTER = "COLUMN_SURVEY_HIDE_AFTER"
        private const val COLUMN_THREAD_ID = "COLUMN_THREAD_ID"
        private const val COLUMN_BLOCK_INPUT = "COLUMN_BLOCK_INPUT"
        private const val COLUMN_SPEECH_STATUS = "COLUMN_SPEECH_STATUS"
        private const val COLUMN_ROLE = "COLUMN_ROLE"
    }
}
