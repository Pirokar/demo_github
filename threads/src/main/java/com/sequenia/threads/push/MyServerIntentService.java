package com.sequenia.threads.push;

import android.util.Log;

import com.advisa.client.api.InOutMessage;
import com.pushserver.android.PushController;
import com.pushserver.android.PushMessage;
import com.pushserver.android.PushServerIntentService;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.utils.PrefUtils;

import org.json.JSONException;

import java.util.List;

/**
 * Created by yuri on 22.06.2016.
 */
public class MyServerIntentService extends PushServerIntentService {
    private static final String TAG = "MyServerIntentService ";

    @Override
    protected boolean saveMessages(List<PushMessage> list) {
        if (list == null) return false;
        try {
            for (int i = 0; i < list.size(); i++) {
                ChatController.getInstance(getApplication(), PrefUtils.getClientID(getApplication())).onConsultMessage(list.get(i),getApplication());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "error while parsing server answer");
        }
        PushController.getInstance(getApplicationContext()).getNextMessageHistoryAsync(10, new RequestCallback<List<InOutMessage>, PushServerErrorException>() {
            @Override
            public void onResult(List<InOutMessage> inOutMessages) {
                Log.e(TAG, "history =" + inOutMessages);
            }

            @Override
            public void onError(PushServerErrorException e) {

            }
        });
        return true;
    }

    @Override
    protected void messagesWereRead(List<String> list) {
        Log.e(TAG, "" + list);
    }
}
