package im.threads.ui.controllers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import im.threads.business.config.BaseConfig;
import im.threads.business.logger.LoggerEdna;
import im.threads.business.models.UpcomingUserMessage;
import im.threads.business.secureDatabase.DatabaseHolder;
import im.threads.business.transport.HistoryLoader;
import im.threads.business.transport.HistoryParser;
import im.threads.ui.activities.QuickAnswerActivity;
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
        LoggerEdna.info("onBind in " + QuickAnswerController.class.getSimpleName());
        ChatController.getInstance().loadHistory();
        compositeDisposable.add(
                Single.fromCallable(() -> HistoryParser.getChatItems(
                            HistoryLoader.INSTANCE.getHistorySync(100, true)
                        ))
                        .doOnSuccess(chatItems -> {
                            DatabaseHolder.getInstance().putChatItems(chatItems);
                            final List<String> uuidList = DatabaseHolder.getInstance().getUnreadMessagesUuid();
                            if (!uuidList.isEmpty()) {
                                BaseConfig.instance.transport.markMessagesAsRead(uuidList);
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
