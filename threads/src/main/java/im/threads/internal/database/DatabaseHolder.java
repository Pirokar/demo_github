package im.threads.internal.database;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import im.threads.ThreadsLib;
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
        return Single.fromCallable(() -> mMyOpenHelper.getAllFileDescriptions())
                .subscribeOn(Schedulers.io());
    }

    public void updateFileDescription(@NonNull FileDescription fileDescription) {
        mMyOpenHelper.updateFileDescription(fileDescription);
    }

    // UserPhrase

    @Nullable
    public ConsultInfo getConsultInfo(@NonNull String id) {
        return mMyOpenHelper.getLastConsultInfo(id);
    }

    public List<UserPhrase> getUnsendUserPhrase(int count) {
        return mMyOpenHelper.getUnsendUserPhrase(count);
    }

    public void setStateOfUserPhraseByProviderId(String providerId, MessageState messageState) {
        mMyOpenHelper.setUserPhraseStateByProviderId(providerId, messageState);
    }

    // ConsultPhrase

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

    // Messages
    public int getMessagesCount() {
        return mMyOpenHelper.getMessagesCount();
    }

    // let the DB time to write the incoming message
    public void getUnreadMessagesCount(@NonNull final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener) {
        // Почему именно 1000 не знает никто...
        new Handler(Looper.getMainLooper()).postDelayed(() -> unreadMessagesCountListener.onUnreadMessagesCountChanged(mMyOpenHelper.getUnreadMessagesCount()), 1000);
    }

    public List<String> getUnreadMessagesProviderIds() {
        return mMyOpenHelper.getUnreadMessagesProviderIds();
    }



    /**
     * For Autotests purposes
     *
     * @return MyOpenHelper instance
     */
    ThreadsDbHelper getMyOpenHelper() {
        return mMyOpenHelper;
    }

    /**
     * Nullify instance. For Autotests purposes
     */
    static void eraseInstance() {
        instance = null;
    }


}
