package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

import im.threads.business.models.ChatItem;

public class SimpleSystemMessage implements ChatItem, SystemMessage {

    private final String uuid;

    private final String type;

    private final long sentAt;

    private final String text;

    private final Long threadId;

    public SimpleSystemMessage(String uuid, String type, long sentAt, String text, Long threadId) {
        this.uuid = uuid;
        this.type = type;
        this.sentAt = sentAt;
        this.text = text;
        this.threadId = threadId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public long getSentAt() {
        return sentAt;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public long getTimeStamp() {
        return sentAt;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        if (otherItem instanceof SimpleSystemMessage) {
            return ObjectsCompat.equals(this.uuid, ((SimpleSystemMessage) otherItem).uuid);
        }
        return false;
    }

    @Override
    public Long getThreadId() {
        return threadId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleSystemMessage that = (SimpleSystemMessage) o;
        return sentAt == that.sentAt &&
                ObjectsCompat.equals(uuid, that.uuid) &&
                ObjectsCompat.equals(type, that.type) &&
                ObjectsCompat.equals(text, that.text) &&
                ObjectsCompat.equals(threadId, that.threadId);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(uuid, type, sentAt, text, threadId);
    }
}
