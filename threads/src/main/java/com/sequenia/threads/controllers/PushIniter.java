package com.sequenia.threads.controllers;

import android.content.Context;
import android.util.Log;

import com.advisa.client.api.InOutMessage;
import com.pushserver.android.PushController;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.formatters.MessageFormatter;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.utils.Callback;
import com.sequenia.threads.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuri on 19.09.2016.
 */
public class PushIniter {
    private static final String TAG = "PushIniter ";
    private Context context;
    private String clientId;

    public PushIniter(Context context, String clientId) {
        this.context = context;
        this.clientId = clientId;
    }

    public void initIfNotInited(final Callback<Void, Exception> handler) {
        try {
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
                                                .getStartMessage(PrefUtils.getUserName(context), PrefUtils.getClientID(context), ""), true, new RequestCallback<String, PushServerErrorException>() {
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
}
