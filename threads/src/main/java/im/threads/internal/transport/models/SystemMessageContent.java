package im.threads.internal.transport.models;

public class SystemMessageContent {

    private String type;
    private String uuid;
    private String text;
    private long threadId;

    public String getType() {
        return type;
    }

    public String getUuid() {
        return uuid;
    }

    public String getText() {
        return text;
    }

    public long getThreadId() {
        return threadId;
    }
}