package im.threads.internal.transport.models;

public class RequestResolveThreadContent {
    private String uuid;
    private long hideAfter;
    private long threadId;

    public String getUuid() {
        return uuid;
    }

    public long getHideAfter() {
        return hideAfter;
    }
    public long getThreadId() {
        return threadId;
    }
}
