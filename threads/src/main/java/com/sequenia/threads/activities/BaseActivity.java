package com.sequenia.threads.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sequenia.threads.AnalyticsTracker;
import com.sequenia.threads.utils.PrefUtils;
import com.sequenia.threads.utils.Tuple;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by yuri on 27.09.2016.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity ";
    private static boolean isInForeground = false;
    private boolean hasFocus = false;
    private static ArrayList<Tuple<BaseActivity, Boolean>> activities = new ArrayList<>();
    private static Handler h;
    private static AnalyticsTracker tracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activities.add(new Tuple<>(this, false));
        if (h == null) h = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (Tuple<BaseActivity, Boolean> tuple : activities) {
            if (tuple.first == this) tuple.second = true;
        }
        h.removeCallbacksAndMessages(null);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isInForeground) {
                    isInForeground = true;
                    onAppForeground();
                }
            }
        }, 1000);
    }


    @Override
    protected void onPause() {
        super.onPause();
        for (Tuple<BaseActivity, Boolean> tuple : activities) {
            if (tuple.first == this) tuple.second = false;
        }
        h.removeCallbacksAndMessages(null);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                int counter = 0;
                for (Tuple<BaseActivity, Boolean> tuple : activities) {
                    if (!tuple.second) counter++;
                }

                if (counter == activities.size() && !hasFocus) {
                    isInForeground = false;
                    onAppBackground();
                }
            }
        }, 1000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Iterator<Tuple<BaseActivity, Boolean>> iter = activities.iterator();
             iter.hasNext(); ) {
            Tuple<BaseActivity, Boolean> tuple = iter.next();
            if (this == tuple.first) iter.remove();
        }
    }

    private void onAppBackground() {
        if (tracker==null)tracker = AnalyticsTracker.getInstance(this,PrefUtils.getGaTrackerId(this));
        tracker.setUserLeftChat();
    }

    private void onAppForeground() {
        if (tracker==null)tracker = AnalyticsTracker.getInstance(this,PrefUtils.getGaTrackerId(this));
        tracker.chatWasOpened(PrefUtils.getClientID(this));
        tracker.setUserEnteredChat();
    }
}
