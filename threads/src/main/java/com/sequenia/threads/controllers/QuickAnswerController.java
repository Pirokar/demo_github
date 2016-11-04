package com.sequenia.threads.controllers;

import android.app.Fragment;
import android.content.Context;
import android.util.Log;

import com.sequenia.threads.activities.TranslucentActivity;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.utils.PrefUtils;

/**
 * Created by yuri on 19.09.2016.
 */
public class QuickAnswerController extends Fragment {
    private static final String TAG = "QuickAnswerController ";
    private TranslucentActivity activity;
    private Context context;

    public static QuickAnswerController getInstance() {
        return new QuickAnswerController();
    }

    public void onBind(final TranslucentActivity activity) {
        if (activity == null) return;
        this.activity = activity;
        context = activity.getApplicationContext();
        ChatController
                .getInstance(activity, PrefUtils.getClientID(activity))
                .getLastUnreadConsultPhrase(new CompletionHandler<ConsultPhrase>() {
                    @Override
                    public void onComplete(ConsultPhrase data) {
                        if (null != data) {
                            activity.setLastUnreadMessage(data);
                        }
                    }

                    @Override
                    public void onError(Throwable e, String message, ConsultPhrase data) {
                    }
                });
    }

    public void unBind() {
        this.activity = null;
    }

    public void onUserAnswer(UpcomingUserMessage upcomingUserMessage) {
        Log.i(TAG, "onUserAnswer");
        if (activity == null && context == null) {
            Log.e(TAG, "onUserAnswer context is null");
            return;
        }
        final Context ctx = activity == null ? context : activity;
        ChatController cc = ChatController
                .getInstance(ctx, PrefUtils.getClientID(ctx));
        cc.onUserInput(upcomingUserMessage);
        cc.setAllMessagesWereRead();
    }
}
