package im.threads.internal.controllers;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;

import im.threads.internal.activities.TranslucentActivity;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.model.CompletionHandler;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.utils.ThreadsLogger;

public final class QuickAnswerController extends Fragment {
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
        DatabaseHolder.getInstance()
                .getLastUnreadPhrase(new CompletionHandler<ConsultPhrase>() {
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

    public void onUserAnswer(@NonNull UpcomingUserMessage upcomingUserMessage) {
        ThreadsLogger.i(TAG, "onUserAnswer");
        if (activity == null && context == null) {
            ThreadsLogger.e(TAG, "onUserAnswer context is null");
            return;
        }
        final Context ctx = activity == null ? context : activity;
        ChatController cc = ChatController.getInstance(ctx);
        cc.onUserInput(upcomingUserMessage);
        cc.setAllMessagesWereRead();
    }
}
