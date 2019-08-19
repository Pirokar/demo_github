package im.threads.internal.model;

/**
 * Объект для обработки пуша с пришедшим threadId
 */
public final class SaveThreadIdChatItem implements ChatItem {

    private Long threadId;

    public SaveThreadIdChatItem(final Long threadId) {
        this.threadId = threadId;
    }

    public Long getThreadId() {
        return threadId;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }
}
