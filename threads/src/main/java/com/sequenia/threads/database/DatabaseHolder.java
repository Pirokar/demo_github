package com.sequenia.threads.database;

import android.content.Context;

import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultConnected;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;

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
                if (items == null || items.size() == 0) {
                    completionHandler.setSuccessful(false);
                    completionHandler.setMessage("nothing here");
                } else {
                    completionHandler.onComplete(items);
                }
            }
        });
    }

    public boolean putChatItem(ChatItem chatItem) {
        if (chatItem instanceof ConsultConnected) {
            mMyOpenHelper.putConsultConnected((ConsultConnected) chatItem);
            return true;
        }
        if (chatItem instanceof ChatPhrase) {
            mMyOpenHelper.putUserPhrase((ChatPhrase) chatItem);
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
                handler.onComplete(mMyOpenHelper.getFileDescription());
            }
        });
    }

}
