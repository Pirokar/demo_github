package im.threads.business.secureDatabase.table

import android.content.ContentValues
import im.threads.business.models.QuestionDTO
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

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
        sqlHelper.writableDatabase.execSQL("delete from $TABLE_QUESTIONS")
    }

    fun getQuestions(sqlHelper: SQLiteOpenHelper, surveySendingId: Long): List<QuestionDTO> {
        val query = "select * from $TABLE_QUESTIONS where $COLUMN_QUESTION_SURVEY_SENDING_ID_EXT = $surveySendingId"
        sqlHelper.readableDatabase
            .rawQuery(query, arrayOf()).use {
                val result = mutableListOf<QuestionDTO>()
                if (!it.moveToFirst()) {
                    return result
                }
                do {
                    val question = QuestionDTO()
                    question.phraseTimeStamp = cursorGetLong(it, COLUMN_QUESTION_TIMESTAMP)
                    question.id = cursorGetLong(it, COLUMN_QUESTION_ID)
                    question.sendingId = cursorGetLong(it, COLUMN_QUESTION_SENDING_ID)
                    question.simple = cursorGetBool(it, COLUMN_QUESTION_SIMPLE)
                    question.text = cursorGetString(it, COLUMN_QUESTION_TEXT)
                    question.scale = cursorGetInt(it, COLUMN_QUESTION_SCALE)
                    // TODO THREADS-3625. This is a workaround on rate = 0 is a negative answer in simple (binary) survey
                    if (isCursorNull(it, COLUMN_QUESTION_RATE)) {
                        // Null is unanswered survey
                        question.rate = null
                    } else {
                        question.rate = cursorGetInt(it, COLUMN_QUESTION_RATE)
                    }
                    result.add(question)
                } while (it.moveToNext())

                return result
            }
    }

    fun putQuestions(sqlHelper: SQLiteOpenHelper, questions: List<QuestionDTO>, surveySendingId: Long) {
        if (questions.isNotEmpty()) {
            val availableQuestions = getQuestions(sqlHelper, surveySendingId).toMutableList()
            questions.forEach { question ->
                val indexOfItem = availableQuestions.indexOfFirst { it.id == question.id && it.text == question.text }
                if (indexOfItem < 0) {
                    availableQuestions.add(question)
                } else {
                    availableQuestions[indexOfItem] = question
                }
            }
            sqlHelper.writableDatabase.beginTransaction()

            sqlHelper.writableDatabase.delete(
                TABLE_QUESTIONS,
                "$COLUMN_QUESTION_SENDING_ID = $surveySendingId",
                null
            )

            availableQuestions.forEach {
                val questionCv = ContentValues()
                questionCv.put(COLUMN_QUESTION_SURVEY_SENDING_ID_EXT, surveySendingId)
                questionCv.put(COLUMN_QUESTION_ID, it.id)
                questionCv.put(COLUMN_QUESTION_SENDING_ID, it.sendingId)
                questionCv.put(COLUMN_QUESTION_SCALE, it.scale)

                if (it.hasRate()) {
                    questionCv.put(COLUMN_QUESTION_RATE, it.rate)
                }

                questionCv.put(COLUMN_QUESTION_TEXT, it.text)
                questionCv.put(COLUMN_QUESTION_SIMPLE, it.simple)
                questionCv.put(COLUMN_QUESTION_TIMESTAMP, it.phraseTimeStamp)

                sqlHelper.writableDatabase.insert(TABLE_QUESTIONS, null, questionCv)
            }

            sqlHelper.writableDatabase.setTransactionSuccessful()
            sqlHelper.writableDatabase.endTransaction()
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
