package im.threads.internal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.util.List;

import im.threads.internal.database.table.FileDescriptionsTable;
import im.threads.internal.database.table.MessagesTable;
import im.threads.internal.database.table.QuestionsTable;
import im.threads.internal.database.table.QuickRepliesTable;
import im.threads.internal.database.table.QuotesTable;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.SpeechMessageUpdate;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;

/**
 * обертка для БД
 */
public final class ThreadsDbHelper extends SQLiteOpenHelper implements DBHelper {

    private static final int VERSION = 15;

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
    public ChatItem getChatItem(String messageUuid) {
        return messagesTable.getChatItem(this, messageUuid);
    }

    @Override
    public void putChatItems(List<ChatItem> items) {
        messagesTable.putChatItems(this, items);
    }

    @Override
    public boolean putChatItem(ChatItem chatItem) {
        return messagesTable.putChatItem(this, chatItem);
    }

    @Override
    public List<FileDescription> getAllFileDescriptions() {
        return fileDescriptionTable.getAllFileDescriptions(this);
    }

    @Override
    public void updateFileDescription(@NonNull FileDescription fileDescription) {
        fileDescriptionTable.updateFileDescription(this, fileDescription);
    }

    @Override
    public ConsultInfo getLastConsultInfo(@NonNull String id) {
        return messagesTable.getLastConsultInfo(this, id);
    }

    @Override
    public List<UserPhrase> getUnsendUserPhrase(int count) {
        return messagesTable.getUnsendUserPhrase(this, count);
    }

    @Override
    public void setUserPhraseStateByProviderId(String providerId, MessageState messageState) {
        messagesTable.setUserPhraseStateByProviderId(this, providerId, messageState);
    }

    @Override
    public ConsultPhrase getLastConsultPhrase() {
        return messagesTable.getLastConsultPhrase(this);
    }

    @Override
    public int setAllConsultMessagesWereRead() {
        return messagesTable.setAllMessagesWereRead(this);
    }

    @Override
    public void setMessageWasRead(String uuid) {
        messagesTable.setMessageWasRead(this, uuid);
    }

    @Override
    public Survey getSurvey(long sendingId) {
        return messagesTable.getSurvey(this, sendingId);
    }

    @Override
    public int setNotSentSurveyDisplayMessageToFalse() {
        return messagesTable.setNotSentSurveyDisplayMessageToFalse(this);
    }

    @Override
    public int setOldRequestResolveThreadDisplayMessageToFalse() {
        return messagesTable.setOldRequestResolveThreadDisplayMessageToFalse(this);
    }

    @Override
    public int getMessagesCount() {
        return messagesTable.getMessagesCount(this);
    }

    @Override
    public int getUnreadMessagesCount() {
        return messagesTable.getUnreadMessagesCount(this);
    }

    @NonNull
    @Override
    public List<String> getUnreadMessagesUuid() {
        return messagesTable.getUnreadMessagesUuid(this);
    }

    public void speechMessageUpdated(SpeechMessageUpdate speechMessageUpdate) {
        messagesTable.speechMessageUpdated(this, speechMessageUpdate);
    }
}
