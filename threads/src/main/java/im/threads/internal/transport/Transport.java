package im.threads.internal.transport;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import im.threads.ConfigBuilder;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UserPhrase;

public interface Transport {

    void init();

    void sendRatingDone(Survey survey);

    void sendResolveThread(boolean approveResolve);

    void sendUserTying(String input);

    void sendInitChatMessage();

    void sendEnvironmentMessage(String clientId);

    void sendMessageRead(String messageId);

    /**
     * TODO THREADS-6292: this method can potentially lead to messages stuck in STATE_SENDING
     */
    void sendMessage(UserPhrase userPhrase, ConsultInfo consultInfo, final String filePath, final String quoteFilePath);

    void sendRatingReceived(long sendingId);

    void sendClientOffline(String clientId);

    ConfigBuilder.TransportType getType();

    @NonNull
    String getToken() throws TransportException;

    void setLifecycle(Lifecycle lifecycle);
}
