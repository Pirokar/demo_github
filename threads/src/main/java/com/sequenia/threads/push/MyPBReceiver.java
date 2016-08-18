package com.sequenia.threads.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.advisa.client.api.InOutMessage;
import com.pushserver.android.PushBroadcastReceiver;
import com.pushserver.android.PushController;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.utils.MessageFormatter;
import com.sequenia.threads.utils.MessageMatcher;
import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.utils.PrefUtils;

import java.util.List;
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
        if (!PrefUtils.isClientIdSet(context) && PrefUtils.getClientID(context) != null) {
            PushController.getInstance(context).setClientIdAsync(PrefUtils.getClientID(context), new RequestCallback<Void, PushServerErrorException>() {
                @Override
                public void onResult(Void aVoid) {
                    Log.d(TAG, "client id was set");
                    PushController.getInstance(context).sendMessageAsync(MessageFormatter.getStartMessage(PrefUtils.getClientName(context), PrefUtils.getClientID(context), ""), true, new RequestCallback<Void, PushServerErrorException>() {
                        @Override
                        public void onResult(Void aVoid) {
                            Log.e(TAG, "client id was set");// TODO: 09.08.2016
                            context.sendBroadcast(new Intent(ChatController.CLIENT_ID_IS_SET_BROADCAST));
                            PrefUtils.setClientIdWasSet(true, context);
                        }

                        @Override
                        public void onError(PushServerErrorException e) {

                        }
                    });
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
