package im.threads.internal.transport;

public class SendMessageResponse {
    public final String messageId;
    public final long sentAt;

    public SendMessageResponse(String messageId, long sentAt) {
        this.messageId = messageId;
        this.sentAt = sentAt;
    }
}
