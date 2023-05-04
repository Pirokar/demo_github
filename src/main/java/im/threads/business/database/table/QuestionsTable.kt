package im.threads.business.database.table

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import im.threads.business.models.QuestionDTO

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

    fun getQuestion(sqlHelper: SQLiteOpenHelper, surveySendingId: Long): QuestionDTO? {
        val query = "select * from $TABLE_QUESTIONS where $COLUMN_QUESTION_SURVEY_SENDING_ID_EXT = ?"
        sqlHelper.writableDatabase.rawQuery(query, arrayOf(surveySendingId.toString())).use { c ->
            if (!c.moveToFirst()) {
                return null
            }
            val question = QuestionDTO()
            question.phraseTimeStamp = cGetLong(c, COLUMN_QUESTION_TIMESTAMP)
            question.id = cGetLong(c, COLUMN_QUESTION_ID)
            question.sendingId = cGetLong(c, COLUMN_QUESTION_SENDING_ID)
            question.simple = cGetBool(c, COLUMN_QUESTION_SIMPLE)
            question.text = cGetString(c, COLUMN_QUESTION_TEXT)
            question.scale = cGetInt(c, COLUMN_QUESTION_SCALE)
            // TODO THREADS-3625. This is a workaround on rate = 0 is a negative answer in simple (binary) survey
            if (cIsNull(c, COLUMN_QUESTION_RATE)) {
                question.rate = 0
            } else {
                question.rate = cGetInt(c, COLUMN_QUESTION_RATE)
            }
            return question
        }
    }

    companion object {
        private const val TABLE_QUESTIONS = "TABLE_QUESTIONS"
        private const val COLUMN_QUESTION_SCALE = "COLUMN_QUESTION_SCALE"
        private const val COLUMN_QUESTION_SURVEY_SENDING_ID_EXT = "COLUMN_QUESTION_SURVEY_SENDING_ID_EXT"
        private const val COLUMN_QUESTION_ID = "COLUMN_QUESTION_ID"
        private const val COLUMN_QUESTION_SENDING_ID = "COLUMN_QUESTION_SENDING_ID"
        private const val COLUMN_QUESTION_RATE = "COLUMN_QUESTION_RATE"
        private const val COLUMN_QUESTION_TEXT = "COLUMN_QUESTION_TEXT"
        private const val COLUMN_QUESTION_SIMPLE = "COLUMN_QUESTION_SIMPLE"
        private const val COLUMN_QUESTION_TIMESTAMP = "COLUMN_TIMESTAMP"
    }
}
