package im.threads.internal.transport;

public final class ChatItemProviderData {
    public final String uuid;
    public final String messageId;
    public final long sentAt;

    public ChatItemProviderData(String uuid, String messageId, long sentAt) {
        this.uuid = uuid;
        this.messageId = messageId;
        this.sentAt = sentAt;
    }
}
