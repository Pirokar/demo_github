package im.threads.business.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.util.List;

import im.threads.business.database.table.FileDescriptionsTable;
import im.threads.business.database.table.MessagesTable;
import im.threads.business.database.table.QuestionsTable;
import im.threads.business.database.table.QuickRepliesTable;
import im.threads.business.database.table.QuotesTable;
import im.threads.business.models.ChatItem;
import im.threads.business.models.FileDescription;

/**
 * обертка для БД
 */
public final class ThreadsDbHelper extends SQLiteOpenHelper implements DBHelper {

    private static final int VERSION = 16;

    private QuotesTable quotesTable;
    private QuickRepliesTable quickRepliesTable;
    private FileDescriptionsTable fileDescriptionTable;
    private QuestionsTable questionsTable;
    private MessagesTable messagesTable;
    private boolean isTablesPrepared = false;

    public ThreadsDbHelper(Context context) {
        super(context, "messages.db", null, VERSION);
        initTables();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        initTables();
        messagesTable.createTable(db);
        quotesTable.createTable(db);
        quickRepliesTable.createTable(db);
        fileDescriptionTable.createTable(db);
        questionsTable.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        initTables();
        if (oldVersion < VERSION) {
            messagesTable.upgradeTable(db, oldVersion, newVersion);
            quotesTable.upgradeTable(db, oldVersion, newVersion);
            quickRepliesTable.upgradeTable(db, oldVersion, newVersion);
            fileDescriptionTable.upgradeTable(db, oldVersion, newVersion);
            questionsTable.upgradeTable(db, oldVersion, newVersion);
            onCreate(db);
        }
        // VERSION 6 - quotes table uuid added, column names changed
        // dropping data with old file paths starting with "file://" prefix
    }

    @Override
    public void cleanDatabase() {
        fileDescriptionTable.cleanTable(this);
        messagesTable.cleanTable(this);
        quotesTable.cleanTable(this);
        quickRepliesTable.cleanTable(this);
        questionsTable.cleanTable(this);
    }

    private void initTables() {
        if (!isTablesPrepared) {
            fileDescriptionTable = new FileDescriptionsTable();
            questionsTable = new QuestionsTable();
            quotesTable = new QuotesTable(fileDescriptionTable);
            quickRepliesTable = new QuickRepliesTable();
            messagesTable = new MessagesTable(fileDescriptionTable, quotesTable, quickRepliesTable, questionsTable);
            isTablesPrepared = true;
        }
    }

    @NonNull
    @Override
    public List<ChatItem> getChatItems(int offset, int limit) {
        return messagesTable.getChatItems(this, offset, limit);
    }

    @Override
    public List<FileDescription> getAllFileDescriptions() {
        return fileDescriptionTable.getAllFileDescriptions(this);
    }
}
