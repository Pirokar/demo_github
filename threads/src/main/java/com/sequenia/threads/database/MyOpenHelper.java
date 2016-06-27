package com.sequenia.threads.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.ConsultConnected;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.model.UserPhrase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yuri on 23.06.2016.
 */
class MyOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "MyOpenHelper ";
    private static final int VERSION = 1;
    static final String TABLE_MESSAGES = "TABLE_MESSAGES";
    static final String COLUMN_TABLE_ID = "TABLE_ID";
    static final String COLUMN_TIMESTAMP = "COLUMN_TIMESTAMP";
    static final String COLUMN_PHRASE = "COLUMN_PHRASE";
    static final String COLUMN_MESSAGE_TYPE = "COLUMN_MESSAGE_TYPE";
    static final String COLUMN_NAME = "COLUMN_NAME";
    static final String COLUMN_AVATAR_PATH = "COLUMN_AVATAR_PATH";
    static final String COLUMN_FILE_PATH = "COLUMN_FILE_PATH";
    static final String COLUMN_MESSAGE_ID = "COLUMN_MESSAGE_ID";
    static final String COLUMN_MESSAGE_SEND_STATE = "COLUMN_MESSAGE_SEND_STATE";
    static final String COLUMN_SEX = "COLUMN_SEX";


    static final String TABLE_QUOTE = "TABLE_QUOTE";
    static final String COLUMN_QUOTE_HEADER = "COLUMN_QUOTE_HEADER";
    static final String COLUMN_QUOTE_BODY = "COLUMN_QUOTE_BODY";
    static final String COLUMN_QUOTE_TIMESTAMP = "COLUMN_QUOTE_TIMESTAMP";
    static final String COLUMN_QUOTE_MESSAGE_ID_EXT = "COLUMN_QUOTE_MESSAGE_ID_EXT";

    static final String TABLE_FILE_DESCRIPTION = "TABLE_FILE_DESCRIPTION";
    static final String COLUMN_FD_HEADER = "COLUMN_FD_HEADER";
    static final String COLUMN_FD_BODY = "COLUMN_FD_BODY";
    static final String COLUMN_FD_TIMESTAMP = "COLUMN_FD_TIMESTAMP";
    static final String COLUMN_FD_MESSAGE_ID_EXT = "COLUMN_FD_MESSAGE_ID_EXT";

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
                        " %s text, " +//file path
                        " %s text, " + // message id
                        "%s integer, " + //sex
                        " %s integer)",//message sent state
                TABLE_MESSAGES, COLUMN_TABLE_ID, COLUMN_TIMESTAMP
                , COLUMN_PHRASE, COLUMN_MESSAGE_TYPE, COLUMN_NAME, COLUMN_AVATAR_PATH,
                COLUMN_FILE_PATH, COLUMN_MESSAGE_ID, COLUMN_SEX, COLUMN_MESSAGE_SEND_STATE));
        db.execSQL(String.format(Locale.US, "create table %s ( " + // TABLE_QUOTE
                        " %s text, " +//header
                        " %s text, " +//body
                        " %s integer, " +//timestamp
                        " %s integer)" // message id
                , TABLE_QUOTE, COLUMN_QUOTE_HEADER, COLUMN_QUOTE_BODY, COLUMN_QUOTE_TIMESTAMP, COLUMN_QUOTE_MESSAGE_ID_EXT));
        db.execSQL(String.format(Locale.US, "create table %s ( " + // TABLE_FILE_DESCRIPTION
                        " %s text, " +//header
                        " %s text, " +//body
                        " %s integer, " +//timestamp
                        " %s integer)" // message id
                , TABLE_FILE_DESCRIPTION, COLUMN_FD_HEADER, COLUMN_FD_BODY, COLUMN_FD_TIMESTAMP, COLUMN_FD_MESSAGE_ID_EXT));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    void putUserPhrase(ChatPhrase phrase) {
        ContentValues cv = new ContentValues();
        boolean isDup = false;
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_MESSAGE_ID + " from " + TABLE_MESSAGES + " where " + COLUMN_MESSAGE_ID + " = ?", new String[]{phrase.getId()});
        if (c.getCount() > 0) isDup = true;
        if (phrase instanceof UserPhrase) {
            UserPhrase userPhrase = (UserPhrase) phrase;
            cv.put(COLUMN_MESSAGE_ID, userPhrase.getMessageId());
            cv.put(COLUMN_PHRASE, userPhrase.getPhrase());
            cv.put(COLUMN_MESSAGE_SEND_STATE, userPhrase.getSentState().getType());
            cv.put(COLUMN_TIMESTAMP, userPhrase.getTimeStamp());
            cv.put(COLUMN_FILE_PATH, userPhrase.getFilePath());
            cv.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_USER_PHRASE.type);
            if (!isDup) {
                getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
            } else {
                getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_ID + " = ?", new String[]{phrase.getId()});
            }
            if (userPhrase.getFileDescription() != null) {
                cv.clear();
                cv.put(COLUMN_FD_MESSAGE_ID_EXT, userPhrase.getMessageId());
                cv.put(COLUMN_FD_HEADER, userPhrase.getFileDescription().getHeader());
                cv.put(COLUMN_FD_BODY, userPhrase.getFileDescription().getText());
                cv.put(COLUMN_FD_TIMESTAMP, userPhrase.getFileDescription().getTimeStamp());
                getWritableDatabase().insert(TABLE_FILE_DESCRIPTION, null, cv);
            }
            if (userPhrase.getQuote() != null) {
                cv.clear();
                cv.put(COLUMN_QUOTE_MESSAGE_ID_EXT, userPhrase.getMessageId());
                cv.put(COLUMN_QUOTE_HEADER, userPhrase.getQuote().getHeader());
                cv.put(COLUMN_QUOTE_BODY, userPhrase.getQuote().getText());
                cv.put(COLUMN_QUOTE_TIMESTAMP, userPhrase.getQuote().getTimeStamp());
                getWritableDatabase().insert(TABLE_QUOTE, null, cv);
            }
        } else if (phrase instanceof ConsultPhrase) {
            ConsultPhrase consultPhrase = (ConsultPhrase) phrase;
            cv.put(COLUMN_MESSAGE_ID, consultPhrase.getMessageId());
            cv.put(COLUMN_PHRASE, consultPhrase.getPhrase());
            cv.put(COLUMN_TIMESTAMP, consultPhrase.getTimeStamp());
            cv.put(COLUMN_FILE_PATH, consultPhrase.getFilePath());
            cv.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_CONSULT_PHRASE.type);
            if (!isDup) {
                getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
            } else {
                getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_ID + " = ? ", new String[]{consultPhrase.getId()});
            }
            if (consultPhrase.getFileDescription() != null) {
                cv.clear();
                cv.put(COLUMN_FD_MESSAGE_ID_EXT, consultPhrase.getMessageId());
                cv.put(COLUMN_FD_HEADER, consultPhrase.getFileDescription().getHeader());
                cv.put(COLUMN_FD_BODY, consultPhrase.getFileDescription().getText());
                cv.put(COLUMN_FD_TIMESTAMP, consultPhrase.getFileDescription().getTimeStamp());
                getWritableDatabase().insert(TABLE_FILE_DESCRIPTION, null, cv);
            }
            if (consultPhrase.getQuote() != null) {
                cv.clear();
                cv.put(COLUMN_QUOTE_MESSAGE_ID_EXT, consultPhrase.getMessageId());
                cv.put(COLUMN_QUOTE_HEADER, consultPhrase.getQuote().getHeader());
                cv.put(COLUMN_QUOTE_BODY, consultPhrase.getQuote().getText());
                cv.put(COLUMN_QUOTE_TIMESTAMP, consultPhrase.getQuote().getTimeStamp());
                getWritableDatabase().insert(TABLE_QUOTE, null, cv);
            }
        }
        c.close();
    }

    void setUserPhraseState(String messageId, MessageState messageState) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_SEND_STATE, messageState.getType());
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_ID + " = ?", new String[]{messageId});
    }

    void putConsultConnected(ConsultConnected consultConnected) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, consultConnected.getName());
        cv.put(COLUMN_TIMESTAMP, consultConnected.getTimeStamp());
        cv.put(COLUMN_AVATAR_PATH, consultConnected.getAvatarPath());
        cv.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_CONSULT_CONNECTED.type);
        cv.put(COLUMN_SEX, consultConnected.getSex() ? "1" : "0");
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_NAME + " , " + COLUMN_TIMESTAMP + " from " + TABLE_MESSAGES + " where " + COLUMN_NAME + " = ? and " + COLUMN_TIMESTAMP + " = ? ", new String[]{consultConnected.getName(), "" + consultConnected.getTimeStamp()});
        if (c.getCount() == 0) {
            c.close();
            getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
        }
    }

    public List<ChatItem> getChatItems(int offset, int limit) {
        List<ChatItem> items = new ArrayList<>();
        String query = String.format(Locale.US, "select * from %s order by %s asc limit %s offset %s", TABLE_MESSAGES, COLUMN_TIMESTAMP, String.valueOf(limit), String.valueOf(offset));
        Cursor c = getWritableDatabase().rawQuery(query, null);
        if (c.getCount() == 0) {
            c.close();
            return items;
        }
        final int INDEX_NAME = c.getColumnIndex(COLUMN_NAME);
        final int INDEX_AVATAR_PATH = c.getColumnIndex(COLUMN_AVATAR_PATH);
        final int INDEX_TIMESTAMP = c.getColumnIndex(COLUMN_TIMESTAMP);
        final int INDEX_FILEPATH = c.getColumnIndex(COLUMN_FILE_PATH);
        final int INDEX_PHRASE = c.getColumnIndex(COLUMN_PHRASE);
        final int INDEX_MESSAGE_ID = c.getColumnIndex(COLUMN_MESSAGE_ID);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            int type = c.getInt(c.getColumnIndex(COLUMN_MESSAGE_TYPE));
            if (type == MessageTypes.TYPE_CONSULT_CONNECTED.type) {
                boolean sex = c.getInt(c.getColumnIndex(COLUMN_SEX)) == 1;
                String name = c.isNull(INDEX_NAME) ? null : c.getString(INDEX_NAME);
                String avatarPath = c.isNull(INDEX_AVATAR_PATH) ? null : c.getString(INDEX_AVATAR_PATH);
                ConsultConnected cc = new ConsultConnected(name, sex, c.getLong(INDEX_TIMESTAMP), avatarPath);
                items.add(cc);
            } else if (type == MessageTypes.TYPE_CONSULT_PHRASE.type) {
                String filePath = c.isNull(INDEX_FILEPATH) ? null : c.getString(INDEX_FILEPATH);
                String avatarPath = c.isNull(INDEX_AVATAR_PATH) ? null : c.getString(INDEX_AVATAR_PATH);
                String phrase = c.isNull(INDEX_PHRASE) ? null : c.getString(INDEX_PHRASE);
                String name = c.isNull(INDEX_NAME) ? null : c.getString(INDEX_NAME);

                ConsultPhrase cp = new ConsultPhrase(avatarPath,
                        filePath,
                        c.getLong(INDEX_TIMESTAMP),
                        phrase,
                        c.getString(INDEX_MESSAGE_ID),
                        name,
                        getQuote(c.getLong(INDEX_MESSAGE_ID)),
                        getFileDescription(c.getLong(INDEX_MESSAGE_ID)));
                items.add(cp);
            } else if (type == MessageTypes.TYPE_USER_PHRASE.type) {
                String filePath = c.isNull(INDEX_FILEPATH) ? null : c.getString(INDEX_FILEPATH);
                String phrase = c.isNull(INDEX_PHRASE) ? null : c.getString(INDEX_PHRASE);
                UserPhrase up = new UserPhrase(
                        c.getString(INDEX_MESSAGE_ID),
                        phrase,
                        getQuote(c.getLong(INDEX_MESSAGE_ID)),
                        c.getLong(INDEX_TIMESTAMP),
                        getFileDescription(c.getLong(INDEX_MESSAGE_ID)),
                        filePath);
                int sentState = c.getInt(c.getColumnIndex(COLUMN_MESSAGE_SEND_STATE));
                MessageState ms = sentState == 1 ? MessageState.STATE_SENT : sentState == 2 ? MessageState.STATE_SENT_AND_SERVER_RECEIVED : MessageState.STATE_NOT_SENT;
                up.setSentState(ms);
                items.add(up);
            }
        }
        c.close();
        return items;
    }

    private Quote getQuote(long messageId) {
        Cursor c = getWritableDatabase().rawQuery("select * from " + TABLE_QUOTE + " where " + COLUMN_QUOTE_MESSAGE_ID_EXT + " = " + messageId, null);
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        String header = c.isNull(c.getColumnIndex(COLUMN_QUOTE_HEADER)) ? null : c.getString(c.getColumnIndex(COLUMN_QUOTE_HEADER));
        String body = c.isNull(c.getColumnIndex(COLUMN_QUOTE_BODY)) ? null : c.getString(c.getColumnIndex(COLUMN_QUOTE_BODY));
        Quote q = new Quote(header, body, c.getLong(c.getColumnIndex(COLUMN_QUOTE_TIMESTAMP)));
        c.close();
        return q;
    }

    private FileDescription getFileDescription(long messageId) {
        String query = String.format(Locale.US, "select * from %s where %s = %s", TABLE_FILE_DESCRIPTION, COLUMN_FD_MESSAGE_ID_EXT, String.valueOf(messageId));
        Cursor c = getWritableDatabase().rawQuery(query, null);
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        String header = c.isNull(c.getColumnIndex(COLUMN_FD_HEADER)) ? null : c.getString(c.getColumnIndex(COLUMN_FD_HEADER));
        String body = c.isNull(c.getColumnIndex(COLUMN_FD_BODY)) ? null : c.getString(c.getColumnIndex(COLUMN_FD_BODY));
        FileDescription fd = new FileDescription(header, body, c.getLong(c.getColumnIndex(COLUMN_FD_TIMESTAMP)));
        c.close();
        return fd;
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
        TYPE_CONSULT_CONNECTED(1),
        TYPE_CONSULT_PHRASE(2),
        TYPE_USER_PHRASE(3);
        int type;
        MessageTypes(int type) {
            this.type = type;
        }
    }
}
