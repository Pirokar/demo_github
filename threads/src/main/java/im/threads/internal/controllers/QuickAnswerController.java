package im.threads.internal.controllers;

import android.app.Fragment;

import androidx.annotation.NonNull;

import im.threads.internal.activities.QuickAnswerActivity;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.utils.ThreadsLogger;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public final class QuickAnswerController extends Fragment {

    private static final String TAG = "QuickAnswerController ";
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static QuickAnswerController getInstance() {
        return new QuickAnswerController();
    }

    public void onBind(@NonNull final QuickAnswerActivity activity) {
        compositeDisposable.add(DatabaseHolder.getInstance().getLastConsultPhrase()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consultPhrase -> {
                    if (consultPhrase != null) {
                        activity.setLastUnreadMessage(consultPhrase);
                    }
                }, e -> {

                })
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    public void onUserAnswer(@NonNull UpcomingUserMessage upcomingUserMessage) {
        ThreadsLogger.i(TAG, "onUserAnswer");
        ChatController cc = ChatController.getInstance();
        cc.onUserInput(upcomingUserMessage);
        cc.setAllMessagesWereRead();
    }
}
