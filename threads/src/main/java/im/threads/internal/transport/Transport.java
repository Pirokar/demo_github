package im.threads.internal.transport;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import java.util.List;

import im.threads.ConfigBuilder;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.retrofit.ApiGenerator;
import im.threads.internal.utils.ThreadsLogger;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class Transport {

    private static final String TAG = Transport.class.getSimpleName();
    private CompositeDisposable compositeDisposable;
    private final ChatUpdateProcessor chatUpdateProcessor = ChatUpdateProcessor.getInstance();

    public void markMessagesAsRead(List<String> messageIds) {
        ThreadsLogger.i(TAG, "markMessagesAsRead : " + messageIds);
        subscribe(
                Completable.fromAction(() -> ApiGenerator.getThreadsApi().markMessageAsRead(messageIds).execute())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                    ThreadsLogger.i(TAG, "messagesAreRead : " + messageIds);
                                    for(String messageId: messageIds) {
                                        chatUpdateProcessor.postConsultMessageWasRead(messageId);
                                    }
                                },
                                e -> {
                                    ThreadsLogger.i(TAG, "error on messages read : " + messageIds);
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

    public abstract void sendInitChatMessage();

    public abstract void sendEnvironmentMessage();

    /**
     * TODO THREADS-6292: this method can potentially lead to messages stuck in STATE_SENDING
     */
    public abstract void sendMessage(UserPhrase userPhrase, ConsultInfo consultInfo, final String filePath, final String quoteFilePath);

    public abstract void sendRatingReceived(long sendingId);

    public abstract void sendClientOffline(String clientId);

    public abstract ConfigBuilder.TransportType getType();

    @NonNull
    public abstract String getToken() throws TransportException;

    public abstract void setLifecycle(Lifecycle lifecycle);
}
