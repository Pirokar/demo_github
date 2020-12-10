package im.threads.internal.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import im.threads.internal.Config;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public final class DatabaseHolder {

    private static DatabaseHolder instance;
    private final ThreadsDbHelper mMyOpenHelper;

    private DatabaseHolder() {
        mMyOpenHelper = new ThreadsDbHelper(Config.instance.context);
    }

    @NonNull
    public static DatabaseHolder getInstance() {
        if (instance == null) {
            instance = new DatabaseHolder();
        }
        return instance;
    }

    // ChatItems

    /**
     * Nullify instance. For Autotests purposes
     */
    static void eraseInstance() {
        instance = null;
    }

    public void cleanDatabase() {
        mMyOpenHelper.cleanDatabase();
    }

    public List<ChatItem> getChatItems(int offset, int limit) {
        return mMyOpenHelper.getChatItems(offset, limit);
    }

    @Nullable
    public ChatItem getChatItem(String messageUuid) {
        return mMyOpenHelper.getChatItem(messageUuid);
    }

    public void putChatItems(final List<ChatItem> items) {
        mMyOpenHelper.putChatItems(items);
    }

    public boolean putChatItem(ChatItem chatItem) {
        return mMyOpenHelper.putChatItem(chatItem);
    }

    // FileDescriptions

    public Single<List<FileDescription>> getAllFileDescriptions() {
        return Single.fromCallable(mMyOpenHelper::getAllFileDescriptions)
                .subscribeOn(Schedulers.io());
    }

    // UserPhrase

    public void updateFileDescription(@NonNull FileDescription fileDescription) {
        mMyOpenHelper.updateFileDescription(fileDescription);
    }

    @Nullable
    public ConsultInfo getConsultInfo(@NonNull String id) {
        return mMyOpenHelper.getLastConsultInfo(id);
    }

    public List<UserPhrase> getUnsendUserPhrase(int count) {
        return mMyOpenHelper.getUnsendUserPhrase(count);
    }

    // ConsultPhrase

    public void setStateOfUserPhraseByProviderId(String providerId, MessageState messageState) {
        mMyOpenHelper.setUserPhraseStateByProviderId(providerId, messageState);
    }

    public Single<ConsultPhrase> getLastConsultPhrase() {
        return Single.fromCallable(mMyOpenHelper::getLastConsultPhrase)
                .subscribeOn(Schedulers.io());
    }

    public Completable setAllConsultMessagesWereRead() {
        return Completable.fromCallable(mMyOpenHelper::setAllConsultMessagesWereRead)
                .subscribeOn(Schedulers.io());
    }

    public void setConsultMessageWasRead(String providerId) {
        mMyOpenHelper.setConsultMessageWasRead(providerId);
    }

    // Survey
    @Nullable
    public Survey getSurvey(long sendingId) {
        return mMyOpenHelper.getSurvey(sendingId);
    }

    public Completable setOldSurveyDisplayMessageToFalse() {
        return Completable.fromCallable(mMyOpenHelper::setOldSurveyDisplayMessageToFalse)
                .subscribeOn(Schedulers.io());
    }

    // Messages
    public int getMessagesCount() {
        return mMyOpenHelper.getMessagesCount();
    }

    // let the DB time to write the incoming message
    public int getUnreadMessagesCount() {
        return mMyOpenHelper.getUnreadMessagesCount();
    }

    @NonNull
    public List<String> getUnreadMessagesUuid() {
        return mMyOpenHelper.getUnreadMessagesUuid();
    }

    /**
     * For Autotests purposes
     *
     * @return MyOpenHelper instance
     */
    ThreadsDbHelper getMyOpenHelper() {
        return mMyOpenHelper;
    }


}
