package com.sequenia.threads.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.pushserver.android.PushMessage;
import com.pushserver.android.PushServerIntentService;
import com.sequenia.threads.R;
import com.sequenia.threads.activities.ChatActivity;
import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.utils.MessageFormatter;
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
        Log.e(TAG, "saveMessages");// TODO: 17.08.2016  
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
