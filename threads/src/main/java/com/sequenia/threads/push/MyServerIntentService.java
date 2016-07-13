package com.sequenia.threads.push;

import android.util.Log;

import com.pushserver.android.PushMessage;
import com.pushserver.android.PushServerIntentService;

import java.util.List;

/**
 * Created by yuri on 22.06.2016.
 */
public class MyServerIntentService extends PushServerIntentService {
    private static final String TAG = "MyServerIntentService ";
    @Override
    protected boolean saveMessages(List<PushMessage> list) {
        Log.e(TAG, ""+list);
        return true;
    }

    @Override
    protected void messagesWereRead(List<String> list) {
        Log.e(TAG, ""+list);
    }
}
