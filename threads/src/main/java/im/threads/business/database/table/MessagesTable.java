package im.threads.business.database.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import im.threads.business.formatters.SpeechStatus;
import im.threads.business.models.ChatItem;
import im.threads.business.models.ConsultConnectionMessage;
import im.threads.business.models.ConsultPhrase;
import im.threads.business.models.MessageStatus;
import im.threads.business.models.RequestResolveThread;
import im.threads.business.models.SimpleSystemMessage;
import im.threads.business.models.Survey;
import im.threads.business.models.UserPhrase;

public class MessagesTable extends Table {
    private static final String TABLE_MESSAGES = "TABLE_MESSAGES";
    private static final String COLUMN_TABLE_ID = "TABLE_ID";
    private static final String COLUMN_MESSAGE_UUID = "COLUMN_MESSAGE_UUID";
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
    private static final String COLUMN_THREAD_ID = "COLUMN_THREAD_ID";
    private static final String COLUMN_BLOCK_INPUT = "COLUMN_BLOCK_INPUT";
    private static final String COLUMN_SPEECH_STATUS = "COLUMN_SPEECH_STATUS";

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
                        "%s integer " //isRead
                        + ", " + COLUMN_DISPLAY_MESSAGE + " integer"
                        + ", " + COLUMN_SURVEY_SENDING_ID + " integer"
                        + ", " + COLUMN_SURVEY_HIDE_AFTER + " integer"
                        + ", " + COLUMN_THREAD_ID + " integer"
                        + ", " + COLUMN_BLOCK_INPUT + " integer"
                        + ", " + COLUMN_SPEECH_STATUS + " text"
                        + ")",
                TABLE_MESSAGES, COLUMN_TABLE_ID, COLUMN_TIMESTAMP
                , COLUMN_PHRASE, COLUMN_FORMATTED_PHRASE, COLUMN_MESSAGE_TYPE, COLUMN_NAME, COLUMN_AVATAR_PATH,
                COLUMN_MESSAGE_UUID, COLUMN_SEX, COLUMN_MESSAGE_SEND_STATE, COLUMN_CONSULT_ID,
                COLUMN_CONSULT_STATUS, COLUMN_CONSULT_TITLE, COLUMN_CONNECTION_TYPE,
                COLUMN_IS_READ));
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

    @NonNull
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
    private ChatItem getChatItem(SQLiteOpenHelper sqlHelper, Cursor c) {
        int type = cGetInt(c, COLUMN_MESSAGE_TYPE);
        if (type == MessageType.CONSULT_CONNECTED.ordinal()) {
            return new ConsultConnectionMessage(
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
            );
        } else if (type == MessageType.SYSTEM_MESSAGE.ordinal()) {
            return new SimpleSystemMessage(
                    cGetString(c, COLUMN_MESSAGE_UUID),
                    cGetString(c, COLUMN_CONNECTION_TYPE),
                    cGetLong(c, COLUMN_TIMESTAMP),
                    cGetString(c, COLUMN_PHRASE),
                    cGetLong(c, COLUMN_THREAD_ID)
            );
        } else if (type == MessageType.CONSULT_PHRASE.ordinal()) {
            return getConsultPhrase(sqlHelper, c);
        } else if (type == MessageType.USER_PHRASE.ordinal()) {
            return getUserPhrase(sqlHelper, c);
        } else if (type == MessageType.SURVEY.ordinal()) {
            return getSurvey(sqlHelper, c);
        } else if (type == MessageType.REQUEST_RESOLVE_THREAD.ordinal()) {
            return getRequestResolveThread(c);
        }
        return null;
    }

    private ConsultPhrase getConsultPhrase(SQLiteOpenHelper sqlHelper, Cursor c) {
        return new ConsultPhrase(
                cGetString(c, COLUMN_MESSAGE_UUID),
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
                cGetLong(c, COLUMN_THREAD_ID),
                quickRepliesTable.getQuickReplies(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
                cGetBool(c, COLUMN_BLOCK_INPUT),
                SpeechStatus.Companion.fromString(cGetString(c, COLUMN_SPEECH_STATUS))
        );
    }

    private UserPhrase getUserPhrase(SQLiteOpenHelper sqlHelper, Cursor c) {
        return new UserPhrase(
                cGetString(c, COLUMN_MESSAGE_UUID),
                cGetString(c, COLUMN_PHRASE),
                quotesTable.getQuote(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
                cGetLong(c, COLUMN_TIMESTAMP),
                fileDescriptionTable.getFileDescription(sqlHelper, cGetString(c, COLUMN_MESSAGE_UUID)),
                MessageStatus.fromOrdinal(cGetInt(c, COLUMN_MESSAGE_SEND_STATE)),
                cGetLong(c, COLUMN_THREAD_ID)
        );
    }

    private Survey getSurvey(SQLiteOpenHelper sqlHelper, Cursor c) {
        long surveySendingId = cGetLong(c, COLUMN_SURVEY_SENDING_ID);
        Survey survey = new Survey(
                cGetString(c, COLUMN_MESSAGE_UUID),
                surveySendingId,
                cGetLong(c, COLUMN_SURVEY_HIDE_AFTER),
                cGetLong(c, COLUMN_TIMESTAMP),
                MessageStatus.fromOrdinal(cGetInt(c, COLUMN_MESSAGE_SEND_STATE)),
                cGetBool(c, COLUMN_IS_READ),
                cGetBool(c, COLUMN_DISPLAY_MESSAGE)
        );
        survey.setQuestions(Collections.singletonList(questionsTable.getQuestion(sqlHelper, surveySendingId)));
        return survey;
    }

    private RequestResolveThread getRequestResolveThread(Cursor c) {
        RequestResolveThread requestResolveThread = new RequestResolveThread(
                cGetString(c, COLUMN_MESSAGE_UUID),
                cGetLong(c, COLUMN_SURVEY_HIDE_AFTER),
                cGetLong(c, COLUMN_TIMESTAMP),
                cGetLong(c, COLUMN_THREAD_ID),
                cGetBool(c, COLUMN_IS_READ)
        );
        if (!cGetBool(c, COLUMN_DISPLAY_MESSAGE)) {
            return null;
        }
        return requestResolveThread;
    }

    private enum MessageType {
        UNKNOWN,
        CONSULT_CONNECTED,
        SYSTEM_MESSAGE,
        CONSULT_PHRASE,
        USER_PHRASE,
        SURVEY,
        REQUEST_RESOLVE_THREAD
    }
}
