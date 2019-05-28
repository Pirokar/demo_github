package im.threads.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import im.threads.model.ChatItem;
import im.threads.model.ChatPhrase;
import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultInfo;
import im.threads.model.ConsultPhrase;
import im.threads.model.FileDescription;
import im.threads.model.MessageState;
import im.threads.model.QuestionDTO;
import im.threads.model.Quote;
import im.threads.model.Survey;
import im.threads.model.UserPhrase;
import im.threads.utils.FileUtils;

/**
 * Created by yuri on 23.06.2016.
 * обертка для БД
 */
class MyOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "MyOpenHelper ";
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
    private static final String COLUMN_QUOTE_HEADER = "COLUMN_QUOTE_HEADER";
    private static final String COLUMN_QUOTE_BODY = "COLUMN_QUOTE_BODY";
    private static final String COLUMN_QUOTE_TIMESTAMP = "COLUMN_QUOTE_TIMESTAMP";
    private static final String COLUMN_QUOTE_MESSAGE_UUID_EXT = "COLUMN_QUOTE_MESSAGE_UUID_EXT";

    private static final String TABLE_FILE_DESCRIPTION = "TABLE_FILE_DESCRIPTION";
    private static final String COLUMN_FD_HEADER = "COLUMN_FD_HEADER";
    private static final String COLUMN_FD_PATH = "COLUMN_FD_PATH";
    private static final String COLUMN_FD_DOWNLOAD_PATH = "COLUMN_WEB_PATH";
    private static final String COLUMN_FD_DOWNLOAD_PROGRESS = "COLUMN_FD_DOWNLOAD_PROGRESS";
    private static final String COLUMN_FD_TIMESTAMP = "COLUMN_FD_TIMESTAMP";
    private static final String COLUMN_FD_SIZE = "COLUMN_FD_SIZE";
    private static final String COLUMN_FD_IS_FROM_QUOTE = "COLUMN_FD_IS_FROM_QUOTE";
    private static final String COLUMN_FD_INCOMING_FILENAME = "COLUMN_FD_INCOMING_FILENAME";
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

    private ArrayList<UserPhrase> cashedPhrases = new ArrayList<>();
    private long lastPhraseRequest = System.currentTimeMillis();

    public MyOpenHelper(Context context) {
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
                        + ", " + COLUMN_CONSULT_ORG_UNIT +  " text," +//COLUMN_CONSULT_ORG_UNIT
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

        db.execSQL(String.format(Locale.US, "create table %s ( " + // TABLE_QUOTE
                        " %s text, " +//header
                        " %s text, " +//body
                        " %s integer, " +//timestamp
                        " %s integer)" // message id
                , TABLE_QUOTE, COLUMN_QUOTE_HEADER, COLUMN_QUOTE_BODY, COLUMN_QUOTE_TIMESTAMP, COLUMN_QUOTE_MESSAGE_UUID_EXT));

        db.execSQL(String.format(Locale.US, "create table %s ( " + // TABLE_FILE_DESCRIPTION
                        "%s text, " +//header
                        "%s text, " +//body
                        "%s integer, " +//timestamp
                        "%s integer, " +// message id
                        "%s text, " + // web path
                        "%s integer, " + //size
                        "%s integer, " + // is from quote
                        "%s text," + // incoming filename
                        "%s integer)" // download progress
                , TABLE_FILE_DESCRIPTION, COLUMN_FD_HEADER, COLUMN_FD_PATH, COLUMN_FD_TIMESTAMP,
                COLUMN_FD_MESSAGE_UUID_EXT, COLUMN_FD_DOWNLOAD_PATH, COLUMN_FD_SIZE, COLUMN_FD_IS_FROM_QUOTE,
                COLUMN_FD_INCOMING_FILENAME, COLUMN_FD_DOWNLOAD_PROGRESS));

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
        cv.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_CONSULT_PHRASE.ordinal());
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
            cv.clear();
            cv.put(COLUMN_QUOTE_MESSAGE_UUID_EXT, phrase.getUuid());
            cv.put(COLUMN_QUOTE_HEADER, phrase.getQuote().getPhraseOwnerTitle());
            cv.put(COLUMN_QUOTE_BODY, phrase.getQuote().getText());
            cv.put(COLUMN_QUOTE_TIMESTAMP, phrase.getQuote().getTimeStamp());
            getWritableDatabase().insert(TABLE_QUOTE, null, cv);
            if (phrase.getQuote().getFileDescription() != null) {
                putFd(phrase.getQuote().getFileDescription(), phrase.getUuid(), true);
            }
        }
        c.close();
    }

    private void insertOrUpdateUserPhrase(UserPhrase phrase) {

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_UUID, phrase.getUuid());
        cv.put(COLUMN_PROVIDER_ID, phrase.getProviderId());
        cv.put(COLUMN_PHRASE, phrase.getPhrase());
        cv.put(COLUMN_TIMESTAMP, phrase.getTimeStamp());
        cv.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_USER_PHRASE.ordinal());
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
            cv.clear();
            cv.put(COLUMN_QUOTE_MESSAGE_UUID_EXT, phrase.getUuid());
            cv.put(COLUMN_QUOTE_HEADER, phrase.getQuote().getPhraseOwnerTitle());
            cv.put(COLUMN_QUOTE_BODY, phrase.getQuote().getText());
            cv.put(COLUMN_QUOTE_TIMESTAMP, phrase.getQuote().getTimeStamp());
            getWritableDatabase().insert(TABLE_QUOTE, null, cv);
            if (phrase.getQuote().getFileDescription() != null) {
                putFd(phrase.getQuote().getFileDescription(), phrase.getUuid(), true);
            }
        }
        c.close();
    }


    void setUserPhraseStateByProviderId(String providerId, MessageState messageState) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_SEND_STATE, messageState.ordinal());
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_PROVIDER_ID + " = ?", new String[]{providerId});
    }

    void setUserPhraseProviderId(String uuid, String providerId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PROVIDER_ID, providerId);
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_UUID + " = ?", new String[]{uuid});
    }

    void putConsultConnected(ConsultConnectionMessage consultConnectionMessage) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, consultConnectionMessage.getName());
        cv.put(COLUMN_TIMESTAMP, consultConnectionMessage.getTimeStamp());
        cv.put(COLUMN_AVATAR_PATH, consultConnectionMessage.getAvatarPath());
        cv.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_CONSULT_CONNECTED.ordinal());
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
                new String[]{consultConnectionMessage.getUuid(), String.valueOf(MessageTypes.TYPE_CONSULT_CONNECTED.ordinal())});

        boolean isDuplicate = c.getCount() > 0;
        c.close();
        if (!isDuplicate) {
            getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
        } else {
            getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_UUID + " = ? ", new String[]{consultConnectionMessage.getUuid()});
        }
    }

    public void insertOrUpdateSurvey(Survey survey) {

        Cursor cSurvey = getWritableDatabase().rawQuery("select " + COLUMN_SURVEY_SENDING_ID
                        + " from " + TABLE_MESSAGES
                        + " where " + COLUMN_SURVEY_SENDING_ID + " = ? and " + COLUMN_MESSAGE_TYPE + " = ? ",
                new String[]{String.valueOf(survey.getSendingId()), String.valueOf(MessageTypes.TYPE_SURVEY.ordinal())});

        boolean surveyExists = cSurvey.getCount() > 0;
        cSurvey.close();

        ContentValues surveyValues = new ContentValues();
        surveyValues.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_SURVEY.ordinal());
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

    public List<ChatPhrase> getSortedPhrases(String query) {
        List<ChatPhrase> list = new ArrayList<>();
        if (query == null) return list;
        List<ChatItem> chatItems = getChatItems(0, -1);
        for (ChatItem chatItem : chatItems) {

            if (chatItem instanceof UserPhrase) {
                if (((UserPhrase) chatItem).getPhraseText() != null
                        && ((UserPhrase) chatItem).getPhraseText().toLowerCase().contains(query.toLowerCase())) {
                    list.add((UserPhrase) chatItem);
                }
            }
            if (chatItem instanceof ConsultPhrase) {
                if (((ConsultPhrase) chatItem).getPhraseText() != null
                        && ((ConsultPhrase) chatItem).getPhraseText().toLowerCase().contains(query.toLowerCase())) {

                    list.add((ConsultPhrase) chatItem);
                }
            }
        }
        return list;
    }

    ConsultPhrase getLastUnreadPhrase() {
        Cursor c = getWritableDatabase().rawQuery("select * from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_TYPE + " = " + MessageTypes.TYPE_CONSULT_PHRASE.ordinal()
                + " and " + COLUMN_IS_READ + " = 0 order by " + COLUMN_TIMESTAMP + " desc", new String[]{});
        if (c.getCount() > 0) {
            c.moveToFirst();

            Pair<Boolean, FileDescription> fd = getFd(cGetString(c, COLUMN_MESSAGE_UUID));

            ConsultPhrase cp = new ConsultPhrase(
                    cGetString(c, COLUMN_MESSAGE_UUID),
                    cGetString(c, COLUMN_PROVIDER_ID),
                    fd != null && !fd.first ? fd.second : null,
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

    public List<ChatItem> getChatItems(int offset, int limit) {
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

            if (type == MessageTypes.TYPE_CONSULT_CONNECTED.ordinal()) {

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

            } else if (type == MessageTypes.TYPE_CONSULT_PHRASE.ordinal()) {

                Pair<Boolean, FileDescription> fd = getFd(cGetString(c, COLUMN_MESSAGE_UUID));

                ConsultPhrase cp = new ConsultPhrase(
                        cGetString(c, COLUMN_MESSAGE_UUID),
                        cGetString(c, COLUMN_PROVIDER_ID),
                        fd != null && !fd.first ? fd.second : null,
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

            } else if (type == MessageTypes.TYPE_USER_PHRASE.ordinal()) {

                Pair<Boolean, FileDescription> fd = getFd(cGetString(c, COLUMN_MESSAGE_UUID));

                UserPhrase up = new UserPhrase(
                        cGetString(c, COLUMN_MESSAGE_UUID),
                        cGetString(c, COLUMN_PROVIDER_ID),
                        cGetString(c, COLUMN_PHRASE),
                        getQuote(cGetString(c, COLUMN_MESSAGE_UUID)),
                        cGetLong(c, COLUMN_TIMESTAMP),
                        fd != null && !fd.first ? fd.second : null);

                int sentState = cGetInt(c, COLUMN_MESSAGE_SEND_STATE);
                up.setSentState(MessageState.fromOrdinal(sentState));

                items.add(up);
            } else if (type == MessageTypes.TYPE_SURVEY.ordinal()) {

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

    private Quote getQuote(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_QUOTE, COLUMN_QUOTE_MESSAGE_UUID_EXT);
        Cursor c = getWritableDatabase().rawQuery(query, new String[]{uuid});
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        Pair<Boolean, FileDescription> quoteFd = getFd(uuid);

        Quote q = new Quote(cGetString(c, COLUMN_QUOTE_HEADER),
                cGetString(c, COLUMN_QUOTE_BODY),
                quoteFd != null && quoteFd.first ? quoteFd.second : null,
                cGetLong(c, COLUMN_QUOTE_TIMESTAMP));

        c.close();
        return q;
    }

    public List<ChatPhrase> queryFiles(String query) {
        List<ChatPhrase> list = getSortedPhrases("");
        List<ChatPhrase> out = new ArrayList<>();
        for (Iterator<ChatPhrase> iter = list.iterator(); iter.hasNext(); ) {
            ChatPhrase cp = iter.next();
            if (cp.getFileDescription() != null) {
                FileDescription fd = cp.getFileDescription();
                if (fd.getIncomingName() != null && fd.getIncomingName().toLowerCase().contains(query.toLowerCase())) {
                    out.add(cp);
                } else if (fd.getFilePath() != null
                        && FileUtils.getLastPathSegment(fd.getFilePath()).toLowerCase().contains(query.toLowerCase())) {
                    out.add(cp);
                }
            } else if (cp.getQuote() != null && cp.getQuote().getFileDescription() != null) {
                FileDescription fd = cp.getQuote().getFileDescription();
                if (fd.getIncomingName() != null && fd.getIncomingName().toLowerCase().contains(query.toLowerCase())) {
                    out.add(cp);
                } else if (fd.getFilePath() != null
                        && FileUtils.getLastPathSegment(fd.getFilePath()).toLowerCase().contains(query.toLowerCase())) {
                    out.add(cp);
                }
            }
        }
        return out;
    }

    private Pair<Boolean, FileDescription> getFd(String uuid) {

        if (TextUtils.isEmpty(uuid)) {
            return null;
        }

        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_FILE_DESCRIPTION, COLUMN_FD_MESSAGE_UUID_EXT);
        Cursor c = getWritableDatabase().rawQuery(query, new String[]{uuid});
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        Integer progress = cGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS);

        FileDescription fd = new FileDescription(cGetString(c, COLUMN_FD_HEADER),
                cGetString(c, COLUMN_FD_PATH),
                cGetLong(c, COLUMN_FD_SIZE),
                cGetLong(c, COLUMN_FD_TIMESTAMP));

        fd.setDownloadProgress(progress);
        fd.setDownloadPath(cGetString(c, COLUMN_FD_DOWNLOAD_PATH));
        fd.setIncomingName(cGetString(c, COLUMN_FD_INCOMING_FILENAME));
        boolean isFromQuote = cGetBool(c, COLUMN_FD_IS_FROM_QUOTE);

        c.close();
        return new Pair<>(isFromQuote, fd);
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

            FileDescription fd = new FileDescription(cGetString(c, COLUMN_FD_HEADER),
                    cGetString(c, COLUMN_FD_PATH),
                    cGetLong(c, COLUMN_FD_SIZE),
                    cGetLong(c, COLUMN_FD_TIMESTAMP));

            fd.setDownloadProgress(progress);
            fd.setIncomingName(cGetString(c, COLUMN_FD_INCOMING_FILENAME));
            fd.setDownloadPath(cGetString(c, COLUMN_FD_DOWNLOAD_PATH));

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

    public void cleanMessagesTable() {
        getWritableDatabase().execSQL("delete  from " + TABLE_MESSAGES);
    }

    public void cleanQuotes() {
        getWritableDatabase().execSQL("delete  from " + TABLE_QUOTE);
    }

    public void cleanFD() {
        getWritableDatabase().execSQL("delete from " + TABLE_FILE_DESCRIPTION);
    }

    private enum MessageTypes {
        TYPE_UNKNOWN,
        TYPE_CONSULT_CONNECTED,
        TYPE_CONSULT_PHRASE,
        TYPE_USER_PHRASE,
        TYPE_SURVEY;
    }

    void setAllRead() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_TYPE + "  = "
                + MessageTypes.TYPE_CONSULT_PHRASE.ordinal()
                + " and " + COLUMN_IS_READ + " = ?", new String[]{String.valueOf(0)});
    }

    void setMessageWereRead(String providerId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        String args[] = new String[1];
        args[0] = providerId;
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_TYPE + "  = "
                + MessageTypes.TYPE_CONSULT_PHRASE.ordinal()
                + " and " + COLUMN_PROVIDER_ID + " = ? ", args);
    }

    List<String> getUnreadMessagesProviderIds() {
        ArrayList<String> ids = new ArrayList<>();
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_PROVIDER_ID +
                " from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_TYPE + " = " + MessageTypes.TYPE_CONSULT_PHRASE.ordinal()
                + " and " + COLUMN_IS_READ + " = 0"
                + " order by " + COLUMN_TIMESTAMP + " asc", null);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            ids.add(c.getString(0));
        }
        c.close();
        return ids;
    }

    private void putFd(FileDescription fileDescription, String id, boolean isFromQuote) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_MESSAGE_UUID_EXT, id);
        cv.put(COLUMN_FD_HEADER, fileDescription.getFileSentTo());
        cv.put(COLUMN_FD_PATH, fileDescription.getFilePath());
        cv.put(COLUMN_FD_DOWNLOAD_PATH, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
        cv.put(COLUMN_FD_IS_FROM_QUOTE, isFromQuote);
        cv.put(COLUMN_FD_INCOMING_FILENAME, fileDescription.getIncomingName());
        getWritableDatabase().insert(TABLE_FILE_DESCRIPTION, null, cv);
    }

    void updateFd(FileDescription fileDescription) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_HEADER, fileDescription.getFileSentTo());
        cv.put(COLUMN_FD_PATH, fileDescription.getFilePath());
        cv.put(COLUMN_FD_DOWNLOAD_PATH, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
        cv.put(COLUMN_FD_INCOMING_FILENAME, fileDescription.getIncomingName());
        getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv,
                "" + COLUMN_FD_INCOMING_FILENAME
                        + " like ? and " + COLUMN_FD_DOWNLOAD_PATH + " like ?",
                new String[]{fileDescription.getIncomingName(), fileDescription.getDownloadPath()});
    }

    ChatPhrase getChatphraseByDescription(FileDescription fileDescription) {

        if (fileDescription == null) return null;

        ChatPhrase cp = null;
        Cursor c = getWritableDatabase().query(true, TABLE_FILE_DESCRIPTION,
                new String[]{COLUMN_FD_MESSAGE_UUID_EXT},
                COLUMN_FD_SIZE + " = " + fileDescription.getSize()
                        + " and " + COLUMN_FD_DOWNLOAD_PATH + " like " + fileDescription.getDownloadPath()
                        + " and " + COLUMN_FD_HEADER + " like " + fileDescription.getFrom(),
                new String[]{},
                null, null, null, null);

        if (c.getCount() > 0) {

            c.moveToFirst();
            String uuid = cGetString(c, COLUMN_FD_MESSAGE_UUID_EXT);

            c = getWritableDatabase().rawQuery("select * from " + TABLE_MESSAGES
                    + " where " + COLUMN_MESSAGE_UUID + " like " + uuid, new String[]{});

            c.moveToFirst();

            int type = cGetInt(c, COLUMN_MESSAGE_TYPE);

            if (type == MessageTypes.TYPE_CONSULT_PHRASE.ordinal()) {
                Pair<Boolean, FileDescription> fd = getFd(cGetString(c, COLUMN_MESSAGE_UUID));

                cp = new ConsultPhrase(
                        cGetString(c, COLUMN_MESSAGE_UUID),
                        cGetString(c, COLUMN_PROVIDER_ID),
                        fd != null && !fd.first ? fd.second : null,
                        getQuote(cGetString(c, COLUMN_MESSAGE_UUID)),
                        cGetString(c, COLUMN_NAME),
                        cGetString(c, COLUMN_PHRASE),
                        cGetLong(c, COLUMN_TIMESTAMP),
                        cGetString(c, COLUMN_CONSULT_ID),
                        cGetString(c, COLUMN_AVATAR_PATH),
                        cGetBool(c, COLUMN_IS_READ),
                        cGetString(c, COLUMN_CONSULT_STATUS),
                        cGetBool(c, COLUMN_SEX));

            } else if (type == MessageTypes.TYPE_USER_PHRASE.ordinal()) {
                Pair<Boolean, FileDescription> fd = getFd(cGetString(c, COLUMN_MESSAGE_UUID));

                cp = new UserPhrase(
                        cGetString(c, COLUMN_MESSAGE_UUID),
                        cGetString(c, COLUMN_PROVIDER_ID),
                        cGetString(c, COLUMN_PHRASE),
                        getQuote(cGetString(c, COLUMN_MESSAGE_UUID)),
                        cGetLong(c, COLUMN_TIMESTAMP),
                        fd != null && !fd.first ? fd.second : null);

                int sentState = cGetInt(c, COLUMN_MESSAGE_SEND_STATE);
                ((UserPhrase) cp).setSentState(MessageState.fromOrdinal(sentState));
            }
        }
        c.close();
        return cp;
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

    void cleanDb() {
        getWritableDatabase().execSQL("delete * from " + TABLE_MESSAGES);
        getWritableDatabase().execSQL("delete * from " + TABLE_FILE_DESCRIPTION);
        getWritableDatabase().execSQL("delete * from " + TABLE_QUOTE);
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

    public static boolean cIsNull(Cursor c, String columnName) {
        return c.isNull(c.getColumnIndex(columnName));
    }

    private boolean cGetBool(Cursor c, String columnName) {
        return cGetInt(c, columnName) == 1;
    }

    /**
     * @param c          Cursor
     * @param columnName
     * @return String or null
     */
    public static @Nullable
    String cGetString(Cursor c, String columnName) {
        return cIsNull(c, columnName) ? null : c.getString(c.getColumnIndex(columnName));
    }

    public static long cGetLong(Cursor c, String columnName) {
        return c.getLong(c.getColumnIndex(columnName));
    }

    public static int cGetInt(Cursor c, String columnName) {
        return cIsNull(c, columnName) ? 0 : c.getInt(c.getColumnIndex(columnName));
    }
}
