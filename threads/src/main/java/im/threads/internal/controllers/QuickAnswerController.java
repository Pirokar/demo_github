package im.threads.internal.controllers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import im.threads.internal.Config;
import im.threads.internal.activities.QuickAnswerActivity;
import im.threads.internal.domain.logger.LoggerEdna;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.secureDatabase.DatabaseHolder;
import im.threads.internal.transport.HistoryLoader;
import im.threads.internal.transport.HistoryParser;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class QuickAnswerController extends Fragment {
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static QuickAnswerController getInstance() {
        return new QuickAnswerController();
    }

    public void onBind(@NonNull final QuickAnswerActivity activity) {
        ChatController.getInstance().loadHistory();
        compositeDisposable.add(
                Single.fromCallable(() -> HistoryParser.getChatItems(HistoryLoader.getHistorySync(100, true)))
                        .doOnSuccess(chatItems -> {
                            DatabaseHolder.getInstance().putChatItems(chatItems);
                            final List<String> uuidList = DatabaseHolder.getInstance().getUnreadMessagesUuid();
                            if (!uuidList.isEmpty()) {
                                Config.instance.transport.markMessagesAsRead(uuidList);
                            }
                        })
                        .flatMap(items -> DatabaseHolder.getInstance().getLastConsultPhrase())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                consultPhrase -> {
                                    if (consultPhrase != null) {
                                        activity.setLastUnreadMessage(consultPhrase);
                                    }
                                },
                                e -> LoggerEdna.error("onBind", e)
                        )
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
        LoggerEdna.info("onUserAnswer");
        ChatController cc = ChatController.getInstance();
        cc.onUserInput(upcomingUserMessage);
        cc.setAllMessagesWereRead();
    }
}
