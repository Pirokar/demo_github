package com.sequenia.threads.controllers;

import android.content.Context;
import android.util.Log;

import com.pushserver.android.PushController;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.formatters.MessageFormatter;
import com.sequenia.threads.utils.Callback;
import com.sequenia.threads.utils.PrefUtils;

/**
 * Created by yuri on 19.09.2016.
 */
@Deprecated
public class PushIniter {
    private static final String TAG = "PushIniter ";
    private Context context;
    private String clientId;

    PushIniter(Context context, String clientId) {
        this.context = context;
        this.clientId = clientId;
    }

    void initIfNotInited(final Callback<Void, Exception> handler) {
        try {
            PrefUtils.setNewClientId(context, clientId);
            Log.i(TAG, "initIfNotInited: PrefUtils.getClientID(context) = " + PrefUtils.getClientID(context) +
                    "\nclientId = " + clientId
                    + "\nPrefUtils.isClientIdSet = " + PrefUtils.isClientIdSet(context));
            if (!PrefUtils.isClientIdSet(context)
                    || !PrefUtils.getClientID(context).equals(clientId)) {
                Log.i(TAG, "setting client id async");
                PushController
                        .getInstance(context)
                        .setClientIdAsync(clientId, new RequestCallback<Void, PushServerErrorException>() {
                            @Override
                            public void onResult(Void aVoid) {
                                PushController
                                        .getInstance(context)
                                        .sendMessageAsync(MessageFormatter
                                                .createClientAboutMessage(PrefUtils.getUserName(context), clientId, ""), true, new RequestCallback<String, PushServerErrorException>() {
                                            @Override
                                            public void onResult(String string) {
                                                Log.e(TAG, "client id was set string =" + string);
                                                PrefUtils.setClientId(context, clientId);
                                                PrefUtils.setClientIdWasSet(true, context);
                                                handler.onSuccess(null);
                                            }

                                            @Override
                                            public void onError(PushServerErrorException e) {
                                                handler.onFail(e);
                                            }
                                        });
                            }

                            @Override
                            public void onError(PushServerErrorException e) {
                                Log.e(TAG, "error while setting client id" + e);
                                handler.onFail(e);
                                e.printStackTrace();
                            }
                        });
            }
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                handler.onFail(e);
            }
            e.printStackTrace();
        }
    }

    public static void onAddressChanged(Context ctx) {
        PushController.getInstance(ctx)
                .sendMessageAsync(MessageFormatter
                        .createClientAboutMessage(PrefUtils.getUserName(ctx)
                                , PrefUtils.getNewClientID(ctx)// TODO: 05.10.2016 implement new client name and email
                                , ""), true, new RequestCallback<String, PushServerErrorException>() {
                    @Override
                    public void onResult(String s) {
                        Log.i(TAG, "onResult: "+s);
                    }

                    @Override
                    public void onError(PushServerErrorException e) {
                        Log.i(TAG, "onError: "+e);
                    }
                });
    }
}
