package im.threads.internal.activities;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import im.threads.ChatStyle;
import im.threads.internal.Config;
import im.threads.internal.controllers.QuickAnswerController;
import im.threads.internal.fragments.QuickAnswerFragment;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.utils.ThreadsLogger;

public final class QuickAnswerActivity
        extends AppCompatActivity {
    private static final String TAG = "QuickAnswerActivity ";
    public static final String ACTION_ANSWER = "im.threads.ACTION_ANSWER";
    public static final String ACTION_CANCEL = "im.threads.ACTION_CANCEL";
    private QuickAnswerReceiver mQuickAnswerReceiver;
    private QuickAnswerController controller;

    public static PendingIntent createPendingIntent(Context context) {
        final Intent buttonIntent = new Intent(context, QuickAnswerActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return PendingIntent.getActivity(
                context,
                1,
                buttonIntent,
                0
        );
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatStyle style = Config.instance.getChatStyle();
        if (Build.VERSION.SDK_INT > 20) {
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


    @Override
    protected void onStop() {
        super.onStop();
        controller.unBind();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mQuickAnswerReceiver);
    }

    private class QuickAnswerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ThreadsLogger.d(TAG, "onReceive: " + intent);
            if (intent.getAction().equalsIgnoreCase(ACTION_CANCEL)) {
                finish();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_ANSWER)) {
                ThreadsLogger.i(TAG, "onReceive: ACTION_ANSWER");
                controller.onUserAnswer(new UpcomingUserMessage(null, null, intent.getStringExtra(ACTION_ANSWER), false));
                finish();
            }
        }
    }
}


