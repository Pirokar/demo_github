package im.threads.ui.activities;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import im.threads.ui.ChatStyle;
import im.threads.business.logger.LoggerEdna;
import im.threads.business.models.ConsultPhrase;
import im.threads.business.models.UpcomingUserMessage;
import im.threads.ui.config.Config;
import im.threads.ui.controllers.QuickAnswerController;
import im.threads.ui.fragments.QuickAnswerFragment;

public final class QuickAnswerActivity extends BaseActivity {
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
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );
    }

    public void setLastUnreadMessage(ConsultPhrase phrase) {
        if (null != phrase) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(QuickAnswerFragment.TAG);
            if(fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
            QuickAnswerFragment.getInstance(phrase.getAvatarPath(),
                    phrase.getConsultName(),
                    phrase.getPhraseText()).show(getSupportFragmentManager(), QuickAnswerFragment.TAG);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatStyle style = Config.getInstance().getChatStyle();
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(style.chatStatusBarColorResId));
        if (getResources().getBoolean(style.windowLightStatusBarResId)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mQuickAnswerReceiver);
    }

    private class QuickAnswerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LoggerEdna.debug("onReceive: " + intent);
            if (intent.getAction().equalsIgnoreCase(ACTION_CANCEL)) {
                finish();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_ANSWER)) {
                LoggerEdna.info("onReceive: ACTION_ANSWER");
                controller.onUserAnswer(new UpcomingUserMessage(null, null, null, intent.getStringExtra(ACTION_ANSWER), false));
                finish();
            }
        }
    }
}


