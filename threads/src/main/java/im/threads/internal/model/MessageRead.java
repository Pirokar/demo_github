package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

import java.util.List;

public class MessageRead implements ChatItem {

    private final List<String> messageIds;

    public MessageRead(List<String> messageId) {
        this.messageIds = messageId;
    }

    public List<String> getMessageId() {
        return messageIds;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        return otherItem instanceof MessageRead;
    }

    @Override
    public Long getThreadId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageRead that = (MessageRead) o;
        return ObjectsCompat.equals(messageIds, that.messageIds);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(messageIds);
    }
}
