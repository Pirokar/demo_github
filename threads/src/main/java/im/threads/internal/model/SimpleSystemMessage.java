package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

public class SimpleSystemMessage implements ChatItem, SystemMessage {

    private String uuid;

    private String type;

    private long sentAt;

    private String text;

    public SimpleSystemMessage(String uuid, String type, long sentAt, String text) {
        this.uuid = uuid;
        this.type = type;
        this.sentAt = sentAt;
        this.text = text;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleSystemMessage that = (SimpleSystemMessage) o;
        return sentAt == that.sentAt &&
                ObjectsCompat.equals(uuid, that.uuid) &&
                ObjectsCompat.equals(type, that.type) &&
                ObjectsCompat.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(uuid, type, sentAt, text);
    }
}
