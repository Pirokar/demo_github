package im.threads.business.database.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import im.threads.business.models.QuestionDTO;

public class QuestionsTable extends Table {

    private static final String TABLE_QUESTIONS = "TABLE_QUESTIONS";
    private static final String COLUMN_QUESTION_SCALE = "COLUMN_QUESTION_SCALE";
    private static final String COLUMN_QUESTION_SURVEY_SENDING_ID_EXT = "COLUMN_QUESTION_SURVEY_SENDING_ID_EXT";
    private static final String COLUMN_QUESTION_ID = "COLUMN_QUESTION_ID";
    private static final String COLUMN_QUESTION_SENDING_ID = "COLUMN_QUESTION_SENDING_ID";
    private static final String COLUMN_QUESTION_RATE = "COLUMN_QUESTION_RATE";
    private static final String COLUMN_QUESTION_TEXT = "COLUMN_QUESTION_TEXT";
    private static final String COLUMN_QUESTION_SIMPLE = "COLUMN_QUESTION_SIMPLE";
    private static final String COLUMN_QUESTION_TIMESTAMP = "COLUMN_TIMESTAMP";

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_QUESTIONS + "("
                + COLUMN_QUESTION_ID + " text,"
                + COLUMN_QUESTION_SURVEY_SENDING_ID_EXT + " text,"
                + COLUMN_QUESTION_SENDING_ID + " text,"
                + COLUMN_QUESTION_TIMESTAMP + " integer,"
                + COLUMN_QUESTION_SIMPLE + " text,"
                + COLUMN_QUESTION_SCALE + " text,"
                + COLUMN_QUESTION_RATE + " text,"
                + COLUMN_QUESTION_TEXT + " text"
                + ")");
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
    }

    @Override
    public void cleanTable(SQLiteOpenHelper sqlHelper) {
        sqlHelper.getWritableDatabase().execSQL("delete from " + TABLE_QUESTIONS);
    }

    public QuestionDTO getQuestion(SQLiteOpenHelper sqlHelper, long surveySendingId) {
        String query = "select * from " + TABLE_QUESTIONS + " where " + COLUMN_QUESTION_SURVEY_SENDING_ID_EXT + " = ?";
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(query, new String[]{String.valueOf(surveySendingId)})) {
            if (!c.moveToFirst()) {
                return null;
            }
            QuestionDTO question = new QuestionDTO();
            question.setPhraseTimeStamp(cGetLong(c, COLUMN_QUESTION_TIMESTAMP));
            question.setId(cGetLong(c, COLUMN_QUESTION_ID));
            question.setSendingId(cGetLong(c, COLUMN_QUESTION_SENDING_ID));
            question.setSimple(cGetBool(c, COLUMN_QUESTION_SIMPLE));
            question.setText(cGetString(c, COLUMN_QUESTION_TEXT));
            question.setScale(cGetInt(c, COLUMN_QUESTION_SCALE));
            //TODO THREADS-3625. This is a workaround on rate = 0 is a negative answer in simple (binary) survey
            if (cIsNull(c, COLUMN_QUESTION_RATE)) {
                //Null is unanswered survey
                question.setRate(null);
            } else {
                question.setRate(cGetInt(c, COLUMN_QUESTION_RATE));
            }
            return question;
        }
    }

    public void putQuestion(SQLiteOpenHelper sqlHelper, QuestionDTO question, long surveySendingId) {
        String questionSql = "select " + COLUMN_QUESTION_SENDING_ID
                + " from " + TABLE_QUESTIONS
                + " where " + COLUMN_QUESTION_SENDING_ID + " = ? ";
        String[] questionSelectionArgs = new String[]{String.valueOf(question.getSendingId())};
        ContentValues questionCv = new ContentValues();
        questionCv.put(COLUMN_QUESTION_SURVEY_SENDING_ID_EXT, surveySendingId);
        questionCv.put(COLUMN_QUESTION_ID, question.getId());
        questionCv.put(COLUMN_QUESTION_SENDING_ID, question.getSendingId());
        questionCv.put(COLUMN_QUESTION_SCALE, question.getScale());
        //TODO THREADS-3625. This is a workaround on rate = 0 is a negative answer in simple (binary) survey
        //Null is unanswered survey
        if (question.hasRate()) {
            questionCv.put(COLUMN_QUESTION_RATE, question.getRate());
        }
        questionCv.put(COLUMN_QUESTION_TEXT, question.getText());
        questionCv.put(COLUMN_QUESTION_SIMPLE, question.isSimple());
        questionCv.put(COLUMN_QUESTION_TIMESTAMP, question.getPhraseTimeStamp());
        try (Cursor questionCursor = sqlHelper.getWritableDatabase().rawQuery(questionSql, questionSelectionArgs)) {
            if (questionCursor.getCount() > 0) {
                sqlHelper.getWritableDatabase().update(
                        TABLE_QUESTIONS,
                        questionCv,
                        COLUMN_QUESTION_SENDING_ID + " = ? ",
                        new String[]{String.valueOf(question.getSendingId())}
                );
            } else {
                sqlHelper.getWritableDatabase().insert(TABLE_QUESTIONS, null, questionCv);
            }
        }
    }

}
