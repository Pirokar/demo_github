package im.threads.internal.model;

public interface ChatItem {
    long getTimeStamp();
    boolean isTheSameItem(ChatItem otherItem);
    Long getThreadId();
}
