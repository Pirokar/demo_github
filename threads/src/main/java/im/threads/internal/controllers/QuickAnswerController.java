package im.threads.internal.controllers;

import android.app.Fragment;

import androidx.annotation.NonNull;

import im.threads.internal.activities.QuickAnswerActivity;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.utils.ThreadsLogger;

public final class QuickAnswerController extends Fragment {
    private static final String TAG = "QuickAnswerController ";

    public static QuickAnswerController getInstance() {
        return new QuickAnswerController();
    }

    public void onBind(@NonNull final QuickAnswerActivity activity) {
        DatabaseHolder.getInstance()
                .getLastConsultPhrase(consultPhrase -> {
                    if (consultPhrase != null) {
                        activity.setLastUnreadMessage(consultPhrase);
                    }
                });
    }

    public void onUserAnswer(@NonNull UpcomingUserMessage upcomingUserMessage) {
        ThreadsLogger.i(TAG, "onUserAnswer");
        ChatController cc = ChatController.getInstance();
        cc.onUserInput(upcomingUserMessage);
        cc.setAllMessagesWereRead();
    }
}
