package im.threads.internal.transport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import java.util.List;

import im.threads.business.rest.models.SettingsResponse;
import im.threads.business.rest.queries.BackendApi;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.domain.logger.LoggerEdna;
import im.threads.internal.model.ClientNotificationDisplayType;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.utils.PrefUtils;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class Transport {
    private final ChatUpdateProcessor chatUpdateProcessor = ChatUpdateProcessor.getInstance();
    private CompositeDisposable compositeDisposable;

    public void markMessagesAsRead(List<String> uuidList) {
        LoggerEdna.info("markMessagesAsRead : " + uuidList);
        subscribe(
                Completable.fromAction(() -> BackendApi.get().markMessageAsRead(uuidList).execute())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                    LoggerEdna.info("messagesAreRead : " + uuidList);
                                    for (String messageId : uuidList) {
                                        chatUpdateProcessor.postIncomingMessageWasRead(messageId);
                                    }
                                },
                                e -> {
                                    LoggerEdna.info("error on messages read : " + uuidList);
                                    chatUpdateProcessor.postError(new TransportException(e.getMessage()));
                                }
                        )

        );
    }

    public void getSettings() {
        subscribe(
                Single.fromCallable(() -> BackendApi.get().settings().execute())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    final SettingsResponse responseBody = response.body();
                                    if (responseBody != null) {
                                        LoggerEdna.info("getting settings : " + responseBody);
                                        String clientNotificationType = responseBody.getClientNotificationDisplayType();
                                        if (clientNotificationType != null && !clientNotificationType.isEmpty()) {
                                            final ClientNotificationDisplayType type = ClientNotificationDisplayType.fromString(clientNotificationType);
                                            PrefUtils.setClientNotificationDisplayType(type);
                                            chatUpdateProcessor.postClientNotificationDisplayType(type);
                                        }
                                    }
                                },
                                e -> {
                                    LoggerEdna.info("error on getting settings : " + e.getMessage());
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
