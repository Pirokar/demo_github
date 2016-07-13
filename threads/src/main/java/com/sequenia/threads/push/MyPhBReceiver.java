package com.sequenia.threads.push;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.pushserver.android.PushBroadcastReceiver;
import com.pushserver.android.PushController;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.controllers.ChatController;

import java.util.UUID;

/**
 * Created by yuri on 22.06.2016.
 */
public class MyPhBReceiver extends PushBroadcastReceiver {
    private static final String TAG = "MyPhBReceiver ";

    @Override
    public void onNewPushNotification(Context context, String s, Bundle bundle) {
        Log.e(TAG, "onNewPushNotification " + s + " " + bundle);
        ChatController.getInstance().onConsultInput(s);
    }

    @Override
    public void onStatusChanged(Context context, String s) {
        Log.e(TAG, "onStatusChanged " + s);
    }

    @Override
    public void onDeviceAddressChanged(Context context, String s) {
        Log.e(TAG, "onDeviceAddressChanged " + s);
        String id = PreferenceManager.getDefaultSharedPreferences(context).getString("Id", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("Id", id).apply();
        }
        PushController.getInstance(context).setClientIdAsync(id, new RequestCallback<Void, PushServerErrorException>() {
            @Override
            public void onResult(Void aVoid) {
                Log.e(TAG, "" + aVoid);
            }

            @Override
            public void onError(PushServerErrorException e) {
                Log.e(TAG, "" + e);
            }
        });
    }

    @Override
    public void onDeviceAddressProblems(Context context, String s) {
        Log.e(TAG, "onDeviceAddressProblems " + s);
    }

    @Override
    public void onError(Context context, String s) {
        Log.e(TAG, "onError " + s);
    }
}
