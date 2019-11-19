package im.threads.internal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import im.threads.internal.model.ChatItem;
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
    private static final int VERSION = 7;
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
    private static final String COLUMN_SURVEY_SENDING_ID = "COLUMN_SURVEY_SENDING_ID";
    private static final String COLUMN_SURVEY_HIDE_AFTER = "COLUMN_SURVEY_HIDE_AFTER";


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
    private static final String COLUMN_FD_SELFIE = "COLUMN_FD_SELFIE";

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
                + COLUMN_FD_DOWNLOAD_PROGRESS + " integer, "
                + COLUMN_FD_SELFIE + " integer)"
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

    /**
     * {@value TABLE_MESSAGES}
     */

    List<ChatItem> getChatItems(int offset, int limit) {
        List<ChatItem> items = new ArrayList<>();
        String query = String.format(Locale.US, "select * from (select * from %s order by %s desc limit %s offset %s) order by %s asc",
                TABLE_MESSAGES, COLUMN_TIMESTAMP, String.valueOf(limit), String.valueOf(offset), COLUMN_TIMESTAMP);
        try (Cursor c = getWritableDatabase().rawQuery(query, null)) {
            if (c.getCount() == 0) {
                return items;
            }
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ChatItem chatItem = getChatItem(c);
                if (chatItem != null) {
                    items.add(chatItem);
                }
            }
        }
        return items;
    }

    @Nullable
    ChatItem getChatItem(String messageUuid) {
        String sql = "select * from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_UUID + " = ?"
                + " order by " + COLUMN_TIMESTAMP + " desc";
        String[] selectionArgs = new String[]{messageUuid};
        try (Cursor c = getWritableDatabase().rawQuery(sql, selectionArgs)) {
            if (c.moveToFirst()) {
                return getChatItem(c);
            }
        }
        return null;
    }

    @Nullable
    Survey getSurvey(long sendingId) {
        String sql = "select * from " + TABLE_MESSAGES
                + " where " + COLUMN_SURVEY_SENDING_ID + " = ?"
                + " order by " + COLUMN_TIMESTAMP + " desc";
        String[] selectionArgs = new String[]{String.valueOf(sendingId)};
        try (Cursor c = getWritableDatabase().rawQuery(sql, selectionArgs)) {
            if (c.moveToFirst()) {
                return getSurvey(c);
            }
        }
        return null;
    }

    List<String> getUnreadMessagesProviderIds() {
        String sql = "select " + COLUMN_PROVIDER_ID +
                " from " + TABLE_MESSAGES +
                " where " + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal() + " and " + COLUMN_IS_READ + " = 0" +
                " order by " + COLUMN_TIMESTAMP + " asc";
        ArrayList<String> ids = new ArrayList<>();
        try (Cursor c = getWritableDatabase().rawQuery(sql, null)) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ids.add(c.getString(0));
            }
        }
        return ids;
    }

    int getMessagesCount() {
        try (Cursor c = getWritableDatabase().rawQuery(String.format(Locale.US, "select count(%s) from %s", COLUMN_TABLE_ID, TABLE_MESSAGES), null)) {
            if (c.getCount() == 0) {
                return 0;
            }
            c.moveToFirst();
            return c.getInt(0);
        }
    }

    @Nullable
    ConsultPhrase getLastConsultPhrase() {
        String sql = "select * from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal()
                + " order by " + COLUMN_TIMESTAMP + " desc";
        try (Cursor c = getWritableDatabase().rawQuery(sql, new String[]{})) {
            if (c.moveToFirst()) {
                return getConsultPhrase(c);
            }
        }
        return null;
    }

    @Nullable
    ConsultInfo getLastConsultInfo(@NonNull String id) {
        String sql = "select " + COLUMN_AVATAR_PATH + ", " + COLUMN_NAME + ", " + COLUMN_CONSULT_STATUS
                + " from " + TABLE_MESSAGES
                + " where " + COLUMN_CONSULT_ID + " =  ? "
                + " order by " + COLUMN_TIMESTAMP + " desc";
        String[] selectionArgs = new String[]{id};
        try (Cursor c = getWritableDatabase().rawQuery(sql, selectionArgs)) {
            if (c.moveToFirst()) {
                return new ConsultInfo(
                        cGetString(c, COLUMN_NAME),
                        id,
                        cGetString(c, COLUMN_CONSULT_STATUS),
                        cGetString(c, COLUMN_CONSULT_ORG_UNIT),
                        cGetString(c, COLUMN_AVATAR_PATH)
                );
            }
        }
        return null;
    }

    boolean putChatItem(ChatItem chatItem) {
        if (chatItem instanceof ConsultConnectionMessage) {
            insertOrUpdateConsultConnectionMessage((ConsultConnectionMessage) chatItem);
            return true;
        }
        if (chatItem instanceof ConsultPhrase) {
            insertOrUpdateConsultPhrase((ConsultPhrase) chatItem);
            return true;
        }
        if (chatItem instanceof UserPhrase) {
            insertOrUpdateUserPhrase((UserPhrase) chatItem);
            return true;
        }
        if (chatItem instanceof Survey) {
            insertOrUpdateSurvey((Survey) chatItem);
        }
        return false;
    }

    void setAllConsultMessagesWereRead() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        String whereClause = COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal() +
                " and " + COLUMN_IS_READ + " = 0";
        getWritableDatabase().update(TABLE_MESSAGES, cv, whereClause, null);
    }

    void setConsultMessageWasRead(String providerId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        String whereClause = COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal() +
                " and " + COLUMN_PROVIDER_ID + " = ? " +
                " and " + COLUMN_IS_READ + " = 0";
        String[] whereArgs = new String[]{providerId};
        getWritableDatabase().update(TABLE_MESSAGES, cv, whereClause, whereArgs);
    }

    @WorkerThread
    void setUserPhraseStateByProviderId(String providerId, MessageState messageState) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_SEND_STATE, messageState.ordinal());
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_PROVIDER_ID + " = ?", new String[]{providerId});
    }

    /**
     * {@value TABLE_FILE_DESCRIPTION}
     */

    List<FileDescription> getAllFileDescriptions() {
        String query = String.format(Locale.US, "select * from %s order by %s desc", TABLE_FILE_DESCRIPTION, COLUMN_FD_TIMESTAMP);
        List<FileDescription> list = new ArrayList<>();
        try (Cursor c = getWritableDatabase().rawQuery(query, new String[]{})) {
            if (!c.moveToFirst()) {
                return list;
            }
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                FileDescription fd = new FileDescription(
                        cGetString(c, COLUMN_FD_FROM),
                        cGetString(c, COLUMN_FD_PATH),
                        cGetLong(c, COLUMN_FD_SIZE),
                        cGetLong(c, COLUMN_FD_TIMESTAMP)
                );
                fd.setDownloadProgress(cGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS));
                fd.setIncomingName(cGetString(c, COLUMN_FD_FILENAME));
                fd.setDownloadPath(cGetString(c, COLUMN_FD_URL));
                fd.setSelfie(cGetBool(c, COLUMN_FD_SELFIE));
                list.add(fd);
            }
            return list;
        }
    }

    void updateFileDescription(@NonNull FileDescription fileDescription) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_FROM, fileDescription.getFrom());
        cv.put(COLUMN_FD_PATH, fileDescription.getFilePath());
        cv.put(COLUMN_FD_URL, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
        cv.put(COLUMN_FD_FILENAME, fileDescription.getIncomingName());
        cv.put(COLUMN_FD_SELFIE, fileDescription.isSelfie());
        getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv,
                "" + COLUMN_FD_FILENAME
                        + " like ? and " + COLUMN_FD_URL + " like ?",
                new String[]{fileDescription.getIncomingName(), fileDescription.getDownloadPath()});
    }

    void cleanDatabase() {
        cleanFileDescriptions();
        cleanMessages();
        cleanQuotes();
        cleanQuestions();
    }

    /**
     * {@value TABLE_MESSAGES}
     */

    @Nullable
    private ChatItem getChatItem(Cursor c) {
        int type = cGetInt(c, COLUMN_MESSAGE_TYPE);
        if (type == MessageType.CONSULT_CONNECTED.ordinal()) {
            return new ConsultConnectionMessage(
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
        } else if (type == MessageType.CONSULT_PHRASE.ordinal()) {
            return getConsultPhrase(c);
        } else if (type == MessageType.USER_PHRASE.ordinal()) {
            return getUserPhrase(c);
        } else if (type == MessageType.SURVEY.ordinal()) {
            return getSurvey(c);
        }
        return null;
    }

    private ConsultPhrase getConsultPhrase(Cursor c) {
        return new ConsultPhrase(
                cGetString(c, COLUMN_MESSAGE_UUID),
                cGetString(c, COLUMN_PROVIDER_ID),
                getFileDescription(cGetString(c, COLUMN_MESSAGE_UUID)),
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
    }

    private UserPhrase getUserPhrase(Cursor c) {
        return new UserPhrase(
                cGetString(c, COLUMN_MESSAGE_UUID),
                cGetString(c, COLUMN_PROVIDER_ID),
                cGetString(c, COLUMN_PHRASE),
                getQuote(cGetString(c, COLUMN_MESSAGE_UUID)),
                cGetLong(c, COLUMN_TIMESTAMP),
                getFileDescription(cGetString(c, COLUMN_MESSAGE_UUID)),
                MessageState.fromOrdinal(cGetInt(c, COLUMN_MESSAGE_SEND_STATE))
        );
    }

    private Survey getSurvey(Cursor c) {
        long surveySendingId = cGetLong(c, COLUMN_SURVEY_SENDING_ID);
        Survey survey = new Survey(
                surveySendingId,
                cGetLong(c, COLUMN_SURVEY_HIDE_AFTER),
                cGetLong(c, COLUMN_TIMESTAMP),
                MessageState.fromOrdinal(cGetInt(c, COLUMN_MESSAGE_SEND_STATE))
        );
        survey.setQuestions(Collections.singletonList(getQuestion(surveySendingId)));
        return survey;
    }

    private void insertOrUpdateConsultConnectionMessage(ConsultConnectionMessage consultConnectionMessage) {
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
        insertOrUpdateMessage(cv);
    }

    private void insertOrUpdateConsultPhrase(ConsultPhrase phrase) {
        ContentValues cv = new ContentValues();
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
        insertOrUpdateMessage(cv);
        if (phrase.getFileDescription() != null) {
            putFileDescription(phrase.getFileDescription(), phrase.getUuid(), false);
        }
        if (phrase.getQuote() != null) {
            putQuote(phrase.getUuid(), phrase.getQuote());
        }
    }

    private void insertOrUpdateUserPhrase(UserPhrase phrase) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_UUID, phrase.getUuid());
        cv.put(COLUMN_PROVIDER_ID, phrase.getProviderId());
        cv.put(COLUMN_PHRASE, phrase.getPhrase());
        cv.put(COLUMN_TIMESTAMP, phrase.getTimeStamp());
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.USER_PHRASE.ordinal());
        cv.put(COLUMN_MESSAGE_SEND_STATE, phrase.getSentState().ordinal());
        insertOrUpdateMessage(cv);
        if (phrase.getFileDescription() != null) {
            putFileDescription(phrase.getFileDescription(), phrase.getUuid(), false);
        }
        if (phrase.getQuote() != null) {
            putQuote(phrase.getUuid(), phrase.getQuote());
        }
    }

    private void insertOrUpdateSurvey(Survey survey) {
        String sql = "select " + COLUMN_SURVEY_SENDING_ID
                + " from " + TABLE_MESSAGES
                + " where " + COLUMN_SURVEY_SENDING_ID + " = ? and " + COLUMN_MESSAGE_TYPE + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(survey.getSendingId()), String.valueOf(MessageType.SURVEY.ordinal())};
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.SURVEY.ordinal());
        cv.put(COLUMN_SURVEY_SENDING_ID, survey.getSendingId());
        cv.put(COLUMN_SURVEY_HIDE_AFTER, survey.getHideAfter());
        cv.put(COLUMN_TIMESTAMP, survey.getTimeStamp());
        cv.put(COLUMN_MESSAGE_SEND_STATE, survey.getSentState().ordinal());
        try (Cursor c = getWritableDatabase().rawQuery(sql, selectionArgs)) {
            if (c.getCount() > 0) {
                getWritableDatabase().update(
                        TABLE_MESSAGES,
                        cv,
                        COLUMN_SURVEY_SENDING_ID + " = ? ",
                        new String[]{String.valueOf(survey.getSendingId())}
                );
            } else {
                getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
            }
        }
        for (QuestionDTO question : survey.getQuestions()) {
            putQuestion(question, survey.getSendingId());
        }
    }

    private void insertOrUpdateMessage(ContentValues cv) {
        String sql = "select " + COLUMN_MESSAGE_UUID +
                " from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_UUID + " = ?";
        String[] selectionArgs = new String[]{cv.getAsString(COLUMN_MESSAGE_UUID)};
        try (Cursor c = getWritableDatabase().rawQuery(sql, selectionArgs)) {
            if (c.getCount() > 0) {
                getWritableDatabase().update(
                        TABLE_MESSAGES,
                        cv,
                        COLUMN_MESSAGE_UUID + " = ? ",
                        new String[]{cv.getAsString(COLUMN_MESSAGE_UUID)}
                );
            } else {
                getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
            }
        }
    }

    /**
     * {@value TABLE_QUOTE}
     */

    private void putQuote(String quotedByMessageUuid, Quote quote) {
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(COLUMN_QUOTE_UUID, quote.getUuid());
        cv.put(COLUMN_QUOTED_BY_MESSAGE_UUID_EXT, quotedByMessageUuid);
        cv.put(COLUMN_QUOTE_FROM, quote.getPhraseOwnerTitle());
        cv.put(COLUMN_QUOTE_BODY, quote.getText());
        cv.put(COLUMN_QUOTE_TIMESTAMP, quote.getTimeStamp());
        try (Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " from " + TABLE_QUOTE
                + " where " + COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " = ?", new String[]{quotedByMessageUuid})) {
            boolean existsInDb = c.getCount() > 0;
            if (existsInDb) {
                getWritableDatabase().update(TABLE_QUOTE, cv,
                        COLUMN_QUOTED_BY_MESSAGE_UUID_EXT + " = ? ", new String[]{quotedByMessageUuid});
            } else {
                getWritableDatabase().insert(TABLE_QUOTE, null, cv);
            }
            if (quote.getFileDescription() != null) {
                putFileDescription(quote.getFileDescription(), quote.getUuid(), true);
            }
        }
    }

    private Quote getQuote(String quotedByMessageUuid) {
        if (TextUtils.isEmpty(quotedByMessageUuid)) {
            return null;
        }
        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_QUOTE, COLUMN_QUOTED_BY_MESSAGE_UUID_EXT);
        try (Cursor c = getWritableDatabase().rawQuery(query, new String[]{quotedByMessageUuid})) {
            if (c.moveToFirst()) {
                return new Quote(
                        cGetString(c, COLUMN_QUOTE_UUID),
                        cGetString(c, COLUMN_QUOTE_FROM),
                        cGetString(c, COLUMN_QUOTE_BODY),
                        getFileDescription(cGetString(c, COLUMN_QUOTE_UUID)),
                        cGetLong(c, COLUMN_QUOTE_TIMESTAMP)
                );
            }
        }
        return null;
    }

    List<Quote> getQuotes() {
        String query = String.format(Locale.US, "select * from %s order by %s desc", TABLE_QUOTE, COLUMN_QUOTE_TIMESTAMP);
        List<Quote> list = new ArrayList<>();
        try (Cursor c = getWritableDatabase().rawQuery(query, new String[]{})) {
            if (!c.moveToFirst()) {
                return list;
            }
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                list.add(
                        new Quote(
                                cGetString(c, COLUMN_QUOTE_UUID),
                                cGetString(c, COLUMN_QUOTE_FROM),
                                cGetString(c, COLUMN_QUOTE_BODY),
                                getFileDescription(cGetString(c, COLUMN_QUOTE_UUID)),
                                cGetLong(c, COLUMN_QUOTE_TIMESTAMP)
                        )
                );
            }
            return list;
        }
    }

    /**
     * {@value TABLE_FILE_DESCRIPTION}
     */

    @Nullable
    private FileDescription getFileDescription(String messageUuid) {
        if (TextUtils.isEmpty(messageUuid)) {
            return null;
        }
        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_FILE_DESCRIPTION, COLUMN_FD_MESSAGE_UUID_EXT);
        try (Cursor c = getWritableDatabase().rawQuery(query, new String[]{messageUuid})) {
            if (!c.moveToFirst()) {
                return null;
            }
            FileDescription fd = new FileDescription(
                    cGetString(c, COLUMN_FD_FROM),
                    cGetString(c, COLUMN_FD_PATH),
                    cGetLong(c, COLUMN_FD_SIZE),
                    cGetLong(c, COLUMN_FD_TIMESTAMP)
            );
            fd.setDownloadProgress(cGetInt(c, COLUMN_FD_DOWNLOAD_PROGRESS));
            fd.setDownloadPath(cGetString(c, COLUMN_FD_URL));
            fd.setSelfie(cGetBool(c, COLUMN_FD_SELFIE));
            fd.setIncomingName(cGetString(c, COLUMN_FD_FILENAME));
            return fd;
        }
    }

    private void putFileDescription(FileDescription fileDescription, String fdMessageUuid, boolean isFromQuote) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FD_MESSAGE_UUID_EXT, fdMessageUuid);
        cv.put(COLUMN_FD_FROM, fileDescription.getFrom());
        if (!TextUtils.isEmpty(fileDescription.getFilePath())) {
            cv.put(COLUMN_FD_PATH, fileDescription.getFilePath());
        }
        cv.put(COLUMN_FD_URL, fileDescription.getDownloadPath());
        cv.put(COLUMN_FD_TIMESTAMP, fileDescription.getTimeStamp());
        cv.put(COLUMN_FD_SIZE, fileDescription.getSize());
        cv.put(COLUMN_FD_DOWNLOAD_PROGRESS, fileDescription.getDownloadProgress());
        cv.put(COLUMN_FD_IS_FROM_QUOTE, isFromQuote);
        cv.put(COLUMN_FD_FILENAME, fileDescription.getIncomingName());
        cv.put(COLUMN_FD_SELFIE, fileDescription.isSelfie());
        String sql = "select " + COLUMN_FD_MESSAGE_UUID_EXT +
                " from " + TABLE_FILE_DESCRIPTION
                + " where " + COLUMN_FD_MESSAGE_UUID_EXT + " = ?";
        String[] selectionArgs = new String[]{fdMessageUuid};
        try (Cursor c = getWritableDatabase().rawQuery(sql, selectionArgs)) {
            boolean existsInDb = c.getCount() > 0;
            if (existsInDb) {
                getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv,
                        COLUMN_FD_MESSAGE_UUID_EXT + " = ? ", new String[]{fdMessageUuid});
            } else {
                getWritableDatabase().insert(TABLE_FILE_DESCRIPTION, null, cv);
            }
        }
    }


    /**
     * {@value TABLE_QUESTIONS}
     */
    private QuestionDTO getQuestion(long surveySendingId) {
        String query = "select * from " + TABLE_QUESTIONS + " where " + COLUMN_QUESTION_SURVEY_SENDING_ID_EXT + " = ?";
        try (Cursor c = getWritableDatabase().rawQuery(query, new String[]{String.valueOf(surveySendingId)})) {
            if (!c.moveToFirst()) {
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
            return question;
        }
    }

    private void putQuestion(QuestionDTO question, long surveySendingId) {
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
        questionCv.put(COLUMN_TIMESTAMP, question.getTimeStamp());
        try (Cursor questionCursor = getWritableDatabase().rawQuery(questionSql, questionSelectionArgs)) {
            if (questionCursor.getCount() > 0) {
                getWritableDatabase().update(
                        TABLE_QUESTIONS,
                        questionCv,
                        COLUMN_QUESTION_SENDING_ID + " = ? ",
                        new String[]{String.valueOf(question.getSendingId())}
                );
            } else {
                getWritableDatabase().insert(TABLE_QUESTIONS, null, questionCv);
            }
        }
    }

    private void cleanFileDescriptions() {
        getWritableDatabase().execSQL("delete from " + TABLE_FILE_DESCRIPTION);
    }

    private void cleanMessages() {
        getWritableDatabase().execSQL("delete from " + TABLE_MESSAGES);
    }

    private void cleanQuotes() {
        getWritableDatabase().execSQL("delete from " + TABLE_QUOTE);
    }

    private void cleanQuestions() {
        getWritableDatabase().execSQL("delete from " + TABLE_QUESTIONS);
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
