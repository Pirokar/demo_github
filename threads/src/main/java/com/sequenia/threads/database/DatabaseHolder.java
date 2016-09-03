package com.sequenia.threads.database;

import android.content.Context;

import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yuri on 23.06.2016.
 */
public class DatabaseHolder {
    private final MyOpenHelper mMyOpenHelper;
    private static DatabaseHolder instance;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public static DatabaseHolder getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHolder(context);
        }
        return instance;
    }

    private DatabaseHolder(Context ctx) {
        mMyOpenHelper = new MyOpenHelper(ctx);
    }

    public List<ChatItem> getChatItems(int offset, int limit) {
        return mMyOpenHelper.getChatItems(offset, limit);
    }

    public void getChatItemsAsync(final int offset, final int limit, final CompletionHandler<List<ChatItem>> completionHandler) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                List<ChatItem> items = mMyOpenHelper.getChatItems(offset, limit);
                if (items == null) {
                    completionHandler.setSuccessful(true);
                    completionHandler.onComplete(new ArrayList<ChatItem>());
                } else {
                    completionHandler.onComplete(items);
                }
            }
        });
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
        return false;
    }

    public void setStateOfUserPhrase(String messageId, MessageState messageState) {
        mMyOpenHelper.setUserPhraseState(messageId, messageState);
    }

    public int getMessagesCount() {
        return mMyOpenHelper.getMessagesCount();
    }

    public void getMessagesCountAsync(final CompletionHandler<Integer> response) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                response.onComplete(mMyOpenHelper.getMessagesCount());
            }
        });
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

    public void queryChatPhrasesAsync(final String query, final CompletionHandler<List<ChatPhrase>> callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<ChatPhrase> list = mMyOpenHelper.getSortedPhrases(query);
                if (query == null)
                    callback.onError(new IllegalArgumentException(), "query is null", new ArrayList<ChatPhrase>());
                callback.onComplete(list);
            }
        });
    }

    public void queryFilesAsync(final String query, final CompletionHandler<List<ChatPhrase>> callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<ChatPhrase> list = mMyOpenHelper.queryFiles(query);
                if (query == null)
                    callback.onError(new IllegalArgumentException(), "query is null", new ArrayList<ChatPhrase>());
                callback.onComplete(list);
            }
        });
    }

    public void putMessagesAsync(final List<ChatItem> items, final CompletionHandler<Void> completionHandler) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                mMyOpenHelper.getWritableDatabase().beginTransaction();
                for (ChatItem item : items) {
                    if (item instanceof ChatPhrase) {
                        mMyOpenHelper.putChatPhrase((ChatPhrase) item);
                    }
                    if (item instanceof ConsultConnectionMessage) {
                        mMyOpenHelper.putConsultConnected((ConsultConnectionMessage) item);
                    }
                }
                mMyOpenHelper.getWritableDatabase().setTransactionSuccessful();
                mMyOpenHelper.getWritableDatabase().endTransaction();
                completionHandler.onComplete(null);
            }
        });

    }

    public void setUserPhraseMessageId(String oldId, String newId) {
        mMyOpenHelper.setUserPhraseMessageId(oldId, newId);
    }

    public void cleanDbAsync(final CompletionHandler<Void> onComplete) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                mMyOpenHelper.cleanDb();
                onComplete.onComplete(null);
            }
        });
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

    public void getChatItemByFileDescription(final FileDescription fileDescription, final CompletionHandler<ChatItem> handler) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                ChatPhrase cp = mMyOpenHelper.getChatphraseByDescription(fileDescription);
                handler.onComplete(cp);
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
}
