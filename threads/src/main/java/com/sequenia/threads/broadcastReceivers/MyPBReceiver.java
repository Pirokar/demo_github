package com.sequenia.threads.broadcastReceivers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.pushserver.android.PushBroadcastReceiver;
import com.pushserver.android.PushController;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.controllers.PushIniter;
import com.sequenia.threads.formatters.MessageFormatter;
import com.sequenia.threads.utils.Callback;
import com.sequenia.threads.utils.MessageMatcher;
import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.utils.PrefUtils;

/**
 * Created by yuri on 22.06.2016.
 */
public class MyPBReceiver extends PushBroadcastReceiver {
    private static final String TAG = "MyPBReceiver ";

    @Override
    public void onNewPushNotification(Context context, String s, Bundle bundle) {
        Log.d(TAG, "onNewPushNotification " + s + " " + bundle);
        if (MessageMatcher.getType(bundle) == MessageMatcher.TYPE_OPERATOR_TYPING
                || MessageMatcher.getType(bundle) == MessageMatcher.TYPE_MESSAGES_READ)
            ChatController.getInstance(context, PrefUtils.getClientID(context)).onSystemMessageFromServer(context, bundle);
    }

    @Override
    public void onStatusChanged(Context context, String s) {
        Log.e(TAG, "onStatusChanged " + s);
    }

    @Override
    public void onDeviceAddressChanged(final Context context, String s) {
        Log.e(TAG, "onDeviceAddressChanged " + s);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (PrefUtils.getNewClientID(context)==null)return;
                    PushController.getInstance(context).setClientId(PrefUtils.getNewClientID(context));
                    PushController.getInstance(context).sendMessage(MessageFormatter.getStartMessage(PrefUtils.getUserName(context),PrefUtils.getNewClientID(context),""),true);
                    context.sendBroadcast(new Intent(ChatController.CLIENT_ID_IS_SET_BROADCAST));
                } catch (PushServerErrorException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
