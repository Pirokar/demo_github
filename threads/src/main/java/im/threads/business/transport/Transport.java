package im.threads.business.transport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.google.gson.Gson;

import java.util.List;

import im.threads.business.logger.LoggerEdna;
import im.threads.business.models.ClientNotificationDisplayType;
import im.threads.business.models.ConsultInfo;
import im.threads.business.models.Survey;
import im.threads.business.models.UserPhrase;
import im.threads.business.rest.models.SettingsResponse;
import im.threads.business.rest.queries.BackendApi;
import im.threads.business.rest.queries.ThreadsApi;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.ui.utils.preferences.PrefUtilsUi;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

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

    public void getSettings() {
        LoggerEdna.info(ThreadsApi.REST_TAG, "Loading settings");
        subscribe(
                Single.fromCallable(() -> BackendApi.get().settings().execute())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    final SettingsResponse responseBody = response.body();
                                    if (responseBody != null) {
                                        showSettingsReceivedSuccessfullyLog(response, responseBody);
                                        String clientNotificationType = responseBody.getClientNotificationDisplayType();
                                        if (clientNotificationType != null && !clientNotificationType.isEmpty()) {
                                            final ClientNotificationDisplayType type = ClientNotificationDisplayType.fromString(clientNotificationType);
                                            PrefUtilsUi.setClientNotificationDisplayType(type);
                                            chatUpdateProcessor.postClientNotificationDisplayType(type);
                                        }
                                    } else {
                                        showSettingsReceivedFailureLog(response);
                                    }
                                },
                                e -> {
                                    showSettingsReceivedErrorLog(e);
                                    chatUpdateProcessor.postError(new TransportException(e.getMessage()));
                                }
                        )

        );
    }

    private void showSettingsReceivedSuccessfullyLog(
            Response<SettingsResponse> response,
            SettingsResponse responseBody
    ) {
        String body;
        try {
            body = new Gson().toJson(responseBody);
        } catch (Exception exc) {
            String errorMessage = response.errorBody() != null ? response.errorBody().toString()
                    : "no error message";
            body = "Settings receive error. Cannot parse the body. Error message: " + errorMessage +
                    "\nException: " + exc;
        }
        LoggerEdna.info(ThreadsApi.REST_TAG, "Settings received. Response: " + response +
                ". Body: " + body);
    }

    private void showSettingsReceivedFailureLog(Response<SettingsResponse> response) {
        LoggerEdna.error(ThreadsApi.REST_TAG, "Settings received. Response body is null! Response: " + response);
    }

    private void showSettingsReceivedErrorLog(Throwable throwable) {
        LoggerEdna.error(ThreadsApi.REST_TAG, "Settings receive error.", throwable);
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
