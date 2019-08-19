package im.threads.internal.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import im.threads.ThreadsLib;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ChatPhrase;
import im.threads.internal.model.CompletionHandler;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;

public class DatabaseHolder {
    private static final String TAG = DatabaseHolder.class.getSimpleName();

    private static DatabaseHolder instance;
    private final MyOpenHelper mMyOpenHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static DatabaseHolder getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHolder(context);
        }
        return instance;
    }

    /**
     * Nullify instance. For Autotests purposes
     */
    static void eraseInstance() {
        instance = null;
    }

    private DatabaseHolder(Context ctx) {
        mMyOpenHelper = new MyOpenHelper(ctx);
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

    public boolean putChatItem(ChatItem chatItem) {
        if (chatItem instanceof ConsultConnectionMessage) {
            mMyOpenHelper.putConsultConnected((ConsultConnectionMessage) chatItem);
            return true;
        }
        if (chatItem instanceof ChatPhrase) {
            mMyOpenHelper.putChatPhrase((ChatPhrase) chatItem);
            return true;
        }
        if (chatItem instanceof Survey) {
            mMyOpenHelper.insertOrUpdateSurvey((Survey) chatItem);
        }
        return false;
    }

    public void setStateOfUserPhraseByProviderId(String providerId, MessageState messageState) {
        mMyOpenHelper.setUserPhraseStateByProviderId(providerId, messageState);
    }

    public int getMessagesCount() {
        return mMyOpenHelper.getMessagesCount();
    }

    public void cleanDatabase() {
        mMyOpenHelper.cleanFD();
        mMyOpenHelper.cleanMessagesTable();
        mMyOpenHelper.cleanQuotes();
    }

    public void getFilesAsync(final CompletionHandler<List<FileDescription>> handler) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                handler.onComplete(mMyOpenHelper.getFd());
            }
        });
    }

    public void updateFileDescription(FileDescription fileDescription) {
        if (fileDescription == null) return;
        mMyOpenHelper.updateFd(fileDescription);
    }

    public void putMessagesSync(final List<ChatItem> items) {
        try {
            mMyOpenHelper.getWritableDatabase().beginTransaction();
            for (ChatItem item : items) {
                if (item instanceof ChatPhrase) {
                    mMyOpenHelper.putChatPhrase((ChatPhrase) item);
                }
                if (item instanceof ConsultConnectionMessage) {
                    mMyOpenHelper.putConsultConnected((ConsultConnectionMessage) item);
                }
                if (item instanceof Survey) {
                    mMyOpenHelper.insertOrUpdateSurvey((Survey) item);
                }
            }
            mMyOpenHelper.getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            ThreadsLogger.e(TAG, "putMessagesSync", e);
        } finally {
            mMyOpenHelper.getWritableDatabase().endTransaction();
        }
    }

    public void setAllMessagesRead(final CompletionHandler<Void> handler) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                mMyOpenHelper.setAllRead();
                handler.onComplete(null);
            }
        });
    }

    public void getLastUnreadPhrase(final CompletionHandler<ConsultPhrase> handler) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                ConsultPhrase cp = mMyOpenHelper.getLastUnreadPhrase();
                handler.onComplete(cp);
            }
        });
    }

    public List<String> getUnreadMessagesProviderIds() {
        return mMyOpenHelper.getUnreadMessagesProviderIds();
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

    private void getUnreadMessagesCount(@NonNull final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener) {
        unreadMessagesCountListener.onUnreadMessagesCountChanged(mMyOpenHelper.getUnreadMessagesProviderIds().size());
    }

    public void setMessageWereRead(String providerId) {
        mMyOpenHelper.setMessageWereRead(providerId);
    }

    public String getLastConsultAvatarPathSync(String id) {
        if (id == null) return null;
        return mMyOpenHelper.getLastOperatorAvatar(id);
    }

    public ConsultInfo getConsultInfoSync(@NonNull String id) {
        return mMyOpenHelper.getLastConsultInfo(id);
    }
}
