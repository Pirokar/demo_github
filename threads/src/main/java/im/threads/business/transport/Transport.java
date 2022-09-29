package im.threads.business.transport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import java.util.List;

import im.threads.business.chat_updates.ChatUpdateProcessor;
import im.threads.business.logger.core.LoggerEdna;
import im.threads.business.models.ConsultInfo;
import im.threads.business.models.Survey;
import im.threads.business.models.UserPhrase;
import im.threads.business.rest.queries.BackendApi;
import im.threads.business.rest.queries.ThreadsApi;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class Transport {
    private final ChatUpdateProcessor chatUpdateProcessor = ChatUpdateProcessor.getInstance();
    private CompositeDisposable compositeDisposable;

    public void markMessagesAsRead(List<String> uuidList) {
        LoggerEdna.info(ThreadsApi.REST_TAG, "markMessagesAsRead : " + uuidList);
        subscribe(
                Completable.fromAction(() -> BackendApi.get().markMessageAsRead(uuidList).execute())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                    LoggerEdna.info(
                                            ThreadsApi.REST_TAG, "messagesAreRead : " + uuidList
                                    );
                                    for (String messageId : uuidList) {
                                        chatUpdateProcessor.postIncomingMessageWasRead(messageId);
                                    }
                                },
                                e -> {
                                    LoggerEdna.info(
                                            ThreadsApi.REST_TAG, "error on messages read : " + uuidList
                                    );
                                    chatUpdateProcessor.postError(new TransportException(e.getMessage()));
                                }
                        )

        );
    }

    private boolean subscribe(final Disposable event) {
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        return compositeDisposable.add(event);
    }


    public abstract void init();

    public abstract void sendRatingDone(Survey survey);

    public abstract void sendResolveThread(boolean approveResolve);

    public abstract void sendUserTying(String input);

    public abstract void sendInit();

    /**
     * TODO THREADS-6292: this method can potentially lead to messages stuck in STATE_SENDING
     */
    public abstract void sendMessage(UserPhrase userPhrase, ConsultInfo consultInfo, final String filePath, final String quoteFilePath);

    public abstract void sendClientOffline(String clientId);

    public abstract void updateLocation(Double latitude, Double longitude);

    @NonNull
    public abstract String getToken() throws TransportException;

    public abstract void setLifecycle(@Nullable Lifecycle lifecycle);
}
