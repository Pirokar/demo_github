package im.threads.internal.transport.threads_gate.responses;

import java.util.Date;

public class SendMessageData {
    private String messageId;
    private Date sentAt;

    public String getMessageId() {
        return messageId;
    }

    public Date getSentAt() {
        return sentAt;
    }
}
