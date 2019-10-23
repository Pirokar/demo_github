package im.threads.internal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ChatPhrase;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.QuestionDTO;
import im.threads.internal.model.Quote;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;

/**
 * обертка для БД
 */
final class MyOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 6;
    private static final String TABLE_MESSAGES = "TABLE_MESSAGES";
    private static final String COLUMN_TABLE_ID = "TABLE_ID";
    private static final String COLUMN_MESSAGE_UUID = "COLUMN_MESSAGE_UUID";
    private static final String COLUMN_PROVIDER_ID = "COLUMN_PROVIDER_ID";
    private static final String COLUMN_TIMESTAMP = "COLUMN_TIMESTAMP";
    private static final String COLUMN_PHRASE = "COLUMN_PHRASE";
    private static final String COLUMN_MESSAGE_TYPE = "COLUMN_MESSAGE_TYPE";
    private static final String COLUMN_NAME = "COLUMN_NAME";
    private static final String COLUMN_AVATAR_PATH = "COLUMN_AVATAR_PATH";
    private static final String COLUMN_MESSAGE_SEND_STATE = "COLUMN_MESSAGE_SEND_STATE";
    private static final String COLUMN_SEX = "COLUMN_SEX";
    private static final String COLUMN_CONSULT_ID = "COLUMN_CONSULT_ID";
    private static final String COLUMN_CONSULT_STATUS = "COLUMN_CONSULT_STATUS";
    private static final String COLUMN_CONSULT_TITLE = "COLUMN_CONSULT_TITLE";
    private static final String COLUMN_CONSULT_ORG_UNIT = "COLUMN_CONSULT_ORG_UNIT";
    private static final String COLUMN_CONNECTION_TYPE = "COLUMN_CONNECTION_TYPE";
    private static final String COLUMN_IS_READ = "COLUMN_IS_READ";
    private static final String COLUMN_DISPLAY_MASSAGE = "COLUMN_DISPLAY_MESSAGE";


    private static final String TABLE_QUOTE = "TABLE_QUOTE";
    private static final String COLUMN_QUOTE_UUID = "COLUMN_QUOTE_UUID";
    private static final String COLUMN_QUOTE_FROM = "COLUMN_QUOTE_HEADER";
    private static final String COLUMN_QUOTE_BODY = "COLUMN_QUOTE_BODY";
    private static final String COLUMN_QUOTE_TIMESTAMP = "COLUMN_QUOTE_TIMESTAMP";
    private static final String COLUMN_QUOTED_BY_MESSAGE_UUID_EXT = "COLUMN_QUOTED_BY_MESSAGE_UUID_EXT";

    private static final String TABLE_FILE_DESCRIPTION = "TABLE_FILE_DESCRIPTION";
    private static final String COLUMN_FD_FROM = "COLUMN_FD_FROM";
    private static final String COLUMN_FD_PATH = "COLUMN_FD_PATH";
    private static final String COLUMN_FD_URL = "COLUMN_URL";
    private static final String COLUMN_FD_DOWNLOAD_PROGRESS = "COLUMN_FD_DOWNLOAD_PROGRESS";
    private static final String COLUMN_FD_TIMESTAMP = "COLUMN_FD_TIMESTAMP";
    private static final String COLUMN_FD_SIZE = "COLUMN_FD_SIZE";
    private static final String COLUMN_FD_IS_FROM_QUOTE = "COLUMN_FD_IS_FROM_QUOTE";
    private static final String COLUMN_FD_FILENAME = "COLUMN_FD_FILENAME";
    private static final String COLUMN_FD_MESSAGE_UUID_EXT = "COLUMN_FD_MESSAGE_UUID_EXT";

    private static final String COLUMN_SURVEY_SENDING_ID = "COLUMN_SURVEY_SENDING_ID";
    private static final String COLUMN_SURVEY_HIDE_AFTER = "COLUMN_SURVEY_HIDE_AFTER";

    private static final String TABLE_QUESTIONS = "TABLE_QUESTIONS";
    private static final String COLUMN_QUESTION_SCALE = "COLUMN_QUESTION_SCALE";
    private static final String COLUMN_QUESTION_SURVEY_SENDING_ID_EXT = "COLUMN_QUESTION_SURVEY_SENDING_ID_EXT";
    private static final String COLUMN_QUESTION_ID = "COLUMN_QUESTION_ID";
    private static final String COLUMN_QUESTION_SENDING_ID = "COLUMN_QUESTION_SENDING_ID";
    private static final String COLUMN_QUESTION_RATE = "COLUMN_QUESTION_RATE";
    private static final String COLUMN_QUESTION_TEXT = "COLUMN_QUESTION_TEXT";
    private static final String COLUMN_QUESTION_SIMPLE = "COLUMN_QUESTION_SIMPLE";

    MyOpenHelper(Context context) {
        super(context, "messages.db", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(Locale.US, "create table %s " +//messages table
                        "( %s integer primary key autoincrement," +//id column
                        " %s integer, " +//timestamp
                        " %s text, " +//phrase
                        " %s integer, " +//item type
                        " %s text, " +//name
                        " %s text, " +//avatar path
                        " %s text, " + // message id
                        " %s integer, " + //sex
                        " %s integer," +//message sent state
                        "%s text," + //consultid
                        "%s text," + //COLUMN_CONSULT_STATUS
                        "%s text"//COLUMN_CONSULT_TITLE
                        + ", " + COLUMN_CONSULT_ORG_UNIT + " text," +//COLUMN_CONSULT_ORG_UNIT
                        "%s text," +//connection type
                        "%s integer," + //isRead
                        "%s text" //COLUMN_BACKEND_ID
                        + ", " + COLUMN_DISPLAY_MASSAGE + " integer"
                        + ", " + COLUMN_SURVEY_SENDING_ID + " integer"
                        + ", " + COLUMN_SURVEY_HIDE_AFTER + " integer"
                        + ")",
                TABLE_MESSAGES, COLUMN_TABLE_ID, COLUMN_TIMESTAMP
                , COLUMN_PHRASE, COLUMN_MESSAGE_TYPE, COLUMN_NAME, COLUMN_AVATAR_PATH,
                COLUMN_MESSAGE_UUID, COLUMN_SEX, COLUMN_MESSAGE_SEND_STATE, COLUMN_CONSULT_ID,
                COLUMN_CONSULT_STATUS, COLUMN_CONSULT_TITLE, COLUMN_CONNECTION_TYPE,
                COLUMN_IS_READ, COLUMN_PROVIDER_ID));
        db.execSQL("CREATE TABLE " + TABLE_QUOTE + "("
                + COLUMN_QUOTE_UUID + " text,"
                + COLUMN_QUOTE_FROM + " text, "
                + COLUMN_QUOTE_BODY + " text, "
                + COLUMN_QUOTE_TIMESTAMP + " integer, "
                + COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " integer)" // message id
        );
        db.execSQL("CREATE TABLE " + TABLE_FILE_DESCRIPTION + " ( "
                + COLUMN_FD_FROM + " text, "
                + COLUMN_FD_PATH + " text, "
                + COLUMN_FD_TIMESTAMP + " integer, "
                + COLUMN_FD_MESSAGE_UUID_EXT + " integer, "
                + COLUMN_FD_URL + " text, "
                + COLUMN_FD_SIZE + " integer, "
                + COLUMN_FD_IS_FROM_QUOTE + " integer, "
                + COLUMN_FD_FILENAME + " text,"
                + COLUMN_FD_DOWNLOAD_PROGRESS + " integer)"
        );
        db.execSQL("CREATE TABLE " + TABLE_QUESTIONS + "("
                + COLUMN_QUESTION_ID + " text,"
                + COLUMN_QUESTION_SURVEY_SENDING_ID_EXT + " text,"
                + COLUMN_QUESTION_SENDING_ID + " text,"
                + COLUMN_TIMESTAMP + " integer,"
                + COLUMN_QUESTION_SIMPLE + " text,"
                + COLUMN_QUESTION_SCALE + " text,"
                + COLUMN_QUESTION_RATE + " text,"
                + COLUMN_QUESTION_TEXT + " text"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COLUMN_DISPLAY_MASSAGE + " INTEGER DEFAULT 0");
        }
        if (oldVersion < VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUOTE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILE_DESCRIPTION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
            onCreate(db);
        }
        // VERSION 6 - quotes table uuid added, column names changed
        // dropping data with old file paths starting with "file://" prefix
    }

    void putChatPhrase(ChatPhrase phrase) {
        if (phrase instanceof ConsultPhrase) {
            insertOrUpdateConsultPhrase((ConsultPhrase) phrase);
        }
        if (phrase instanceof UserPhrase) {
            insertOrUpdateUserPhrase((UserPhrase) phrase);
        }
    }

    private void insertOrUpdateConsultPhrase(ConsultPhrase phrase) {
        ContentValues cv = new ContentValues();
        boolean isDup = false;
        Cursor c;
        c = getWritableDatabase().rawQuery("select " + COLUMN_MESSAGE_UUID + " from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_UUID + " = ?", new String[]{phrase.getUuid()});
        if (c.getCount() > 0) isDup = true;
        cv.put(COLUMN_MESSAGE_UUID, phrase.getUuid());
        cv.put(COLUMN_PHRASE, phrase.getPhrase());
        cv.put(COLUMN_TIMESTAMP, phrase.getTimeStamp());
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.CONSULT_PHRASE.ordinal());
        cv.put(COLUMN_AVATAR_PATH, phrase.getAvatarPath());
        cv.put(COLUMN_CONSULT_ID, phrase.getConsultId());
        cv.put(COLUMN_IS_READ, phrase.isRead());
        cv.put(COLUMN_CONSULT_STATUS, phrase.getStatus());
        cv.put(COLUMN_NAME, phrase.getConsultName());
        cv.put(COLUMN_PROVIDER_ID, phrase.getProviderId());
        cv.put(COLUMN_SEX, phrase.getSex());
        if (!isDup) {
            getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
        } else {
            getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_UUID + " = ? ", new String[]{phrase.getUuid()});
        }
        if (phrase.getFileDescription() != null) {
            putFd(phrase.getFileDescription(), phrase.getUuid(), false);
        }
        if (phrase.getQuote() != null) {
            putQuote(phrase.getUuid(), phrase.getQuote());
        }
        c.close();
    }

    private void insertOrUpdateUserPhrase(UserPhrase phrase) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_UUID, phrase.getUuid());
        cv.put(COLUMN_PROVIDER_ID, phrase.getProviderId());
        cv.put(COLUMN_PHRASE, phrase.getPhrase());
        cv.put(COLUMN_TIMESTAMP, phrase.getTimeStamp());
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.USER_PHRASE.ordinal());
        cv.put(COLUMN_MESSAGE_SEND_STATE, phrase.getSentState().ordinal());
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_MESSAGE_UUID + " from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_UUID + " = ?", new String[]{phrase.getUuid()});
        boolean existsInDb = c.getCount() > 0;
        if (existsInDb) {
            getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_UUID + " = ? ", new String[]{phrase.getUuid()});
        } else {
            getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
        }
        if (phrase.getFileDescription() != null) {
            putFd(phrase.getFileDescription(), phrase.getUuid(), false);
        }
        if (phrase.getQuote() != null) {
            putQuote(phrase.getUuid(), phrase.getQuote());
        }
        c.close();
    }

    void setUserPhraseStateByProviderId(String providerId, MessageState messageState) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_SEND_STATE, messageState.ordinal());
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_PROVIDER_ID + " = ?", new String[]{providerId});
    }

    void putConsultConnected(ConsultConnectionMessage consultConnectionMessage) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, consultConnectionMessage.getName());
        cv.put(COLUMN_TIMESTAMP, consultConnectionMessage.getTimeStamp());
        cv.put(COLUMN_AVATAR_PATH, consultConnectionMessage.getAvatarPath());
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.CONSULT_CONNECTED.ordinal());
        cv.put(COLUMN_SEX, consultConnectionMessage.getSex());
        cv.put(COLUMN_CONNECTION_TYPE, consultConnectionMessage.getConnectionType());
        cv.put(COLUMN_CONSULT_ID, consultConnectionMessage.getConsultId());
        cv.put(COLUMN_CONSULT_STATUS, consultConnectionMessage.getStatus());
        cv.put(COLUMN_CONSULT_TITLE, consultConnectionMessage.getTitle());
        cv.put(COLUMN_CONSULT_ORG_UNIT, consultConnectionMessage.getOrgUnit());
        cv.put(COLUMN_MESSAGE_UUID, consultConnectionMessage.getUuid());
        cv.put(COLUMN_DISPLAY_MASSAGE, consultConnectionMessage.isDisplayMessage());
        if (consultConnectionMessage.getName() == null) {
            getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
            return;
        }
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_MESSAGE_UUID
                        + " from " + TABLE_MESSAGES
                        + " where " + COLUMN_MESSAGE_UUID + " = ? and " + COLUMN_MESSAGE_TYPE + " = ? ",
                new String[]{consultConnectionMessage.getUuid(), String.valueOf(MessageType.CONSULT_CONNECTED.ordinal())});
        boolean isDuplicate = c.getCount() > 0;
        c.close();
        if (!isDuplicate) {
            getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
        } else {
            getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_UUID + " = ? ", new String[]{consultConnectionMessage.getUuid()});
        }
    }

    void insertOrUpdateSurvey(Survey survey) {
        Cursor cSurvey = getWritableDatabase().rawQuery("select " + COLUMN_SURVEY_SENDING_ID
                        + " from " + TABLE_MESSAGES
                        + " where " + COLUMN_SURVEY_SENDING_ID + " = ? and " + COLUMN_MESSAGE_TYPE + " = ? ",
                new String[]{String.valueOf(survey.getSendingId()), String.valueOf(MessageType.SURVEY.ordinal())});

        boolean surveyExists = cSurvey.getCount() > 0;
        cSurvey.close();
        ContentValues surveyValues = new ContentValues();
        surveyValues.put(COLUMN_MESSAGE_TYPE, MessageType.SURVEY.ordinal());
        surveyValues.put(COLUMN_SURVEY_SENDING_ID, survey.getSendingId());
        surveyValues.put(COLUMN_SURVEY_HIDE_AFTER, survey.getHideAfter());
        surveyValues.put(COLUMN_TIMESTAMP, survey.getTimeStamp());
        surveyValues.put(COLUMN_MESSAGE_SEND_STATE, survey.getSentState().ordinal());
        if (surveyExists) {
            getWritableDatabase().update(TABLE_MESSAGES, surveyValues,
                    COLUMN_SURVEY_SENDING_ID + " = ? ", new String[]{String.valueOf(survey.getSendingId())});
        } else {
            getWritableDatabase().insert(TABLE_MESSAGES, null, surveyValues);
        }
        for (QuestionDTO question : survey.getQuestions()) {
            Cursor cQuestion = getWritableDatabase().rawQuery("select " + COLUMN_QUESTION_SENDING_ID
                            + " from " + TABLE_QUESTIONS
                            + " where " + COLUMN_QUESTION_SENDING_ID + " = ? ",
                    new String[]{String.valueOf(question.getSendingId())});
            boolean questionExists = cQuestion.getCount() > 0;
            cQuestion.close();
            ContentValues questionValues = new ContentValues();
            questionValues.put(COLUMN_QUESTION_SURVEY_SENDING_ID_EXT, survey.getSendingId());
            questionValues.put(COLUMN_QUESTION_ID, question.getId());
            questionValues.put(COLUMN_QUESTION_SENDING_ID, question.getSendingId());
            questionValues.put(COLUMN_QUESTION_SCALE, question.getScale());
            //TODO THREADS-3625. This is a workaround on rate = 0 is a negative answer in simple (binary) survey
            //Null is unanswered survey
            if (question.hasRate()) {
                questionValues.put(COLUMN_QUESTION_RATE, question.getRate());
            }
            questionValues.put(COLUMN_QUESTION_TEXT, question.getText());
            questionValues.put(COLUMN_QUESTION_SIMPLE, question.isSimple());
            questionValues.put(COLUMN_TIMESTAMP, question.getTimeStamp());
            if (questionExists) {
                getWritableDatabase().update(TABLE_QUESTIONS, questionValues,
                        COLUMN_QUESTION_SENDING_ID + " = ? ", new String[]{String.valueOf(question.getSendingId())});
            } else {
                getWritableDatabase().insert(TABLE_QUESTIONS, null, questionValues);
            }
        }
    }

    ConsultPhrase getLastUnreadPhrase() {
        Cursor c = getWritableDatabase().rawQuery("select * from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal()
                + " and " + COLUMN_IS_READ + " = 0 order by " + COLUMN_TIMESTAMP + " desc", new String[]{});
        if (c.getCount() > 0) {
            c.moveToFirst();
            FileDescription fd = getFd(cGetString(c, COLUMN_MESSAGE_UUID));
            ConsultPhrase cp = new ConsultPhrase(
                    cGetString(c, COLUMN_MESSAGE_UUID),
                    cGetString(c, COLUMN_PROVIDER_ID),
                    fd,
                    getQuote(cGetString(c, COLUMN_MESSAGE_UUID)),
                    cGetString(c, COLUMN_NAME),
                    cGetString(c, COLUMN_PHRASE),
                    cGetLong(c, COLUMN_TIMESTAMP),
                    cGetString(c, COLUMN_CONSULT_ID),
                    cGetString(c, COLUMN_AVATAR_PATH),
                    cGetBool(c, COLUMN_IS_READ),
                    cGetString(c, COLUMN_CONSULT_STATUS),
                    cGetBool(c, COLUMN_SEX)
            );
            c.close();
            return cp;
        }
        return null;
    }

    List<ChatItem> getChatItems(int offset, int limit) {
        List<ChatItem> items = new ArrayList<>();
        String query = String.format(Locale.US, "select * from (select * from %s order by %s desc limit %s offset %s) order by %s asc",
                TABLE_MESSAGES, COLUMN_TIMESTAMP, String.valueOf(limit), String.valueOf(offset), COLUMN_TIMESTAMP);
        Cursor c = getWritableDatabase().rawQuery(query, null);
        if (c.getCount() == 0) {
            c.close();
            return items;
        }
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            int type = cGetInt(c, COLUMN_MESSAGE_TYPE);
            if (type == MessageType.CONSULT_CONNECTED.ordinal()) {
                ConsultConnectionMessage cc = new ConsultConnectionMessage(
                        cGetString(c, COLUMN_MESSAGE_UUID),
                        cGetString(c, COLUMN_PROVIDER_ID),
                        cGetString(c, COLUMN_CONSULT_ID),
                        cGetString(c, COLUMN_CONNECTION_TYPE),
                        cGetString(c, COLUMN_NAME),
                        cGetBool(c, COLUMN_SEX),
                        cGetLong(c, COLUMN_TIMESTAMP),
                        cGetString(c, COLUMN_AVATAR_PATH),
                        cGetString(c, COLUMN_CONSULT_STATUS),
                        cGetString(c, COLUMN_CONSULT_TITLE),
                        cGetString(c, COLUMN_CONSULT_ORG_UNIT),
                        cGetBool(c, COLUMN_DISPLAY_MASSAGE));
                items.add(cc);
            } else if (type == MessageType.CONSULT_PHRASE.ordinal()) {
                FileDescription fd = getFd(cGetString(c, COLUMN_MESSAGE_UUID));
                ConsultPhrase cp = new ConsultPhrase(
                        cGetString(c, COLUMN_MESSAGE_UUID),
                        cGetString(c, COLUMN_PROVIDER_ID),
                        fd,
                        getQuote(cGetString(c, COLUMN_MESSAGE_UUID)),
                        cGetString(c, COLUMN_NAME),
                        cGetString(c, COLUMN_PHRASE),
                        cGetLong(c, COLUMN_TIMESTAMP),
                        cGetString(c, COLUMN_CONSULT_ID),
                        cGetString(c, COLUMN_AVATAR_PATH),
                        cGetBool(c, COLUMN_IS_READ),
                        cGetString(c, COLUMN_CONSULT_STATUS),
                        cGetBool(c, COLUMN_SEX)
                );
                items.add(cp);
            } else if (type == MessageType.USER_PHRASE.ordinal()) {
                FileDescription fd = getFd(cGetString(c, COLUMN_MESSAGE_UUID));
                UserPhrase up = new UserPhrase(
                        cGetString(c, COLUMN_MESSAGE_UUID),
                        cGetString(c, COLUMN_PROVIDER_ID),
                        cGetString(c, COLUMN_PHRASE),
                        getQuote(cGetString(c, COLUMN_MESSAGE_UUID)),
                        cGetLong(c, COLUMN_TIMESTAMP),
                        fd);
                int sentState = cGetInt(c, COLUMN_MESSAGE_SEND_STATE);
                up.setSentState(MessageState.fromOrdinal(sentState));
                items.add(up);
            } else if (type == MessageType.SURVEY.ordinal()) {
                long surveySendingId = cGetLong(c, COLUMN_SURVEY_SENDING_ID);
                Survey survey = new Survey(
                        surveySendingId,
                        cGetLong(c, COLUMN_SURVEY_HIDE_AFTER),
                        cGetLong(c, COLUMN_TIMESTAMP),
                        MessageState.fromOrdinal(cGetInt(c, COLUMN_MESSAGE_SEND_STATE))
                );
                survey.setQuestions(Collections.singletonList(getQuestion(surveySendingId)));
                items.add(survey);
            }
        }
        c.close();
        return items;
    }

    private void putQuote(String quotedByMessageUuid, Quote quote) {
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_QUOTE_UUID, quote.getUuid());
        cv.put(COLUMN_QUOTED_BY_MESSAGE_UUID_EXT, quotedByMessageUuid);
        cv.put(COLUMN_QUOTE_FROM, quote.getPhraseOwnerTitle());
        cv.put(COLUMN_QUOTE_BODY, quote.getText());
        cv.put(COLUMN_QUOTE_TIMESTAMP, quote.getTimeStamp());
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " from " + TABLE_QUOTE
                + " where " + COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " = ?", new String[]{quotedByMessageUuid});
        boolean existsInDb = c.getCount() > 0;
        if (existsInDb) {
            getWritableDatabase().update(TABLE_QUOTE, cv,
                    COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " = ? ", new String[]{quotedByMessageUuid});
        } else {
            getWritableDatabase().insert(TABLE_QUOTE, null, cv);
        }
        c.close();
        if (quote.getFileDescription() != null) {
            putFd(quote.getFileDescription(), quote.getUuid(), true);
        }
    }

    private Quote getQuote(String quotedByMessageUuid) {
        if (TextUtils.isEmpty(quotedByMessageUuid)) {
            return null;
        }
        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_QUOTE, COLUMN_QUOTED_BY_MESSAGE_UUID_EXT);
        Cursor c = getWritableDatabase().rawQuery(query, new String[]{quotedByMessageUuid});
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        FileDescription quoteFd = getFd(cGetString(c, COLUMN_QUOTE_UUID));
        Quote q = new Quote(cGetString(c, COLUMN_QUOTE_UUID),
                cGetString(c, COLUMN_QUOTE_FROM),
                cGetString(c, COLUMN_QUOTE_BODY),
                quoteFd,
                cGetLong(c, COLUMN_QUOTE_TIMESTAMP));
        c.close();
        return q;
    }

    List<Quote> getQuotes() {
        String query = String.format(Locale.US, "select * from %s order by %s desc", TABLE_QUOTE, COLUMN_QUOTE_TIMESTAMP);
        List<Quote> list = new ArrayList<>();
        Cursor c = getWritableDatabase().rawQuery(query, new String[]{});
        if (!c.moveToFirst()) {
            c.close();
            return list;
        }
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            FileDescription quoteFd = getFd(cGetString(c, COLUMN_QUOTE_UUID));
            Quote q = new Quote(cGetString(c, COLUMN_QUOTE_UUID),
                    cGetString(c, COLUMN_QUOTE_FROM),
                    cGetString(c, COLUMN_QUOTE_BODY),
                    quoteFd,
                    cGetLong(c, COLUMN_QUOTE_TIMESTAMP));

            list.add(q);
        }
        c.close();
        return list;
    }

    private FileDescription getFd(String messageUuid) {
        if (TextUtils.isEmpty(messageUuid)) {
            return null;
        }
        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_FILE_DESCRIPTION, COLUMN_FD_MESSAGE_UUID_EXT);
        Cursor c = getWritableDatabase().rawQuery(query, new String[]{messageUuid});
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        int progress = cGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS);
        FileDescription fd = new FileDescription(cGetString(c, COLUMN_FD_FROM),
                cGetString(c, COLUMN_FD_PATH),
                cGetLong(c, COLUMN_FD_SIZE),
                cGetLong(c, COLUMN_FD_TIMESTAMP));
        fd.setDownloadProgress(progress);
        fd.setDownloadPath(cGetString(c, COLUMN_FD_URL));
        fd.setIncomingName(cGetString(c, COLUMN_FD_FILENAME));
        c.close();
        return fd;
    }

    List<FileDescription> getFd() {
        String query = String.format(Locale.US, "select * from %s order by %s desc", TABLE_FILE_DESCRIPTION, COLUMN_FD_TIMESTAMP);
        List<FileDescription> list = new ArrayList<>();
        Cursor c = getWritableDatabase().rawQuery(query, new String[]{});
        if (!c.moveToFirst()) {
            c.close();
            return list;
        }
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Integer progress = cGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS);
            FileDescription fd = new FileDescription(cGetString(c, COLUMN_FD_FROM),
                    cGetString(c, COLUMN_FD_PATH),
                    cGetLong(c, COLUMN_FD_SIZE),
                    cGetLong(c, COLUMN_FD_TIMESTAMP));

            fd.setDownloadProgress(progress);
            fd.setIncomingName(cGetString(c, COLUMN_FD_FILENAME));
            fd.setDownloadPath(cGetString(c, COLUMN_FD_URL));
            list.add(fd);
        }
        c.close();
        return list;
    }

    private QuestionDTO getQuestion(long surveySendingId) {
        String query = "select * from " + TABLE_QUESTIONS + " where " + COLUMN_QUESTION_SURVEY_SENDING_ID_EXT + " = ?";
        Cursor c = getWritableDatabase().rawQuery(query, new String[]{String.valueOf(surveySendingId)});
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        QuestionDTO question = new QuestionDTO();
        question.setPhraseTimeStamp(cGetLong(c, COLUMN_TIMESTAMP));
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
        c.close();
        return question;
    }

    int getMessagesCount() {
        Cursor c = getWritableDatabase().rawQuery(String.format(Locale.US, "select count(%s) from %s", COLUMN_TABLE_ID, TABLE_MESSAGES), null);
        if (c.getCount() == 0) {
            c.close();
            return 0;
        }
        c.moveToFirst();
        int i = c.getInt(0);
        c.close();
        return i;
    }

    void cleanMessagesTable() {
        getWritableDatabase().execSQL("delete  from " + TABLE_MESSAGES);
    }

    void cleanQuotes() {
        getWritableDatabase().execSQL("delete  from " + TABLE_QUOTE);
    }

    void cleanFD() {
        getWritableDatabase().execSQL("delete from " + TABLE_FILE_DESCRIPTION);
    }

    void setAllRead() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_TYPE + "  = "
                + MessageType.CONSULT_PHRASE.ordinal()
                + " and " + COLUMN_IS_READ + " = ?", new String[]{String.valueOf(0)});
    }

    void setMessageWereRead(String providerId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        String args[] = new String[1];
        args[0] = providerId;
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_TYPE + "  = "
                + MessageType.CONSULT_PHRASE.ordinal()
                + " and " + COLUMN_PROVIDER_ID + " = ? ", args);
    }

    List<String> getUnreadMessagesProviderIds() {
        ArrayList<String> ids = new ArrayList<>();
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_PROVIDER_ID +
                " from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal()
                + " and " + COLUMN_IS_READ + " = 0"
                + " order by " + COLUMN_TIMESTAMP + " asc", null);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            ids.add(c.getString(0));
        }
        c.close();
        return ids;
    }

    private void putFd(FileDescription fileDescription, String fdMessageUuid, boolean isFromQuote) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_MESSAGE_UUID_EXT, fdMessageUuid);
        cv.put(COLUMN_FD_FROM, fileDescription.getFileSentTo());
        if (!TextUtils.isEmpty(fileDescription.getFilePath())) {
            cv.put(COLUMN_FD_PATH, fileDescription.getFilePath());
        }
        cv.put(COLUMN_FD_URL, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
        cv.put(COLUMN_FD_IS_FROM_QUOTE, isFromQuote);
        cv.put(COLUMN_FD_FILENAME, fileDescription.getIncomingName());
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_FD_MESSAGE_UUID_EXT + " from " + TABLE_FILE_DESCRIPTION
                + " where " + COLUMN_FD_MESSAGE_UUID_EXT + " = ?", new String[]{fdMessageUuid});
        boolean existsInDb = c.getCount() > 0;
        if (existsInDb) {
            getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv,
                    COLUMN_FD_MESSAGE_UUID_EXT + " = ? ", new String[]{fdMessageUuid});
        } else {
            getWritableDatabase().insert(TABLE_FILE_DESCRIPTION, null, cv);
        }
        c.close();
    }

    void updateFd(FileDescription fileDescription) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_FROM, fileDescription.getFileSentTo());
        cv.put(COLUMN_FD_PATH, fileDescription.getFilePath());
        cv.put(COLUMN_FD_URL, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
        cv.put(COLUMN_FD_FILENAME, fileDescription.getIncomingName());
        getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv,
                "" + COLUMN_FD_FILENAME
                        + " like ? and " + COLUMN_FD_URL + " like ?",
                new String[]{fileDescription.getIncomingName(), fileDescription.getDownloadPath()});
    }

    String getLastOperatorAvatar(String id) {
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_AVATAR_PATH
                + " from " + TABLE_MESSAGES
                + " where " + COLUMN_CONSULT_ID + " =  ? "
                + " order by " + COLUMN_TIMESTAMP + " desc", new String[]{id});
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        return cGetString(c, COLUMN_AVATAR_PATH);
    }

    ConsultInfo getLastConsultInfo(@NonNull String id) {
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_AVATAR_PATH + ", " + COLUMN_NAME + ", " + COLUMN_CONSULT_STATUS
                + " from " + TABLE_MESSAGES
                + " where " + COLUMN_CONSULT_ID + " =  ? "
                + " order by " + COLUMN_TIMESTAMP + " desc", new String[]{id});
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        return new ConsultInfo(cGetString(c, COLUMN_NAME)
                , id
                , cGetString(c, COLUMN_CONSULT_STATUS)
                , cGetString(c, COLUMN_CONSULT_ORG_UNIT)
                , cGetString(c, COLUMN_AVATAR_PATH));
    }

    private static boolean cIsNull(Cursor c, String columnName) {
        return c.isNull(c.getColumnIndex(columnName));
    }

    private static boolean cGetBool(Cursor c, String columnName) {
        return cGetInt(c, columnName) == 1;
    }

    @Nullable
    private static String cGetString(Cursor c, String columnName) {
        return cIsNull(c, columnName) ? null : c.getString(c.getColumnIndex(columnName));
    }

    private static long cGetLong(Cursor c, String columnName) {
        return c.getLong(c.getColumnIndex(columnName));
    }

    private static int cGetInt(Cursor c, String columnName) {
        return cIsNull(c, columnName) ? 0 : c.getInt(c.getColumnIndex(columnName));
    }

    private enum MessageType {
        UNKNOWN,
        CONSULT_CONNECTED,
        CONSULT_PHRASE,
        USER_PHRASE,
        SURVEY
    }
}