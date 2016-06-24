package com.sequenia.threads.database;

import android.content.Context;

import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.ConsultConnected;
import com.sequenia.threads.model.DatabaseResponse;
import com.sequenia.threads.model.MessageState;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yuri on 23.06.2016.
 */
public class DatabaseHolder {
    private final MyOpenHelper database;
    private static DatabaseHolder instance;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public static DatabaseHolder getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHolder(context);
        }
        return instance;
    }

    private DatabaseHolder(Context ctx) {
        database = new MyOpenHelper(ctx);
    }

    public List<ChatItem> getChatItems(int offset, int limit) {
        return database.getChatItems(offset, limit);
    }

    public void getChatItemsAsync(final int offset, final int limit, final DatabaseResponse<List<ChatItem>> completionHandler) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                List<ChatItem> items = database.getChatItems(offset, limit);
                if (items==null||items.size()==0){
                    completionHandler.setSuccessful(false);
                    completionHandler.setMessage("nothing here");
                }else {
                    completionHandler.onComplete(items);
                }
            }
        });
    }

    public boolean putChatItem(ChatItem chatItem) {
        if (chatItem instanceof ConsultConnected) {
            database.putConsultConnected((ConsultConnected) chatItem);
            return true;
        }
        if (chatItem instanceof ChatPhrase) {
            database.putUserPharse((ChatPhrase) chatItem);
            return true;
        }
        return false;
    }

    public void setStateOfUserPhrase(long messageId, MessageState messageState) {
        database.setUserPhraseState(messageId, messageState);
    }


}
