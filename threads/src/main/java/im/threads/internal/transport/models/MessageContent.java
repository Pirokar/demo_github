package im.threads.internal.transport.models;

import java.util.Date;
import java.util.List;

import im.threads.internal.model.QuickReply;

public class MessageContent {
    private String uuid;
    private String text;
    private Date receivedDate;
    private Operator operator;
    private List<String> providerIds;
    private List<Attachment> attachments;
    private List<Quote> quotes;
    private List<QuickReply> quickReplies;

    public String getUuid() {
        return uuid;
    }

    public String getText() {
        return text;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public Operator getOperator() {
        return operator;
    }

    public List<String> getProviderIds() {
        return providerIds;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public List<Quote> getQuotes() {
        return quotes;
    }

    public List<QuickReply> getQuickReplies() {
        return quickReplies;
    }
}
