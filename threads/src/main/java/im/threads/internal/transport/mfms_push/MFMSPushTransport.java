package im.threads.internal.transport.mfms_push;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mfms.android.push_lite.PushController;
import com.mfms.android.push_lite.exception.PushServerErrorException;
import com.mfms.android.push_lite.repo.push.remote.api.InMessageSend;

import im.threads.ConfigBuilder;
import im.threads.internal.Config;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.formatters.ChatItemType;
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

public final class MFMSPushTransport implements Transport {

    private final ChatUpdateProcessor chatUpdateProcessor = ChatUpdateProcessor.getInstance();

    private CompositeDisposable compositeDisposable;

    @Override
    public void init() {
        PushController.getInstance(Config.instance.context).init();
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
                                response -> chatUpdateProcessor.postSurveySendSuccess(survey.getSendingId()),
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
    public void sendInitChatMessage() {
        subscribe(
                Completable.fromAction(() -> {
                    String message = OutgoingMessageCreator.createInitChatMessage(PrefUtils.getClientID(), PrefUtils.getData()).toString();
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
    public void sendEnvironmentMessage(String clientId) {
        subscribe(
                Completable.fromAction(() -> {
                    final String message = OutgoingMessageCreator.createEnvironmentMessage(
                            PrefUtils.getUserName(),
                            clientId,
                            PrefUtils.getClientIDEncrypted(),
                            PrefUtils.getData(),
                            Config.instance.context
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
    public void sendMessageRead(String messageId) {
        subscribe(
                Completable.fromAction(() -> getPushControllerInstance().notifyMessageRead(messageId))
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> chatUpdateProcessor.postConsultMessageWasRead(messageId),
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
                            PrefUtils.getClientID(),
                            PrefUtils.getThreadID()
                    ).toString();
                    return sendMessageMFMSSync(message, false);
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                response -> {
                                    long sentAt = response.getSentAt() == null ? 0 : response.getSentAt().getMillis();
                                    chatUpdateProcessor.postChatItemSendSuccess(new ChatItemProviderData(userPhrase.getUuid(), response.getMessageId(), sentAt));
                                },
                                e -> {
                                    chatUpdateProcessor.postChatItemSendError(userPhrase.getUuid());
                                    chatUpdateProcessor.postError(new TransportException(e.getMessage()));
                                }
                        )
        );
    }

    @Override
    public void sendRatingReceived(long sendingId) {
        subscribe(
                Completable.fromAction(() -> {
                    final String message = OutgoingMessageCreator.createRatingReceivedMessage(
                            sendingId,
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
    public void setLifecycle(Lifecycle lifecycle) {
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
