package im.threads.internal.secureDatabase.table

import android.content.ContentValues
import im.threads.internal.model.QuestionDTO
import im.threads.internal.secureDatabase.ThreadsDbHelper.Companion.DB_PASSWORD
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

class QuestionsTable : Table() {
    override fun createTable(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE " + TABLE_QUESTIONS + "(" +
                COLUMN_QUESTION_ID + " text," +
                COLUMN_QUESTION_SURVEY_SENDING_ID_EXT + " text," +
                COLUMN_QUESTION_SENDING_ID + " text," +
                COLUMN_QUESTION_TIMESTAMP + " integer," +
                COLUMN_QUESTION_SIMPLE + " text," +
                COLUMN_QUESTION_SCALE + " text," +
                COLUMN_QUESTION_RATE + " text," +
                COLUMN_QUESTION_TEXT + " text" +
                ")"
        )
    }

    override fun upgradeTable(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUESTIONS")
    }

    override fun cleanTable(sqlHelper: SQLiteOpenHelper) {
        sqlHelper.getWritableDatabase(DB_PASSWORD).execSQL("delete from $TABLE_QUESTIONS")
    }

    fun getQuestion(sqlHelper: SQLiteOpenHelper, surveySendingId: Long): QuestionDTO? {
        val query =
            "select * from $TABLE_QUESTIONS where $COLUMN_QUESTION_SURVEY_SENDING_ID_EXT = ?"
        sqlHelper.getWritableDatabase(DB_PASSWORD)
            .rawQuery(query, arrayOf(surveySendingId.toString())).use {
                if (!it.moveToFirst()) {
                    return null
                }
                val question = QuestionDTO()
                question.phraseTimeStamp = cursorGetLong(it, COLUMN_QUESTION_TIMESTAMP)
                question.id = cursorGetLong(it, COLUMN_QUESTION_ID)
                question.sendingId = cursorGetLong(it, COLUMN_QUESTION_SENDING_ID)
                question.isSimple = cursorGetBool(it, COLUMN_QUESTION_SIMPLE)
                question.text = cursorGetString(it, COLUMN_QUESTION_TEXT)
                question.scale = cursorGetInt(it, COLUMN_QUESTION_SCALE)
                // TODO THREADS-3625. This is a workaround on rate = 0 is a negative answer in simple (binary) survey
                if (isCursorNull(it, COLUMN_QUESTION_RATE)) {
                    // Null is unanswered survey
                    question.setRate(null)
                } else {
                    question.rate = cursorGetInt(it, COLUMN_QUESTION_RATE)
                }
                return question
            }
    }

    fun putQuestion(sqlHelper: SQLiteOpenHelper, question: QuestionDTO, surveySendingId: Long) {
        val questionSql = (
            "select " + COLUMN_QUESTION_SENDING_ID +
                " from " + TABLE_QUESTIONS +
                " where " + COLUMN_QUESTION_SENDING_ID + " = ? "
            )
        val questionSelectionArgs = arrayOf(question.sendingId.toString())
        val questionCv = ContentValues()
        questionCv.put(COLUMN_QUESTION_SURVEY_SENDING_ID_EXT, surveySendingId)
        questionCv.put(COLUMN_QUESTION_ID, question.id)
        questionCv.put(COLUMN_QUESTION_SENDING_ID, question.sendingId)
        questionCv.put(COLUMN_QUESTION_SCALE, question.scale)
        // TODO THREADS-3625. This is a workaround on rate = 0 is a negative answer in simple (binary) survey
        // Null is unanswered survey
        if (question.hasRate()) {
            questionCv.put(COLUMN_QUESTION_RATE, question.rate)
        }
        questionCv.put(COLUMN_QUESTION_TEXT, question.text)
        questionCv.put(COLUMN_QUESTION_SIMPLE, question.isSimple)
        questionCv.put(COLUMN_QUESTION_TIMESTAMP, question.phraseTimeStamp)
        sqlHelper.getWritableDatabase(DB_PASSWORD).rawQuery(questionSql, questionSelectionArgs)
            .use { questionCursor ->
                if (questionCursor.count > 0) {
                    sqlHelper.getWritableDatabase(DB_PASSWORD)
                        .update(
                            TABLE_QUESTIONS,
                            questionCv,
                            "$COLUMN_QUESTION_SENDING_ID = ? ",
                            arrayOf(question.sendingId.toString())
                        )
                } else {
                    sqlHelper.getWritableDatabase(DB_PASSWORD)
                        .insert(TABLE_QUESTIONS, null, questionCv)
                }
            }
    }
}

private const val TABLE_QUESTIONS = "TABLE_QUESTIONS"
private const val COLUMN_QUESTION_SCALE = "COLUMN_QUESTION_SCALE"
private const val COLUMN_QUESTION_SURVEY_SENDING_ID_EXT = "COLUMN_QUESTION_SURVEY_SENDING_ID_EXT"
private const val COLUMN_QUESTION_ID = "COLUMN_QUESTION_ID"
private const val COLUMN_QUESTION_SENDING_ID = "COLUMN_QUESTION_SENDING_ID"
private const val COLUMN_QUESTION_RATE = "COLUMN_QUESTION_RATE"
private const val COLUMN_QUESTION_TEXT = "COLUMN_QUESTION_TEXT"
private const val COLUMN_QUESTION_SIMPLE = "COLUMN_QUESTION_SIMPLE"
private const val COLUMN_QUESTION_TIMESTAMP = "COLUMN_TIMESTAMP"
