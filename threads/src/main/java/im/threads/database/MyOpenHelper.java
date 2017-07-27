package im.threads.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.ArrayList;
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
import im.threads.model.Quote;
import im.threads.model.UserPhrase;
import im.threads.utils.FileUtils;

/**
 * Created by yuri on 23.06.2016.
 * обертка для БД
 */
class MyOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "MyOpenHelper ";
    private static final int VERSION = 1;
    private static final String TABLE_MESSAGES = "TABLE_MESSAGES";
    private static final String COLUMN_TABLE_ID = "TABLE_ID";
    private static final String COLUMN_TIMESTAMP = "COLUMN_TIMESTAMP";
    private static final String COLUMN_PHRASE = "COLUMN_PHRASE";
    private static final String COLUMN_MESSAGE_TYPE = "COLUMN_MESSAGE_TYPE";
    private static final String COLUMN_NAME = "COLUMN_NAME";
    private static final String COLUMN_AVATAR_PATH = "COLUMN_AVATAR_PATH";
    private static final String COLUMN_MESSAGE_ID = "COLUMN_MESSAGE_ID";
    private static final String COLUMN_MESSAGE_SEND_STATE = "COLUMN_MESSAGE_SEND_STATE";
    private static final String COLUMN_SEX = "COLUMN_SEX";
    private static final String COLUMN_CONSULT_ID = "COLUMN_CONSULT_ID";
    private static final String COLUMN_CONSULT_STATUS = "COLUMN_CONSULT_STATUS";
    private static final String COLUMN_CONSULT_TITLE = "COLUMN_CONSULT_TITLE";
    private static final String COLUMN_CONNECTION_TYPE = "COLUMN_CONNECTION_TYPE";
    private static final String COLUMN_IS_READ = "COLUMN_IS_READ";
    private static final String COLUMN_BACKEND_ID = "COLUMN_BACKEND_ID";


    private static final String TABLE_QUOTE = "TABLE_QUOTE";
    private static final String COLUMN_QUOTE_HEADER = "COLUMN_QUOTE_HEADER";
    private static final String COLUMN_QUOTE_BODY = "COLUMN_QUOTE_BODY";
    private static final String COLUMN_QUOTE_TIMESTAMP = "COLUMN_QUOTE_TIMESTAMP";
    private static final String COLUMN_QUOTE_MESSAGE_ID_EXT = "COLUMN_QUOTE_MESSAGE_ID_EXT";

    private static final String TABLE_FILE_DESCRIPTION = "TABLE_FILE_DESCRIPTION";
    private static final String COLUMN_FD_HEADER = "COLUMN_FD_HEADER";
    private static final String COLUMN_FD_PATH = "COLUMN_FD_PATH";
    private static final String COLUMN_FD_DOWNLOAD_PATH = "COLUMN_WEB_PATH";
    private static final String COLUMN_FD_DOWNLOAD_PROGRESS = "COLUMN_FD_DOWNLOAD_PROGRESS";
    private static final String COLUMN_FD_TIMESTAMP = "COLUMN_FD_TIMESTAMP";
    private static final String COLUMN_FD_SIZE = "COLUMN_FD_SIZE";
    private static final String COLUMN_FD_IS_FROM_QUOTE = "COLUMN_FD_IS_FROM_QUOTE";
    private static final String COLUMN_FD_INCOMING_FILENAME = "COLUMN_FD_INCOMING_FILENAME";
    private static final String COLUMN_FD_MESSAGE_ID_EXT = "COLUMN_FD_MESSAGE_ID_EXT";
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
                        "%s integer, " + //sex
                        " %s integer," +//message sent state
                        "%s text," + //consultid
                        "%s text," + //COLUMN_CONSULT_STATUS
                        "%s text," +//COLUMN_CONSULT_TITLE
                        "%s text," +//connection type
                        "%s integer," + //isRead
                        "%s text)", //COLUMN_BACKEND_ID
                TABLE_MESSAGES, COLUMN_TABLE_ID, COLUMN_TIMESTAMP
                , COLUMN_PHRASE, COLUMN_MESSAGE_TYPE, COLUMN_NAME, COLUMN_AVATAR_PATH,
                COLUMN_MESSAGE_ID, COLUMN_SEX, COLUMN_MESSAGE_SEND_STATE, COLUMN_CONSULT_ID,
                COLUMN_CONSULT_STATUS, COLUMN_CONSULT_TITLE, COLUMN_CONNECTION_TYPE,
                COLUMN_IS_READ, COLUMN_BACKEND_ID));
        db.execSQL(String.format(Locale.US, "create table %s ( " + // TABLE_QUOTE
                        " %s text, " +//header
                        " %s text, " +//body
                        " %s integer, " +//timestamp
                        " %s integer)" // message id
                , TABLE_QUOTE, COLUMN_QUOTE_HEADER, COLUMN_QUOTE_BODY, COLUMN_QUOTE_TIMESTAMP, COLUMN_QUOTE_MESSAGE_ID_EXT));
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
                , TABLE_FILE_DESCRIPTION, COLUMN_FD_HEADER, COLUMN_FD_PATH, COLUMN_FD_TIMESTAMP, COLUMN_FD_MESSAGE_ID_EXT, COLUMN_FD_DOWNLOAD_PATH, COLUMN_FD_SIZE, COLUMN_FD_IS_FROM_QUOTE, COLUMN_FD_INCOMING_FILENAME, COLUMN_FD_DOWNLOAD_PROGRESS));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void putUserPhrase(UserPhrase userPhrase) {
        ArrayList<UserPhrase> phrasesInDb = new ArrayList<>();
        if (cashedPhrases == null || cashedPhrases.isEmpty()
                || (System.currentTimeMillis() - lastPhraseRequest) > 300) {
            Cursor c = getWritableDatabase().rawQuery("select * from " + TABLE_MESSAGES, new String[]{});
            final int INDEX_TIMESTAMP = c.getColumnIndex(COLUMN_TIMESTAMP);
            final int INDEX_PHRASE = c.getColumnIndex(COLUMN_PHRASE);
            final int INDEX_MESSAGE_ID = c.getColumnIndex(COLUMN_MESSAGE_ID);
            final int INDEX_BACKEND_ID = c.getColumnIndex(COLUMN_BACKEND_ID);
            final int INDEX_TYPE = c.getColumnIndex(COLUMN_MESSAGE_TYPE);
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                if (c.getInt(INDEX_TYPE) == MessageTypes.TYPE_USER_PHRASE.type) {
                    String phrase1 = c.isNull(INDEX_PHRASE) ? null : c.getString(INDEX_PHRASE);
                    Pair<Boolean, FileDescription> fd = getFd(c.getString(INDEX_MESSAGE_ID));
                    UserPhrase up = new UserPhrase(
                            c.getString(INDEX_MESSAGE_ID),
                            phrase1,
                            getQuote(c.getString(INDEX_MESSAGE_ID)),
                            c.getLong(INDEX_TIMESTAMP),
                            fd != null && !fd.first ? fd.second : null,
                            c.getString(INDEX_BACKEND_ID));
                    int sentState = c.getInt(c.getColumnIndex(COLUMN_MESSAGE_SEND_STATE));
                    MessageState ms = MessageState.STATE_WAS_READ;
                    if (sentState == 0) {
                        ms = MessageState.STATE_NOT_SENT;
                    } else if (sentState == 1) {
                        ms = MessageState.STATE_SENT;
                    } else if (sentState == 2) {
                        ms = MessageState.STATE_WAS_READ;
                    } else if (sentState == 3) {
                        ms = MessageState.STATE_NOT_SENT;
                    }
                    up.setSentState(ms);
                    phrasesInDb.add(up);
                }
            }
            c.close();
            lastPhraseRequest = System.currentTimeMillis();
            cashedPhrases = phrasesInDb;
        } else {
            phrasesInDb = cashedPhrases;
        }
        if (phrasesInDb.contains(userPhrase)) return;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_ID, userPhrase.getMessageId());
        cv.put(COLUMN_PHRASE, userPhrase.getPhrase());
        cv.put(COLUMN_MESSAGE_SEND_STATE, userPhrase.getSentState().getType());
        cv.put(COLUMN_TIMESTAMP, userPhrase.getTimeStamp());
        cv.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_USER_PHRASE.type);
        cv.put(COLUMN_BACKEND_ID, userPhrase.getBackendId());
        getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
        if (userPhrase.getFileDescription() != null) {
            putFd(userPhrase.getFileDescription(), userPhrase.getMessageId(), false);
        }
        if (userPhrase.getQuote() != null) {
            cv.clear();
            cv.put(COLUMN_QUOTE_MESSAGE_ID_EXT, userPhrase.getMessageId());
            cv.put(COLUMN_QUOTE_HEADER, userPhrase.getQuote().getPhraseOwnerTitle());
            cv.put(COLUMN_QUOTE_BODY, userPhrase.getQuote().getText());
            cv.put(COLUMN_QUOTE_TIMESTAMP, userPhrase.getQuote().getTimeStamp());
            getWritableDatabase().insert(TABLE_QUOTE, null, cv);
            if (userPhrase.getQuote().getFileDescription() != null) {
                putFd(userPhrase.getQuote().getFileDescription(), userPhrase.getMessageId(), true);
            }
        }
    }


    void putChatPhrase(ChatPhrase phrase) {
        if (phrase instanceof ConsultPhrase) {
            insertOrUpdateConsultPhrase((ConsultPhrase) phrase);
        }

        if (phrase instanceof UserPhrase) {
            putUserPhrase((UserPhrase) phrase);
        }
    }

    private void insertOrUpdateConsultPhrase(ConsultPhrase phrase) {
        ContentValues cv = new ContentValues();
        boolean isDup = false;
        Cursor c;
        c = getWritableDatabase().rawQuery("select " + COLUMN_MESSAGE_ID + " from " + TABLE_MESSAGES + " where " + COLUMN_BACKEND_ID + " = ?", new String[]{phrase.getBackendId()});
        if (c.getCount() > 0) isDup = true;
        cv.put(COLUMN_MESSAGE_ID, phrase.getMessageId());
        cv.put(COLUMN_PHRASE, phrase.getPhrase());
        cv.put(COLUMN_TIMESTAMP, phrase.getTimeStamp());
        cv.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_CONSULT_PHRASE.type);
        cv.put(COLUMN_AVATAR_PATH, phrase.getAvatarPath());
        cv.put(COLUMN_CONSULT_ID, phrase.getConsultId());
        cv.put(COLUMN_IS_READ, phrase.isRead());
        cv.put(COLUMN_CONSULT_STATUS, phrase.getStatus());
        cv.put(COLUMN_NAME, phrase.getConsultName());
        cv.put(COLUMN_SEX, phrase.getSex());
        cv.put(COLUMN_BACKEND_ID, phrase.getBackendId());
        if (!isDup) {
            getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
        } else {
            getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_ID + " = ? ", new String[]{phrase.getId()});
        }
        if (phrase.getFileDescription() != null) {
            putFd(phrase.getFileDescription(), phrase.getMessageId(), false);
        }
        if (phrase.getQuote() != null) {
            cv.clear();
            cv.put(COLUMN_QUOTE_MESSAGE_ID_EXT, phrase.getMessageId());
            cv.put(COLUMN_QUOTE_HEADER, phrase.getQuote().getPhraseOwnerTitle());
            cv.put(COLUMN_QUOTE_BODY, phrase.getQuote().getText());
            cv.put(COLUMN_QUOTE_TIMESTAMP, phrase.getQuote().getTimeStamp());
            getWritableDatabase().insert(TABLE_QUOTE, null, cv);
            if (phrase.getQuote().getFileDescription() != null) {
                putFd(phrase.getQuote().getFileDescription(), phrase.getMessageId(), true);
            }
        }
        c.close();
    }


    void setUserPhraseState(String messageId, MessageState messageState) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_SEND_STATE, messageState.getType());
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_ID + " = ?", new String[]{messageId});
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_BACKEND_ID + " = ?", new String[]{messageId});
    }

    void setUserPhraseMessageId(String oldMessageId, String newMessageId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MESSAGE_ID, newMessageId);
        getWritableDatabase().update(TABLE_MESSAGES, cv, COLUMN_MESSAGE_ID + " = ?", new String[]{oldMessageId});
        cv.clear();
        cv.put(COLUMN_QUOTE_MESSAGE_ID_EXT, newMessageId);
        getWritableDatabase().update(TABLE_QUOTE, cv, COLUMN_QUOTE_MESSAGE_ID_EXT + " = ?", new String[]{oldMessageId});
        cv.clear();
        cv.put(COLUMN_FD_MESSAGE_ID_EXT, newMessageId);
        getWritableDatabase().update(TABLE_FILE_DESCRIPTION, cv, COLUMN_FD_MESSAGE_ID_EXT + " = ?", new String[]{oldMessageId});
    }

    void putConsultConnected(ConsultConnectionMessage consultConnectionMessage) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, consultConnectionMessage.getName());
        cv.put(COLUMN_TIMESTAMP, consultConnectionMessage.getTimeStamp());
        cv.put(COLUMN_AVATAR_PATH, consultConnectionMessage.getAvatarPath());
        cv.put(COLUMN_MESSAGE_TYPE, MessageTypes.TYPE_CONSULT_CONNECTED.type);
        cv.put(COLUMN_SEX, consultConnectionMessage.getSex() ? "1" : "0");
        cv.put(COLUMN_CONNECTION_TYPE, consultConnectionMessage.getConnectionType());
        cv.put(COLUMN_CONSULT_ID, consultConnectionMessage.getConsultId());
        cv.put(COLUMN_CONSULT_STATUS, consultConnectionMessage.getStatus());
        cv.put(COLUMN_CONSULT_TITLE, consultConnectionMessage.getTitle());
        cv.put(COLUMN_MESSAGE_ID, consultConnectionMessage.getMessageId());
        if (consultConnectionMessage.getName() == null) {
            getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
            return;
        }
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_NAME + " , " + COLUMN_TIMESTAMP + " from " + TABLE_MESSAGES + " where " + COLUMN_NAME + " = ? and " + COLUMN_TIMESTAMP + " = ? ", new String[]{consultConnectionMessage.getName(), "" + consultConnectionMessage.getTimeStamp()});
        if (c.getCount() == 0) {
            c.close();
            getWritableDatabase().insert(TABLE_MESSAGES, null, cv);
        }
    }

    public List<ChatPhrase> getSortedPhrases(String query) {
        List<ChatPhrase> list = new ArrayList<>();
        if (query == null) return list;
        List<ChatItem> chatItems = getChatItems(0, -1);
        for (ChatItem chatItem : chatItems) {
            if (chatItem instanceof UserPhrase) {
                if (((UserPhrase) chatItem).getPhraseText() != null && ((UserPhrase) chatItem).getPhraseText().toLowerCase().contains(query.toLowerCase())) {
                    list.add((UserPhrase) chatItem);
                }
            }
            if (chatItem instanceof ConsultPhrase) {
                if (((ConsultPhrase) chatItem).getPhraseText() != null && ((ConsultPhrase) chatItem).getPhraseText().toLowerCase().contains(query.toLowerCase())) {
                    list.add((ConsultPhrase) chatItem);
                }
            }
        }
        return list;
    }

    ConsultPhrase getLastUnreadPhrase() {
        Cursor c = getWritableDatabase().rawQuery("select * from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_TYPE + " = " + MessageTypes.TYPE_CONSULT_PHRASE.type
                + " and " + COLUMN_IS_READ + " = 0 order by " + COLUMN_TIMESTAMP + " desc", new String[]{});
        if (c.getCount() > 0) {
            c.moveToFirst();
            final int INDEX_NAME = c.getColumnIndex(COLUMN_NAME);
            final int INDEX_AVATAR_PATH = c.getColumnIndex(COLUMN_AVATAR_PATH);
            final int INDEX_TIMESTAMP = c.getColumnIndex(COLUMN_TIMESTAMP);
            final int INDEX_PHRASE = c.getColumnIndex(COLUMN_PHRASE);
            final int INDEX_MESSAGE_ID = c.getColumnIndex(COLUMN_MESSAGE_ID);
            final int INDEX_BACKEND_ID = c.getColumnIndex(COLUMN_BACKEND_ID);
            final int INDEX_CONSULT_ID = c.getColumnIndex(COLUMN_CONSULT_ID);
            final int INDEX_IS_READ = c.getColumnIndex(COLUMN_IS_READ);
            final int INDEX_SEX = c.getColumnIndex(COLUMN_SEX);
            String avatarPath = c.isNull(INDEX_AVATAR_PATH) ? null : c.getString(INDEX_AVATAR_PATH);
            String phrase = c.isNull(INDEX_PHRASE) ? null : c.getString(INDEX_PHRASE);
            String name = c.isNull(INDEX_NAME) ? null : c.getString(INDEX_NAME);
            String status = c.isNull(c.getColumnIndex(COLUMN_CONSULT_STATUS)) ? null : c.getString(c.getColumnIndex(COLUMN_CONSULT_STATUS));
            boolean isRead = c.getInt(INDEX_IS_READ) == 1;
            Pair<Boolean, FileDescription> fd = getFd(c.getString(INDEX_MESSAGE_ID));
            ConsultPhrase cp = new ConsultPhrase(
                    fd != null && !fd.first ? fd.second : null,
                    getQuote(c.getString(INDEX_MESSAGE_ID)),
                    name,
                    c.getString(INDEX_MESSAGE_ID),
                    phrase,
                    c.getLong(INDEX_TIMESTAMP),
                    c.getString(INDEX_CONSULT_ID),
                    avatarPath
                    , isRead
                    , status
                    , c.getInt(INDEX_SEX) == 1
                    , c.getString(INDEX_BACKEND_ID)
            );
            c.close();
            return cp;
        }
        return null;
    }

    public List<ChatItem> getChatItems(int offset, int limit) {
        List<ChatItem> items = new ArrayList<>();
        String query = String.format(Locale.US, "select * from (select * from %s order by %s desc limit %s offset %s) order by %s asc", TABLE_MESSAGES, COLUMN_TIMESTAMP, String.valueOf(limit), String.valueOf(offset), COLUMN_TIMESTAMP);
        Cursor c = getWritableDatabase().rawQuery(query, null);
        if (c.getCount() == 0) {
            c.close();
            return items;
        }
        final int INDEX_NAME = c.getColumnIndex(COLUMN_NAME);
        final int INDEX_AVATAR_PATH = c.getColumnIndex(COLUMN_AVATAR_PATH);
        final int INDEX_TIMESTAMP = c.getColumnIndex(COLUMN_TIMESTAMP);
        final int INDEX_PHRASE = c.getColumnIndex(COLUMN_PHRASE);
        final int INDEX_MESSAGE_ID = c.getColumnIndex(COLUMN_MESSAGE_ID);
        final int INDEX_BACKEND_ID = c.getColumnIndex(COLUMN_BACKEND_ID);
        final int INDEX_CONNECTION_TYPE = c.getColumnIndex(COLUMN_CONNECTION_TYPE);
        final int INDEX_CONSULT_ID = c.getColumnIndex(COLUMN_CONSULT_ID);
        final int INDEX_IS_READ = c.getColumnIndex(COLUMN_IS_READ);
        final int INDEX_SEX = c.getColumnIndex(COLUMN_SEX);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            int type = c.getInt(c.getColumnIndex(COLUMN_MESSAGE_TYPE));
            if (type == MessageTypes.TYPE_CONSULT_CONNECTED.type) {
                boolean sex = c.getInt(c.getColumnIndex(COLUMN_SEX)) == 1;
                String name = c.isNull(INDEX_NAME) ? null : c.getString(INDEX_NAME);
                String avatarPath = c.isNull(INDEX_AVATAR_PATH) ? null : c.getString(INDEX_AVATAR_PATH);
                String connectionType = c.getString(INDEX_CONNECTION_TYPE);
                int indexStatus = c.getColumnIndex(COLUMN_CONSULT_STATUS);
                int indextitle = c.getColumnIndex(COLUMN_CONSULT_TITLE);
                String status = c.isNull(indexStatus) ? null : c.getString(indexStatus);
                String title = c.isNull(indextitle) ? null : c.getString(indextitle);
                ConsultConnectionMessage cc =
                        new ConsultConnectionMessage(c.getString(INDEX_CONSULT_ID), connectionType, name, sex, c.getLong(INDEX_TIMESTAMP), avatarPath, status, title, c.getString(INDEX_MESSAGE_ID), false);
                items.add(cc);
            } else if (type == MessageTypes.TYPE_CONSULT_PHRASE.type) {
                String avatarPath = c.isNull(INDEX_AVATAR_PATH) ? null : c.getString(INDEX_AVATAR_PATH);
                String phrase = c.isNull(INDEX_PHRASE) ? null : c.getString(INDEX_PHRASE);
                String name = c.isNull(INDEX_NAME) ? null : c.getString(INDEX_NAME);
                String status = c.isNull(c.getColumnIndex(COLUMN_CONSULT_STATUS)) ? null : c.getString(c.getColumnIndex(COLUMN_CONSULT_STATUS));
                boolean isRead = c.getInt(INDEX_IS_READ) == 1;
                Pair<Boolean, FileDescription> fd = getFd(c.getString(INDEX_MESSAGE_ID));
                ConsultPhrase cp = new ConsultPhrase(
                        fd != null && !fd.first ? fd.second : null,
                        getQuote(c.getString(INDEX_MESSAGE_ID)),
                        name,
                        c.getString(INDEX_MESSAGE_ID),
                        phrase,
                        c.getLong(INDEX_TIMESTAMP),
                        c.getString(INDEX_CONSULT_ID),
                        avatarPath
                        , isRead
                        , status
                        , c.getInt(INDEX_SEX) == 1
                        , c.getString(INDEX_BACKEND_ID)
                );
                items.add(cp);
            } else if (type == MessageTypes.TYPE_USER_PHRASE.type) {
                String phrase = c.isNull(INDEX_PHRASE) ? null : c.getString(INDEX_PHRASE);
                Pair<Boolean, FileDescription> fd = getFd(c.getString(INDEX_MESSAGE_ID));
                UserPhrase up = new UserPhrase(
                        c.getString(INDEX_MESSAGE_ID),
                        phrase,
                        getQuote(c.getString(INDEX_MESSAGE_ID)),
                        c.getLong(INDEX_TIMESTAMP),
                        fd != null && !fd.first ? fd.second : null,
                        c.getString(INDEX_BACKEND_ID));
                int sentState = c.getInt(c.getColumnIndex(COLUMN_MESSAGE_SEND_STATE));
                MessageState ms = MessageState.STATE_WAS_READ;
                if (sentState == 0) {
                    ms = MessageState.STATE_NOT_SENT;
                } else if (sentState == 1) {
                    ms = MessageState.STATE_SENT;
                } else if (sentState == 2) {
                    ms = MessageState.STATE_WAS_READ;
                } else if (sentState == 3) {
                    ms = MessageState.STATE_NOT_SENT;
                }
                up.setSentState(ms);
                items.add(up);
            }
        }
        c.close();
        return items;
    }

    private Quote getQuote(String messageId) {
        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_QUOTE, COLUMN_QUOTE_MESSAGE_ID_EXT);
        Cursor c = getWritableDatabase().rawQuery(query, new String[]{messageId});
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        String header = c.isNull(c.getColumnIndex(COLUMN_QUOTE_HEADER)) ? null : c.getString(c.getColumnIndex(COLUMN_QUOTE_HEADER));
        String body = c.isNull(c.getColumnIndex(COLUMN_QUOTE_BODY)) ? null : c.getString(c.getColumnIndex(COLUMN_QUOTE_BODY));
        Pair<Boolean, FileDescription> quoteFd = getFd(messageId);
        Quote q = new Quote(header, body, quoteFd != null && quoteFd.first ? quoteFd.second : null, c.getLong(c.getColumnIndex(COLUMN_QUOTE_TIMESTAMP)));
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

    private Pair<Boolean, FileDescription> getFd(String messageId) {
        String query = String.format(Locale.US, "select * from %s where %s = ?", TABLE_FILE_DESCRIPTION, COLUMN_FD_MESSAGE_ID_EXT);
        Cursor c = getWritableDatabase().rawQuery(query, new String[]{messageId});
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        String header = c.isNull(c.getColumnIndex(COLUMN_FD_HEADER)) ? null : c.getString(c.getColumnIndex(COLUMN_FD_HEADER));
        String path = c.isNull(c.getColumnIndex(COLUMN_FD_PATH)) ? null : c.getString(c.getColumnIndex(COLUMN_FD_PATH));
        Integer progress = c.isNull(c.getColumnIndex(COLUMN_FD_DOWNLOAD_PROGRESS)) ? 0 : c.getInt(c.getColumnIndex(COLUMN_FD_DOWNLOAD_PROGRESS));
        FileDescription fd = new FileDescription(header, path, c.getLong(c.getColumnIndex(COLUMN_FD_SIZE)), c.getLong(c.getColumnIndex(COLUMN_FD_TIMESTAMP)));
        fd.setDownloadProgress(progress);
        fd.setDownloadPath(c.getString(c.getColumnIndex(COLUMN_FD_DOWNLOAD_PATH)));
        fd.setIncomingName(c.getString(c.getColumnIndex(COLUMN_FD_INCOMING_FILENAME)));
        boolean isFromQuote = c.getInt(c.getColumnIndex(COLUMN_FD_IS_FROM_QUOTE)) == 1;
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
        int fdHeaderIndex = c.getColumnIndex(COLUMN_FD_HEADER);
        int fdPAthIndex = c.getColumnIndex(COLUMN_FD_PATH);
        int fdProgress = c.getColumnIndex(COLUMN_FD_DOWNLOAD_PROGRESS);
        int fdTimeStamp = c.getColumnIndex(COLUMN_FD_TIMESTAMP);
        int fdFilename = c.getColumnIndex(COLUMN_FD_INCOMING_FILENAME);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String header = c.isNull(fdHeaderIndex) ? null : c.getString(fdHeaderIndex);
            String path = c.isNull(fdPAthIndex) ? null : c.getString(fdPAthIndex);
            Integer progress = c.isNull(fdProgress) ? 0 : c.getInt(fdProgress);
            FileDescription fd = new FileDescription(header, path, c.getLong(c.getColumnIndex(COLUMN_FD_SIZE)), c.getLong(fdTimeStamp));
            fd.setDownloadProgress(progress);
            fd.setIncomingName(c.isNull(fdFilename) ? null : c.getString(fdFilename));
            fd.setDownloadPath(c.getString(c.getColumnIndex(COLUMN_FD_DOWNLOAD_PATH)));
            list.add(fd);
        }
        c.close();
        return list;
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

    void setAllRead() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        getWritableDatabase().update(
                TABLE_MESSAGES
                , cv
                , COLUMN_MESSAGE_TYPE + "  = " + MessageTypes.TYPE_CONSULT_PHRASE.type + " and " + COLUMN_IS_READ + " = ?"
                , new String[]{String.valueOf(0)});
    }

    void setMessageWereRead(String consultMessageId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IS_READ, true);
        String args[] = new String[1];
        args[0] = consultMessageId;
        getWritableDatabase().update(
                TABLE_MESSAGES
                , cv
                , COLUMN_MESSAGE_TYPE + "  = " + MessageTypes.TYPE_CONSULT_PHRASE.type + " and " + COLUMN_MESSAGE_ID + " = ? "
                , args);
    }

    List<String> getUnreadMessagesId() {
        ArrayList<String> ids = new ArrayList<>();
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_MESSAGE_ID +
                " from " + TABLE_MESSAGES
                + " where " + COLUMN_MESSAGE_TYPE + " = " + MessageTypes.TYPE_CONSULT_PHRASE.type
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
        cv.put(COLUMN_FD_MESSAGE_ID_EXT, id);
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
                        + " like ? and " + COLUMN_FD_DOWNLOAD_PATH + " like ?"
                , new String[]{fileDescription.getIncomingName(), fileDescription.getDownloadPath()});
    }

    ChatPhrase getChatphraseByDescription(FileDescription fileDescription) {
        if (fileDescription == null) return null;
        ChatPhrase cp = null;
        Cursor c = getWritableDatabase().query(true
                , TABLE_FILE_DESCRIPTION
                , new String[]{COLUMN_FD_MESSAGE_ID_EXT}
                , COLUMN_FD_SIZE + " = " + fileDescription.getSize()
                        + " and " + COLUMN_FD_DOWNLOAD_PATH + " like " + fileDescription.getDownloadPath()
                        + " and " + COLUMN_FD_HEADER + " like " + fileDescription.getFrom()
                , new String[]{}, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            String id = c.getString(c.getColumnIndex(COLUMN_FD_MESSAGE_ID_EXT));
            c = getWritableDatabase().rawQuery("select * from " + TABLE_MESSAGES + " where " + COLUMN_MESSAGE_ID + " like " + id, new String[]{});
            c.moveToFirst();
            final int INDEX_NAME = c.getColumnIndex(COLUMN_NAME);
            final int INDEX_AVATAR_PATH = c.getColumnIndex(COLUMN_AVATAR_PATH);
            final int INDEX_TIMESTAMP = c.getColumnIndex(COLUMN_TIMESTAMP);
            final int INDEX_PHRASE = c.getColumnIndex(COLUMN_PHRASE);
            final int INDEX_MESSAGE_ID = c.getColumnIndex(COLUMN_MESSAGE_ID);
            final int INDEX_BACKEND_ID = c.getColumnIndex(COLUMN_BACKEND_ID);
            final int INDEX_CONNECTION_TYPE = c.getColumnIndex(COLUMN_CONNECTION_TYPE);
            final int INDEX_CONSULT_ID = c.getColumnIndex(COLUMN_CONSULT_ID);
            final int INDEX_IS_READ = c.getColumnIndex(COLUMN_IS_READ);
            final int INDEX_SEX = c.getColumnIndex(COLUMN_SEX);
            int type = c.getInt(c.getColumnIndex(COLUMN_MESSAGE_TYPE));
            if (type == MessageTypes.TYPE_CONSULT_PHRASE.type) {
                String avatarPath = c.isNull(INDEX_AVATAR_PATH) ? null : c.getString(INDEX_AVATAR_PATH);
                String phrase = c.isNull(INDEX_PHRASE) ? null : c.getString(INDEX_PHRASE);
                String name = c.isNull(INDEX_NAME) ? null : c.getString(INDEX_NAME);
                String status = c.isNull(c.getColumnIndex(COLUMN_CONSULT_STATUS)) ? null : c.getString(c.getColumnIndex(COLUMN_CONSULT_STATUS));
                boolean isRead = c.getInt(INDEX_IS_READ) == 1;
                Pair<Boolean, FileDescription> fd = getFd(c.getString(INDEX_MESSAGE_ID));
                cp = new ConsultPhrase(
                        fd != null && !fd.first ? fd.second : null,
                        getQuote(c.getString(INDEX_MESSAGE_ID)),
                        name,
                        c.getString(INDEX_MESSAGE_ID),
                        phrase,
                        c.getLong(INDEX_TIMESTAMP),
                        c.getString(INDEX_CONSULT_ID),
                        avatarPath
                        , isRead
                        , status
                        , c.getInt(INDEX_SEX) == 1
                        , c.getString(INDEX_BACKEND_ID)
                );
            } else if (type == MessageTypes.TYPE_USER_PHRASE.type) {
                String phrase = c.isNull(INDEX_PHRASE) ? null : c.getString(INDEX_PHRASE);
                Pair<Boolean, FileDescription> fd = getFd(c.getString(INDEX_MESSAGE_ID));
                cp = new UserPhrase(
                        c.getString(INDEX_MESSAGE_ID),
                        phrase,
                        getQuote(c.getString(INDEX_MESSAGE_ID)),
                        c.getLong(INDEX_TIMESTAMP),
                        fd != null && !fd.first ? fd.second : null,
                        c.getString(INDEX_BACKEND_ID)
                );
                int sentState = c.getInt(c.getColumnIndex(COLUMN_MESSAGE_SEND_STATE));
                MessageState ms = MessageState.STATE_WAS_READ;
                if (sentState == 0) {
                    ms = MessageState.STATE_NOT_SENT;
                } else if (sentState == 1) {
                    ms = MessageState.STATE_SENT;
                } else if (sentState == 2) {
                    ms = MessageState.STATE_WAS_READ;
                } else if (sentState == 3) {
                    ms = MessageState.STATE_NOT_SENT;
                }
                ((UserPhrase) cp).setSentState(ms);
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
        return c.getString(c.getColumnIndex(COLUMN_AVATAR_PATH));
    }

    void cleanDb() {
        getWritableDatabase().execSQL("delete * from " + TABLE_MESSAGES);
        getWritableDatabase().execSQL("delete * from " + TABLE_FILE_DESCRIPTION);
        getWritableDatabase().execSQL("delete * from " + TABLE_QUOTE);
    }

    ConsultInfo getLastConsultInfo(String id) {
        Cursor c = getWritableDatabase().rawQuery("select " + COLUMN_AVATAR_PATH + ", " + COLUMN_NAME + ", " + COLUMN_CONSULT_STATUS
                + " from " + TABLE_MESSAGES
                + " where " + COLUMN_CONSULT_ID + " =  ? "
                + " order by " + COLUMN_TIMESTAMP + " desc", new String[]{id});
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        return new ConsultInfo(c.getString(c.getColumnIndex(COLUMN_NAME))
                , id
                , c.getString(c.getColumnIndex(COLUMN_CONSULT_STATUS))
                , c.getString(c.getColumnIndex(COLUMN_AVATAR_PATH)));
    }
}
