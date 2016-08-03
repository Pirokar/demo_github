package com.sequenia.threads.utils;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by yuri on 01.08.2016.
 */
public class DownloadService extends IntentService {
    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
