package im.threads.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import im.threads.controllers.QuickAnswerController;
import im.threads.fragments.QuickAnswerFragment;
import im.threads.model.ChatStyle;
import im.threads.model.ConsultPhrase;
import im.threads.model.UpcomingUserMessage;
import im.threads.utils.PrefUtils;

/**
 * Created by yuri on 03.09.2016.
 */
public class TranslucentActivity
        extends AppCompatActivity {
    private static final String TAG = "TranslucentActivity ";
    public static final String ACTION_ANSWER = "im.threads.ACTION_ANSWER";
    public static final String ACTION_CANCEL = "im.threads.ACTION_CANCEL";
    private QuickAnswerReceiver mQuickAnswerReceiver;
    private QuickAnswerController controller;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatStyle style = PrefUtils.getIncomingStyle(this);
        if (style!=null && style.chatStatusBarColorResId != ChatStyle.INVALID && Build.VERSION.SDK_INT > 20) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(style.chatStatusBarColorResId));
        }
        mQuickAnswerReceiver = new QuickAnswerReceiver();
        controller = QuickAnswerController.getInstance();

    }

    @Override
    protected void onResume() {
        super.onResume();
        controller.onBind(this);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ANSWER);
        filter.addAction(ACTION_CANCEL);
        manager.registerReceiver(mQuickAnswerReceiver, filter);
    }

    public void setLastUnreadMessage(ConsultPhrase phrase) {
        if (null != phrase) {
            QuickAnswerFragment fr =
                    QuickAnswerFragment.getInstance(phrase.getAvatarPath(),
                            phrase.getConsultName(),
                            phrase.getPhrase());
            fr.show(getFragmentManager(), null);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        controller.unBind();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mQuickAnswerReceiver);
    }

    private class QuickAnswerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent);
            if (intent.getAction().equalsIgnoreCase(ACTION_CANCEL)) {
                finish();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_ANSWER)) {
                Log.i(TAG, "onReceive: ACTION_ANSWER");
                controller.onUserAnswer(new UpcomingUserMessage(null, null, intent.getStringExtra(ACTION_ANSWER), false));
                finish();
            }
        }
    }
}


