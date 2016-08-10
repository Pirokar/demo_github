package com.sequenia.threads.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.pushserver.android.PushBroadcastReceiver;
import com.pushserver.android.PushController;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.utils.MessageMatcher;
import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.utils.PrefUtils;

import java.util.UUID;

/**
 * Created by yuri on 22.06.2016.
 */
public class MyPBReceiver extends PushBroadcastReceiver {
    private static final String TAG = "MyPBReceiver ";

    @Override
    public void onNewPushNotification(Context context, String s, Bundle bundle) {
        Log.e(TAG, "onNewPushNotification " + s + " " + bundle);
        if (MessageMatcher.getType(bundle) == MessageMatcher.TYPE_MESSAGE) return;
        ChatController.getInstance(context, PrefUtils.getClientID(context)).onSystemMessageFromServer(context, bundle);
    }

    @Override
    public void onStatusChanged(Context context, String s) {
        Log.e(TAG, "onStatusChanged " + s);
    }

    @Override
    public void onDeviceAddressChanged(final Context context, String s) {
        Log.d(TAG, "onDeviceAddressChanged " + s);
        Log.e(TAG, "PrefUtils.getClientID(context) = " + PrefUtils.getClientID(context));// TODO: 10.08.2016
        Log.e(TAG, "PrefUtils.isClientIdSet(context) = " + PrefUtils.isClientIdSet(context));// TODO: 10.08.2016
        if (!PrefUtils.isClientIdSet(context) && PrefUtils.getClientID(context) != null) {
            PushController.getInstance(context).setClientIdAsync(PrefUtils.getClientID(context), new RequestCallback<Void, PushServerErrorException>() {
                @Override
                public void onResult(Void aVoid) {
                    Log.d(TAG, "client id was set");
                    PrefUtils.setClientIdWasSet(true, context);
                    context.sendBroadcast(new Intent(ChatController.CLIENT_ID_IS_SET_BROADCAST));
                }

                @Override
                public void onError(PushServerErrorException e) {
                    Log.e(TAG, "" + e);
                }
            });
        }
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
