package im.threads.internal.transport.mfms_push;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.edna.android.push_lite.PushController;
import com.edna.android.push_lite.exception.PushServerErrorException;
import com.edna.android.push_lite.repo.push.remote.api.InMessageSend;

import im.threads.ConfigBuilder;
import im.threads.internal.Config;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.formatters.ChatItemType;
import im.threads.internal.model.CampaignMessage;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.transport.ChatItemProviderData;
import im.threads.internal.transport.OutgoingMessageCreator;
import im.threads.internal.transport.Transport;
import im.threads.internal.transport.TransportException;
import im.threads.internal.utils.PrefUtils;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class MFMSPushTransport extends Transport implements LifecycleObserver {

    private final ChatUpdateProcessor chatUpdateProcessor = ChatUpdateProcessor.getInstance();

    private CompositeDisposable compositeDisposable;
    @Nullable
    private Lifecycle lifecycle;

    @Override
    public void init() {
    }

    @Override
    public void sendRatingDone(Survey survey) {
        subscribe(
                Single.fromCallable(() -> {
                    String message = OutgoingMessageCreator.createRatingDoneMessage(
                            survey,
                            PrefUtils.getClientID(),
                            PrefUtils.getAppMarker()
                    ).toString();
                    return sendMessageMFMSSync(message, false);
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                response -> chatUpdateProcessor.postSurveySendSuccess(survey),
                                e -> chatUpdateProcessor.postError(new TransportException(e.getMessage()))
                        )
        );
    }

    @Override
    public void sendResolveThread(boolean approveResolve) {
        // if user approve to resolve the thread - send CLOSE_THREAD push
        // else if user doesn't approve to resolve the thread - send REOPEN_THREAD push
        // and then delete the request from the chat history
        subscribe(
                Completable.fromAction(() -> {
                    final String clientID = PrefUtils.getClientID();
                    final String message = approveResolve ?
                            OutgoingMessageCreator.createResolveThreadMessage(clientID).toString() :
                            OutgoingMessageCreator.createReopenThreadMessage(clientID).toString();
                    sendMessageMFMSSync(message, true);
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> chatUpdateProcessor.postRemoveChatItem(ChatItemType.REQUEST_CLOSE_THREAD),
                                e -> chatUpdateProcessor.postError(new TransportException(e.getMessage()))
                        )
        );
    }

    @Override
    public void sendUserTying(String input) {
        subscribe(
                Completable.fromAction(() -> {
                    String message = OutgoingMessageCreator.createMessageTyping(PrefUtils.getClientID(), input).toString().replace("\\\\", "");
                    sendMessageMFMSSync(message, true);
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                },
                                e -> chatUpdateProcessor.postError(new TransportException(e.getMessage()))
                        )
        );
    }

    @Override
    public void sendInit() {
        subscribe(
                Completable.fromAction(() -> {
                    String initMessage = OutgoingMessageCreator.createInitChatMessage(PrefUtils.getClientID(), PrefUtils.getData()).toString();
                    sendMessageMFMSSync(initMessage, true);
                    final String environmentMessage = OutgoingMessageCreator.createEnvironmentMessage(
                            PrefUtils.getUserName(),
                            PrefUtils.getClientID(),
                            PrefUtils.getClientIDEncrypted(),
                            PrefUtils.getData(),
                            Config.instance.context
                    ).toString();
                    sendMessageMFMSSync(environmentMessage, true);
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                },
                                e -> chatUpdateProcessor.postError(new TransportException(e.getMessage()))
                        )
        );
    }

    @Override
    public void sendMessage(UserPhrase userPhrase, ConsultInfo consultInfo, @Nullable String filePath, @Nullable String quoteFilePath) {
        subscribe(
                Single.fromCallable(() -> {
                    final String message = OutgoingMessageCreator.createUserPhraseMessage(
                            userPhrase,
                            consultInfo,
                            quoteFilePath,
                            filePath,
                            PrefUtils.getClientID()
                    ).toString();
                    return sendMessageMFMSSync(message, false);
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                response -> {
                                    long sentAt = response.getSentAt() == null ? 0 : response.getSentAt().getMillis();
                                    CampaignMessage campaignMessage = userPhrase.getCampaignMessage();
                                    if (campaignMessage != null) {
                                        chatUpdateProcessor.postCampaignMessageReplySuccess(campaignMessage);
                                    }
                                    chatUpdateProcessor.postChatItemSendSuccess(new ChatItemProviderData(userPhrase.getId(), response.getMessageId(), sentAt));
                                },
                                e -> {
                                    if (userPhrase.getId() != null) {
                                        chatUpdateProcessor.postChatItemSendError(userPhrase.getId());
                                    }
                                    chatUpdateProcessor.postError(new TransportException(e.getMessage()));
                                }
                        )
        );
    }

    @Override
    public void sendRatingReceived(Survey survey) {
        subscribe(
                Completable.fromAction(() -> {
                    final String message = OutgoingMessageCreator.createRatingReceivedMessage(
                            survey.getSendingId(),
                            PrefUtils.getClientID()
                    ).toString();
                    sendMessageMFMSSync(message, true);
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                },
                                e -> chatUpdateProcessor.postError(new TransportException(e.getMessage()))
                        )
        );
    }

    @Override
    public void sendClientOffline(String clientId) {
        subscribe(
                Completable.fromAction(() -> {
                    String message = OutgoingMessageCreator.createMessageClientOffline(clientId).toString().replace("\\\\", "");
                    sendMessageMFMSSync(message, true);
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                },
                                e -> chatUpdateProcessor.postError(new TransportException(e.getMessage()))
                        )
        );
    }

    @Override
    public ConfigBuilder.TransportType getType() {
        return ConfigBuilder.TransportType.MFMS_PUSH;
    }

    @NonNull
    @Override
    public String getToken() throws TransportException {
        try {
            String clientIdSignature = PrefUtils.getClientIdSignature();
            return (TextUtils.isEmpty(clientIdSignature) ? getPushControllerInstance().getDeviceAddress() : clientIdSignature)
                    + ":" + PrefUtils.getClientID();
        } catch (final PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

    @Override
    public synchronized void setLifecycle(@Nullable Lifecycle lifecycle) {
        if (this.lifecycle != null) {
            this.lifecycle.removeObserver(this);
        }
        this.lifecycle = lifecycle;
        if (this.lifecycle != null) {
            this.lifecycle.addObserver(this);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void sendUserInfo() {
        if (!TextUtils.isEmpty(PrefUtils.getClientID())) {
            sendInit();
        }
    }


    /**
     * Метод-обертка над методом mfms sendMessage
     */
    private InMessageSend.Response sendMessageMFMSSync(String message, boolean isSystem) throws PushServerErrorException {
        return getPushControllerInstance().sendMessage(message, isSystem);
    }

    /**
     * Call to {@link PushController}
     *
     * @throws PushServerErrorException, if deviceAddress is empty
     */
    private PushController getPushControllerInstance() throws PushServerErrorException {
        PushController controller = PushController.getInstance(Config.instance.context);
        String deviceAddress = controller.getDeviceAddress();
        if (deviceAddress != null && !deviceAddress.isEmpty()) {
            return controller;
        } else {
            throw new PushServerErrorException("DEVICE_ADDRESS_INVALID");
        }
    }

    private boolean subscribe(final Disposable event) {
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        return compositeDisposable.add(event);
    }
}
