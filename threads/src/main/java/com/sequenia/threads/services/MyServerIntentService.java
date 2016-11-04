package com.sequenia.threads.services;

import android.content.Intent;
import android.util.Log;

import com.pushserver.android.PushMessage;
import com.pushserver.android.PushServerIntentService;
import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.utils.PrefUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuri on 22.06.2016.
 */
public class MyServerIntentService extends PushServerIntentService {
    private static final String TAG = "MyServerIntentService ";


    @Override
    protected boolean saveMessages(List<PushMessage> list) {
        Log.i(TAG, "saveMessages " + list);
        if (list == null) return false;
        try {
            for (int i = 0; i < list.size(); i++) {
                ChatController.getInstance(getApplication(), PrefUtils.getClientID(getApplication())).onConsultMessage(list.get(i), getApplication());
            }
            Intent intent = new Intent(getApplicationContext(), NotificationService.class);
            ArrayList<PushMessage> al = new ArrayList<>(list);
            intent.putParcelableArrayListExtra(NotificationService.ACTION_ADD_UNREAD_MESSAGE, al);
            intent.setAction(NotificationService.ACTION_ADD_UNREAD_MESSAGE);
            startService(intent);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "error while parsing server answer");
        }
        return true;
    }

    @Override
    protected void messagesWereRead(List<String> list) {
        Log.e(TAG, "messagesWereRead " + list);
    }
}
