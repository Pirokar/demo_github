package com.sequenia.threads.push;

import android.os.Bundle;
import android.util.Log;

import com.pushserver.android.PushGcmIntentService;
import com.sequenia.threads.controllers.ChatController;

/**
 * Created by yuri on 14.07.2016.
 */
public class MyPGCMService extends PushGcmIntentService {
    private static final String THIS_TAG = " MyPGCMService ";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        Log.e(THIS_TAG, "data = " + data);
        ChatController.getInstance().onMessageFromServer(null, data);
    }
}
