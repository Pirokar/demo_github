package im.threads.internal.transport.mfms_push;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.mfms.android.push_lite.PushController;
import com.mfms.android.push_lite.exception.PushServerErrorException;
import com.mfms.android.push_lite.repo.push.remote.api.InMessageSend;

import im.threads.internal.Config;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.retrofit.ServiceGenerator;
import im.threads.internal.transport.SendMessageResponse;
import im.threads.internal.transport.Transport;
import im.threads.internal.transport.TransportException;
import im.threads.internal.utils.PrefUtils;

public final class MFMSPushTransport implements Transport {
    @Override
    public void init() {
        PushController.getInstance(Config.instance.context).init();
        ServiceGenerator.setUserAgent(OutgoingMessageCreator.getUserAgent(Config.instance.context));
    }

    @WorkerThread
    @Override
    public void sendRatingDone(Survey survey) throws TransportException {
        String message = OutgoingMessageCreator.createRatingDoneMessage(
                survey,
                PrefUtils.getClientID(),
                PrefUtils.getAppMarker()
        );
        try {
            sendMessageMFMSSync(message, false);
        } catch (PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

    @WorkerThread
    @Override
    public void sendResolveThread(boolean approveResolve) throws TransportException {
        // if user approve to resolve the thread - send CLOSE_THREAD push
        // else if user doesn't approve to resolve the thread - send REOPEN_THREAD push
        // and then delete the request from the chat history
        final String clientID = PrefUtils.getClientID();
        final String message = approveResolve ?
                OutgoingMessageCreator.createResolveThreadMessage(clientID) :
                OutgoingMessageCreator.createReopenThreadMessage(clientID);
        try {
            sendMessageMFMSSync(message, true);
        } catch (PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

    @WorkerThread
    @Override
    public void sendUserTying(String input) throws TransportException {
        String message = OutgoingMessageCreator.createMessageTyping(PrefUtils.getClientID(), input);
        try {
            sendMessageMFMSSync(message, true);
        } catch (PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

    @WorkerThread
    @Override
    public void sendInitChatMessage() throws TransportException {
        String message = OutgoingMessageCreator.createInitChatMessage(PrefUtils.getClientID(), PrefUtils.getData());
        try {
            sendMessageMFMSSync(message, true);
        } catch (PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

    @WorkerThread
    @Override
    public void sendEnvironmentMessage(String clientId) throws TransportException {
        final String message = OutgoingMessageCreator.createEnvironmentMessage(
                PrefUtils.getUserName(),
                clientId,
                PrefUtils.getClientIDEncrypted(),
                PrefUtils.getData(),
                Config.instance.context
        );
        try {
            sendMessageMFMSSync(message, true);
        } catch (PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

    @Override
    public void sendMessageRead(String messageId) throws TransportException {
        try {
            getPushControllerInstance().notifyMessageRead(messageId);
        } catch (final PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

    @Override
    public SendMessageResponse sendMessage(UserPhrase userPhrase, ConsultInfo consultInfo, @Nullable String filePath, @Nullable String quoteFilePath) throws TransportException {
        final String message = OutgoingMessageCreator.createUserPhraseMessage(
                userPhrase,
                consultInfo,
                quoteFilePath,
                filePath,
                PrefUtils.getClientID(),
                PrefUtils.getThreadID()
        );
        try {
            InMessageSend.Response response = sendMessageMFMSSync(message, false);
            long sentAt = response.getSentAt() == null ? 0 : response.getSentAt().getMillis();
            return new SendMessageResponse(response.getMessageId(), sentAt);
        } catch (final PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

    @Override
    public void sendRatingReceived(long sendingId) throws TransportException {
        final String message = OutgoingMessageCreator.createRatingReceivedMessage(
                sendingId,
                PrefUtils.getClientID()
        );
        try {
            sendMessageMFMSSync(message, true);
        } catch (final PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

    @Override
    public void sendClientOffline(String clientId) throws TransportException {
        String message = OutgoingMessageCreator.createMessageClientOffline(clientId);
        try {
            sendMessageMFMSSync(message, true);
        } catch (final PushServerErrorException e) {
            throw new TransportException(e.getMessage());
        }
    }

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
            throw new PushServerErrorException(PushServerErrorException.DEVICE_ADDRESS_INVALID);
        }
    }
}
