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
        Log.i(TAG, "onDeviceAddressChanged " + s);
        PushController.getInstance(context).sendMessageAsync(MessageFormatter
                .getStartMessage(PrefUtils.getUserName(context), PrefUtils.getClientID(context), ""), true, new RequestCallback<String, PushServerErrorException>() {
            @Override
            public void onResult(String s) {
                context.sendBroadcast(new Intent(ChatController.CLIENT_ID_IS_SET_BROADCAST));
                PrefUtils.setClientIdWasSet(true, context);
            }

            @Override
            public void onError(PushServerErrorException e) {

            }
        });
       /* new PushIniter(context,PrefUtils.getClientID(context)).initIfNotInited(new Callback<Void, Exception>() {
            @Override
            public void onSuccess(Void result) {
                PrefUtils.setClientIdWasSet(true, context);
            }

            @Override
            public void onFail(Exception error) {

            }*/
    }

       /* if (!PrefUtils.isClientIdSet(context) && PrefUtils.getClientID(context) != null) {
            PushController.getInstance(context).setClientIdAsync(PrefUtils.getClientID(context), new RequestCallback<Void, PushServerErrorException>() {
                @Override
                public void onResult(Void aVoid) {
                    Log.d(TAG, "client id was set");
                    PushController.getInstance(context).sendMessageAsync(MessageFormatter.getStartMessage(PrefUtils.getUserName(context), PrefUtils.getClientID(context), ""), true, new RequestCallback<String, PushServerErrorException>() {
                        @Override
                        public void onResult(String string) {
                            Log.e(TAG, "client id was set string = " + string);
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
        }*/


    @Override
    public void onDeviceAddressProblems(Context context, String s) {
        Log.e(TAG, "onDeviceAddressProblems " + s);
    }

    @Override
    public void onError(Context context, String s) {
        Log.e(TAG, "onError " + s);
    }
}
