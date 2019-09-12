package im.threads.internal.transport.threads_gate.responses;

import com.google.gson.JsonObject;

import java.util.Date;

public class BaseMessage {
    private String messageId;
    private Date sentAt;
    private String notification;
    private JsonObject content;

    public String getMessageId() {
        return messageId;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public String getNotification() {
        return notification;
    }

    public JsonObject getContent() {
        return content;
    }
}
