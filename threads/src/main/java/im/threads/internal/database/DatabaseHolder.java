package im.threads.internal.database;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import im.threads.ThreadsLib;
import im.threads.internal.Config;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.CompletionHandler;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.utils.ThreadsLogger;

public final class DatabaseHolder {
    private static final String TAG = DatabaseHolder.class.getSimpleName();

    private static DatabaseHolder instance;
    private final MyOpenHelper mMyOpenHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @NonNull
    public static DatabaseHolder getInstance() {
        if (instance == null) {
            instance = new DatabaseHolder();
        }
        return instance;
    }

    /**
     * Nullify instance. For Autotests purposes
     */
    static void eraseInstance() {
        instance = null;
    }

    private DatabaseHolder() {
        mMyOpenHelper = new MyOpenHelper(Config.instance.context);
    }

    /**
     * For Autotests purposes
     *
     * @return MyOpenHelper instance
     */
    MyOpenHelper getMyOpenHelper() {
        return mMyOpenHelper;
    }

    public List<ChatItem> getChatItems(int offset, int limit) {
        return mMyOpenHelper.getChatItems(offset, limit);
    }

    public List<UserPhrase> getUnsendUserPhrase(int count) {
        List<UserPhrase> userPhrases = new ArrayList<>();
        List<ChatItem> chatItems = mMyOpenHelper.getChatItems(0, count);
        for (ChatItem chatItem : chatItems) {
            if (chatItem instanceof UserPhrase) {
                if (((UserPhrase) chatItem).getSentState() == MessageState.STATE_NOT_SENT) {
                    userPhrases.add((UserPhrase) chatItem);
                }
            }
        }
        return userPhrases;
    }

    public void getLastConsultPhrase(final CompletionHandler<ConsultPhrase> handler) {
        executorService.execute(() -> {
            ConsultPhrase cp = mMyOpenHelper.getLastConsultPhrase();
            handler.onComplete(cp);
        });
    }

    @Nullable
    public ChatItem getChatItem(String messageUuid) {
        return mMyOpenHelper.getChatItem(messageUuid);
    }

    @Nullable
    public Survey getSurvey(long sendingId) {
        return mMyOpenHelper.getSurvey(sendingId);
    }

    @Nullable
    public ConsultInfo getConsultInfo(@NonNull String id) {
        return mMyOpenHelper.getLastConsultInfo(id);
    }

    public List<String> getUnreadMessagesProviderIds() {
        return mMyOpenHelper.getUnreadMessagesProviderIds();
    }

    public int getMessagesCount() {
        return mMyOpenHelper.getMessagesCount();
    }

    // let the DB time to write the incoming message
    public void getUnreadMessagesCount(boolean immediate, @NonNull final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener) {
        if (immediate) {
            getUnreadMessagesCount(unreadMessagesCountListener);
        } else {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> getUnreadMessagesCount(unreadMessagesCountListener), 1000);
        }
    }

    public void putMessagesSync(final List<ChatItem> items) {
        try {
            mMyOpenHelper.getWritableDatabase().beginTransaction();
            for (ChatItem item : items) {
                mMyOpenHelper.putChatItem(item);
            }
            mMyOpenHelper.getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            ThreadsLogger.e(TAG, "putMessagesSync", e);
        } finally {
            mMyOpenHelper.getWritableDatabase().endTransaction();
        }
    }

    public boolean putChatItem(ChatItem chatItem) {
        return mMyOpenHelper.putChatItem(chatItem);
    }

    @WorkerThread
    public void setStateOfUserPhraseByProviderId(String providerId, MessageState messageState) {
        mMyOpenHelper.setUserPhraseStateByProviderId(providerId, messageState);
    }

    public void setAllConsultMessagesWereRead(final CompletionHandler<Void> handler) {
        executorService.execute(() -> {
            mMyOpenHelper.setAllConsultMessagesWereRead();
            handler.onComplete(null);
        });
    }

    public void setConsultMessageWasRead(String providerId) {
        mMyOpenHelper.setConsultMessageWasRead(providerId);
    }

    public void getAllFileDescriptions(final CompletionHandler<List<FileDescription>> handler) {
        executorService.execute(() -> handler.onComplete(mMyOpenHelper.getAllFileDescriptions()));
    }

    public void updateFileDescription(@NonNull FileDescription fileDescription) {
        mMyOpenHelper.updateFileDescription(fileDescription);
    }

    public void cleanDatabase() {
        mMyOpenHelper.cleanDatabase();
    }

    private void getUnreadMessagesCount(@NonNull final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener) {
        unreadMessagesCountListener.onUnreadMessagesCountChanged(mMyOpenHelper.getUnreadMessagesCount());
    }
}
