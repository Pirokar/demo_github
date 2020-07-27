package im.threads.internal.model;

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
}
