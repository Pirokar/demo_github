package im.threads.internal.database.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.QuestionDTO;
import im.threads.internal.model.SimpleSystemMessage;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.utils.ThreadsLogger;

public class MessagesTable extends Table {

    private static final String TAG = MessagesTable.class.getCanonicalName();

    private static final String TABLE_MESSAGES = "TABLE_MESSAGES";
    private static final String COLUMN_TABLE_ID = "TABLE_ID";
    private static final String COLUMN_MESSAGE_UUID = "COLUMN_MESSAGE_UUID";
    private static final String COLUMN_PROVIDER_ID = "COLUMN_PROVIDER_ID";
    private static final String COLUMN_PROVIDER_IDS = "COLUMN_PROVIDER_IDS";
    private static final String COLUMN_TIMESTAMP = "COLUMN_TIMESTAMP";
    private static final String COLUMN_PHRASE = "COLUMN_PHRASE";
    private static final String COLUMN_FORMATTED_PHRASE = "COLUMN_FORMATTED_PHRASE";
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
    private static final String COLUMN_DISPLAY_MESSAGE = "COLUMN_DISPLAY_MESSAGE";
    private static final String COLUMN_SURVEY_SENDING_ID = "COLUMN_SURVEY_SENDING_ID";
    private static final String COLUMN_SURVEY_HIDE_AFTER = "COLUMN_SURVEY_HIDE_AFTER";

    private final FileDescriptionsTable fileDescriptionTable;
    private final QuotesTable quotesTable;
    private final QuickRepliesTable quickRepliesTable;
    private final QuestionsTable questionsTable;

    public MessagesTable(
            FileDescriptionsTable fileDescriptionTable,
            QuotesTable quotesTable,
            QuickRepliesTable quickRepliesTable,
            QuestionsTable questionsTable) {
        this.fileDescriptionTable = fileDescriptionTable;
        this.quotesTable = quotesTable;
        this.quickRepliesTable = quickRepliesTable;
        this.questionsTable = questionsTable;
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL(String.format(Locale.US, "create table %s " +//messages table
                        "( %s integer primary key autoincrement," +//id column
                        " %s integer, " +//timestamp
                        " %s text, " +//phrase
                        " %s text, " + //COLUMN_FORMATTED_PHRASE
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
                        "%s text, " + //COLUMN_PROVIDER_ID
                        "%s text " //COLUMN_PROVIDER_IDS
                        + ", " + COLUMN_DISPLAY_MESSAGE + " integer"
                        + ", " + COLUMN_SURVEY_SENDING_ID + " integer"
                        + ", " + COLUMN_SURVEY_HIDE_AFTER + " integer"
                        + ")",
                TABLE_MESSAGES, COLUMN_TABLE_ID, COLUMN_TIMESTAMP
                , COLUMN_PHRASE, COLUMN_FORMATTED_PHRASE, COLUMN_MESSAGE_TYPE, COLUMN_NAME, COLUMN_AVATAR_PATH,
                COLUMN_MESSAGE_UUID, COLUMN_SEX, COLUMN_MESSAGE_SEND_STATE, COLUMN_CONSULT_ID,
                COLUMN_CONSULT_STATUS, COLUMN_CONSULT_TITLE, COLUMN_CONNECTION_TYPE,
                COLUMN_IS_READ, COLUMN_PROVIDER_ID, COLUMN_PROVIDER_IDS));
    }

    @Override
    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COLUMN_DISPLAY_MESSAGE + " INTEGER DEFAULT 0");
        }
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        }
    }

    @Override
    public void cleanTable(SQLiteOpenHelper sqlHelper) {
        sqlHelper.getWritableDatabase().execSQL("delete from " + TABLE_MESSAGES);
    }

    public List<ChatItem> getChatItems(SQLiteOpenHelper sqlHelper, int offset, int limit) {
        List<ChatItem> items = new ArrayList<>();
        String query = String.format(Locale.US, "select * from (select * from %s order by %s desc limit %s offset %s) order by %s asc",
                TABLE_MESSAGES, COLUMN_TIMESTAMP, limit, offset, COLUMN_TIMESTAMP);
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(query, null)) {
            if (c.getCount() == 0) {
                return items;
            }
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ChatItem chatItem = getChatItem(sqlHelper, c);
                if (chatItem != null) {
                    items.add(chatItem);
                }
            }
        }
        return items;
    }

    @Nullable
    public ChatItem getChatItem(SQLiteOpenHelper sqlHelper, String messageUuid) {
        String sql = "select * from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_UUID + " = ?"
                + " order by " + COLUMN_TIMESTAMP + " desc";
        String[] selectionArgs = new String[]{messageUuid};
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, selectionArgs)) {
            if (c.moveToFirst()) {
                return getChatItem(sqlHelper, c);
            }
        }
        return null;
    }

    public void putChatItems(SQLiteOpenHelper sqlHelper, List<ChatItem> chatItems) {
        try {
            sqlHelper.getWritableDatabase().beginTransaction();
            for (ChatItem item : chatItems) {
                putChatItem(sqlHelper, item);
            }
            sqlHelper.getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            ThreadsLogger.e(TAG, "putMessagesSync", e);
        } finally {
            sqlHelper.getWritableDatabase().endTransaction();
        }
    }

    public boolean putChatItem(SQLiteOpenHelper sqlHelper, ChatItem chatItem) {
        if (chatItem instanceof ConsultConnectionMessage) {
            insertOrUpdateMessage(sqlHelper, getConsultConnectionMessageCV((ConsultConnectionMessage) chatItem));
            return true;
        }
        if (chatItem instanceof SimpleSystemMessage) {
            insertOrUpdateMessage(sqlHelper, getSimpleSystemMessageCV((SimpleSystemMessage) chatItem));
            return true;
        }
        if (chatItem instanceof ConsultPhrase) {
            final ConsultPhrase phrase = (ConsultPhrase) chatItem;
            insertOrUpdateMessage(sqlHelper, getConsultPhraseCV(phrase));
            if (phrase.getFileDescription() != null) {
                fileDescriptionTable.putFileDescription(sqlHelper, phrase.getFileDescription(), phrase.getUuid(), false);
            }
            if (phrase.getQuote() != null) {
                quotesTable.putQuote(sqlHelper, phrase.getUuid(), phrase.getQuote());
            }
            if (phrase.getQuickReplies() != null) {
                quickRepliesTable.putQuickReplies(sqlHelper, phrase.getId(), phrase.getQuickReplies());
            }
            return true;
        }
        if (chatItem instanceof UserPhrase) {
            final UserPhrase phrase = (UserPhrase) chatItem;
            insertOrUpdateMessage(sqlHelper, getUserPhraseCV(phrase));
            if (phrase.getFileDescription() != null) {
                fileDescriptionTable.putFileDescription(sqlHelper, phrase.getFileDescription(), phrase.getUuid(), false);
            }
            if (phrase.getQuote() != null) {
                quotesTable.putQuote(sqlHelper, phrase.getUuid(), phrase.getQuote());
            }
            return true;
        }
        if (chatItem instanceof Survey) {
            insertOrUpdateSurvey(sqlHelper, (Survey) chatItem);
        }
        return false;
    }

    @Nullable
    public ConsultInfo getLastConsultInfo(SQLiteOpenHelper sqlHelper, @NonNull String id) {
        String sql = "select " + COLUMN_AVATAR_PATH + ", " + COLUMN_NAME + ", " + COLUMN_CONSULT_STATUS
                + " from " + TABLE_MESSAGES
                + " where " + COLUMN_CONSULT_ID + " =  ? "
                + " order by " + COLUMN_TIMESTAMP + " desc";
        String[] selectionArgs = new String[]{id};
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, selectionArgs)) {
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

    public List<UserPhrase> getUnsendUserPhrase(SQLiteOpenHelper sqlHelper, int count) {
        List<UserPhrase> userPhrases = new ArrayList<>();
        List<ChatItem> chatItems = getChatItems(sqlHelper, 0, count);
        for (ChatItem chatItem : chatItems) {
            if (chatItem instanceof UserPhrase) {
                if (((UserPhrase) chatItem).getSentState() == MessageState.STATE_NOT_SENT) {
                    userPhrases.add((UserPhrase) chatItem);
                }
            }
        }
        return userPhrases;
    }

    public void setUserPhraseStateByProviderId(SQLiteOpenHelper sqlHelper, String providerId, MessageState messageState) {
        final ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_SEND_STATE, messageState.ordinal());
        sqlHelper.getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_PROVIDER_ID + " = ?", new String[]{providerId});
    }

    @Nullable
    public ConsultPhrase getLastConsultPhrase(SQLiteOpenHelper sqlHelper) {
        String sql = "select * from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal()
                + " order by " + COLUMN_TIMESTAMP + " desc";
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, new String[]{})) {
            if (c.moveToFirst()) {
                return getConsultPhrase(sqlHelper, c);
            }
        }
        return null;
    }

    public int setAllConsultMessagesWereRead(SQLiteOpenHelper sqlHelper) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        String whereClause = COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal() +
                " and " + COLUMN_IS_READ + " = 0";
        return sqlHelper.getWritableDatabase().update(TABLE_MESSAGES, cv, whereClause, null);
    }

    public void setConsultMessageWasRead(SQLiteOpenHelper sqlHelper, String providerId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        String whereClause = COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal() +
                " and " + COLUMN_PROVIDER_ID + " = ? " +
                " and " + COLUMN_IS_READ + " = 0";
        sqlHelper.getWritableDatabase().update(TABLE_MESSAGES, cv, whereClause, new String[]{providerId});
    }

    @Nullable
    public Survey getSurvey(SQLiteOpenHelper sqlHelper, long sendingId) {
        String sql = "select * from " + TABLE_MESSAGES
                + " where " + COLUMN_SURVEY_SENDING_ID + " = ?"
                + " order by " + COLUMN_TIMESTAMP + " desc";
        String[] selectionArgs = new String[]{String.valueOf(sendingId)};
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, selectionArgs)) {
            if (c.moveToFirst()) {
                return getSurvey(sqlHelper, c);
            }
        }
        return null;
    }

    public int getMessagesCount(SQLiteOpenHelper sqlHelper) {
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(String.format(Locale.US, "select count(%s) from %s", COLUMN_TABLE_ID, TABLE_MESSAGES), null)) {
            if (c.getCount() == 0) {
                return 0;
            }
            c.moveToFirst();
            return c.getInt(0);
        }
    }

    public int getUnreadMessagesCount(SQLiteOpenHelper sqlHelper) {
        String sql = "select " + COLUMN_PROVIDER_ID + " , " + COLUMN_PROVIDER_IDS +
                " from " + TABLE_MESSAGES +
                " where " + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal() + " and " + COLUMN_IS_READ + " = 0" +
                " order by " + COLUMN_TIMESTAMP + " asc";
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, null)) {
            return c.getCount();
        }
    }

    public List<String> getUnreadMessagesUuid(SQLiteOpenHelper sqlHelper) {
        String sql = "select " + COLUMN_MESSAGE_UUID +
                " from " + TABLE_MESSAGES +
                " where " + COLUMN_MESSAGE_TYPE + " = " + MessageType.CONSULT_PHRASE.ordinal() + " and " + COLUMN_IS_READ + " = 0" +
                " order by " + COLUMN_TIMESTAMP + " asc";
        Set<String> ids = new HashSet<>();
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, null)) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ids.add(cGetString(c, COLUMN_MESSAGE_UUID));
            }
        }
        return new ArrayList<>(ids);
    }

    @Nullable
    private ChatItem getChatItem(SQLiteOpenHelper sqlHelper, Cursor c) {
        int type = cGetInt(c, COLUMN_MESSAGE_TYPE);
        if (type == MessageType.CONSULT_CONNECTED.ordinal()) {
            return new ConsultConnectionMessage(
                    cGetString(c, COLUMN_MESSAGE_UUID),
                    cGetString(c, COLUMN_PROVIDER_ID),
                    stringToList(cGetString(c, COLUMN_PROVIDER_IDS)),
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
                    cGetString(c, COLUMN_PHRASE)
            );
        } else if (type == MessageType.SYSTEM_MESSAGE.ordinal()) {
            return new SimpleSystemMessage(
                    cGetString(c, COLUMN_MESSAGE_UUID),
                    cGetString(c, COLUMN_MESSAGE_TYPE),
                    cGetLong(c, COLUMN_TIMESTAMP),
                    cGetString(c, COLUMN_PHRASE)
            );
        } else if (type == MessageType.CONSULT_PHRASE.ordinal()) {
            return getConsultPhrase(sqlHelper, c);
        } else if (type == MessageType.USER_PHRASE.ordinal()) {
            return getUserPhrase(sqlHelper, c);
        } else if (type == MessageType.SURVEY.ordinal()) {
            return getSurvey(sqlHelper, c);
        }
        return null;
    }

    private ConsultPhrase getConsultPhrase(SQLiteOpenHelper sqlHelper, Cursor c) {
        return new ConsultPhrase(
                cGetString(c, COLUMN_MESSAGE_UUID),
                cGetString(c, COLUMN_PROVIDER_ID),
                stringToList(cGetString(c, COLUMN_PROVIDER_IDS)),
                fileDescriptionTable.getFileDescription(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
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
                quickRepliesTable.getQuickReplies(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID))
        );
    }

    private UserPhrase getUserPhrase(SQLiteOpenHelper sqlHelper, Cursor c) {
        return new UserPhrase(
                cGetString(c, COLUMN_MESSAGE_UUID),
                cGetString(c, COLUMN_PROVIDER_ID),
                stringToList(cGetString(c, COLUMN_PROVIDER_IDS)),
                cGetString(c, COLUMN_PHRASE),
                quotesTable.getQuote(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
                cGetLong(c, COLUMN_TIMESTAMP),
                fileDescriptionTable.getFileDescription(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
                MessageState.fromOrdinal(cGetInt(c, COLUMN_MESSAGE_SEND_STATE))
        );
    }

    private Survey getSurvey(SQLiteOpenHelper sqlHelper, Cursor c) {
        long surveySendingId = cGetLong(c, COLUMN_SURVEY_SENDING_ID);
        Survey survey = new Survey(
                surveySendingId,
                cGetLong(c, COLUMN_SURVEY_HIDE_AFTER),
                cGetLong(c, COLUMN_TIMESTAMP),
                MessageState.fromOrdinal(cGetInt(c, COLUMN_MESSAGE_SEND_STATE))
        );
        if (survey.getHideAfter() * 1000 + survey.getTimeStamp() <= System.currentTimeMillis()) {
            return null;
        }
        survey.setQuestions(Collections.singletonList(questionsTable.getQuestion(sqlHelper, surveySendingId)));
        return survey;
    }

    private ContentValues getConsultPhraseCV(ConsultPhrase phrase) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_UUID, phrase.getUuid());
        cv.put(COLUMN_PHRASE, phrase.getPhrase());
        cv.put(COLUMN_FORMATTED_PHRASE, phrase.getFormattedPhrase());
        cv.put(COLUMN_TIMESTAMP, phrase.getTimeStamp());
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.CONSULT_PHRASE.ordinal());
        cv.put(COLUMN_AVATAR_PATH, phrase.getAvatarPath());
        cv.put(COLUMN_CONSULT_ID, phrase.getConsultId());
        cv.put(COLUMN_IS_READ, phrase.isRead());
        cv.put(COLUMN_CONSULT_STATUS, phrase.getStatus());
        cv.put(COLUMN_NAME, phrase.getConsultName());
        cv.put(COLUMN_PROVIDER_ID, phrase.getProviderId());
        cv.put(COLUMN_PROVIDER_IDS, listToString(phrase.getProviderIds()));
        cv.put(COLUMN_SEX, phrase.getSex());
        return cv;
    }

    private ContentValues getUserPhraseCV(UserPhrase phrase) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_UUID, phrase.getUuid());
        cv.put(COLUMN_PROVIDER_ID, phrase.getProviderId());
        cv.put(COLUMN_PROVIDER_IDS, listToString(phrase.getProviderIds()));
        cv.put(COLUMN_PHRASE, phrase.getPhrase());
        cv.put(COLUMN_TIMESTAMP, phrase.getTimeStamp());
        cv.put(COLUMN_MESSAGE_TYPE, MessageType.USER_PHRASE.ordinal());
        cv.put(COLUMN_MESSAGE_SEND_STATE, phrase.getSentState().ordinal());
        return cv;
    }

    private ContentValues getConsultConnectionMessageCV(ConsultConnectionMessage consultConnectionMessage) {
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
        cv.put(COLUMN_DISPLAY_MESSAGE, consultConnectionMessage.isDisplayMessage());
        cv.put(COLUMN_PHRASE, consultConnectionMessage.getText());
        return cv;
    }

    private ContentValues getSimpleSystemMessageCV(SimpleSystemMessage simpleSystemMessage) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_UUID, simpleSystemMessage.getUuid());
        cv.put(COLUMN_MESSAGE_TYPE, simpleSystemMessage.getType());
        cv.put(COLUMN_TIMESTAMP, simpleSystemMessage.getTimeStamp());
        cv.put(COLUMN_PHRASE, simpleSystemMessage.getText());
        return cv;
    }

    private void insertOrUpdateMessage(SQLiteOpenHelper sqlHelper, ContentValues cv) {
        String sql = "select " + COLUMN_MESSAGE_UUID +
                " from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_UUID + " = ?";
        String[] selectionArgs = new String[]{cv.getAsString(COLUMN_MESSAGE_UUID)};
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, selectionArgs)) {
            if (c.getCount() > 0) {
                sqlHelper.getWritableDatabase().update(
                        TABLE_MESSAGES,
                        cv,
                        COLUMN_MESSAGE_UUID + " = ? ",
                        new String[]{cv.getAsString(COLUMN_MESSAGE_UUID)}
                );
            } else {
                sqlHelper.getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
            }
        }
    }

    private void insertOrUpdateSurvey(SQLiteOpenHelper sqlHelper, Survey survey) {
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
        try (Cursor c = sqlHelper.getWritableDatabase().rawQuery(sql, selectionArgs)) {
            if (c.getCount() > 0) {
                sqlHelper.getWritableDatabase().update(
                        TABLE_MESSAGES,
                        cv,
                        COLUMN_SURVEY_SENDING_ID + " = ? ",
                        new String[]{String.valueOf(survey.getSendingId())}
                );
            } else {
                sqlHelper.getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
            }
        }
        for (QuestionDTO question : survey.getQuestions()) {
            questionsTable.putQuestion(sqlHelper, question, survey.getSendingId());
        }
    }

    private List<String> stringToList(String text) {
        if (text == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(text.split(";"));
    }

    private String listToString(List<String> list) {
        if (list == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String divider = "";
        for (String item : list) {
            stringBuilder.append(divider).append(item);
            divider = ";";
        }
        return stringBuilder.toString();
    }

    private enum MessageType {
        UNKNOWN,
        CONSULT_CONNECTED,
        SYSTEM_MESSAGE,
        CONSULT_PHRASE,
        USER_PHRASE,
        SURVEY
    }
}
