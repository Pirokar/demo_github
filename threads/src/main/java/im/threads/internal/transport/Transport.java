package im.threads.internal.transport;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import java.util.List;

import im.threads.ConfigBuilder;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.database.DatabaseHolder;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ClientNotificationDisplayType;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.SettingsResponse;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.retrofit.ApiGenerator;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class Transport {

    private static final String TAG = Transport.class.getSimpleName();
    private CompositeDisposable compositeDisposable;
    private final ChatUpdateProcessor chatUpdateProcessor = ChatUpdateProcessor.getInstance();

    public void markMessagesAsRead(List<String> uuidList) {
        ThreadsLogger.i(TAG, "markMessagesAsRead : " + uuidList);
        subscribe(
                Completable.fromAction(() -> ApiGenerator.getThreadsApi().markMessageAsRead(uuidList).execute())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                    ThreadsLogger.i(TAG, "messagesAreRead : " + uuidList);
                                    for(String messageId: uuidList) {
                                        ChatItem chatItem = DatabaseHolder.getInstance().getChatItem(messageId);
                                        if (chatItem instanceof ConsultPhrase) {
                                            chatUpdateProcessor.postConsultMessageWasRead(((ConsultPhrase) chatItem).getProviderId());
                                        }
                                    }
                                },
                                e -> {
                                    ThreadsLogger.i(TAG, "error on messages read : " + uuidList);
                                    chatUpdateProcessor.postError(new TransportException(e.getMessage()));
                                }
                        )

        );
    }

    public void getSettings() {
        subscribe(
            Single.fromCallable(() -> ApiGenerator.getThreadsApi().settings().execute())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            response -> {
                                final SettingsResponse responseBody = response.body();
                                if (responseBody != null) {
                                    ThreadsLogger.i(TAG, "getting settings : " + responseBody);
                                    final ClientNotificationDisplayType type = ClientNotificationDisplayType.fromString(responseBody.getClientNotificationDisplayType());
                                    PrefUtils.setClientNotificationDisplayType(type);
                                    chatUpdateProcessor.postClientNotificationDisplayType(type);
                                }
                            },
                            e -> {
                                ThreadsLogger.i(TAG, "error on getting settings : " + e.getMessage());
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
