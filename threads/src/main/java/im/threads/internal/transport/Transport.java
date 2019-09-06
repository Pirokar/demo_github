package im.threads.internal.transport;

import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;

public interface Transport {

    void init();

    void sendRatingDone(Survey survey) throws TransportException;

    void sendResolveThread(boolean approveResolve) throws TransportException;

    void sendUserTying(String input) throws TransportException;

    void sendInitChatMessage() throws TransportException;

    void sendEnvironmentMessage(String clientId) throws TransportException;

    void onClientIdChanged(String clientId) throws TransportException;

    void sendMessageRead(String messageId) throws TransportException;

    SendMessageResponse sendMessage(UserPhrase userPhrase, ConsultInfo consultInfo, final String mfmsFilePath, final String mfmsQuoteFilePath) throws TransportException;

    void sendRatingReceived(long sendingId) throws TransportException;

    void onSettingClientId(String clientId) throws TransportException;

    void notifyMessageUpdateNeeded() throws TransportException;

    void sendClientOffline(String clientId) throws TransportException;

    String getToken() throws TransportException;
}
