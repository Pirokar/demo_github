package im.threads.internal.transport.models;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;

import im.threads.internal.model.QuickReply;
import im.threads.internal.model.Settings;

public final class MessageContent {
    private String uuid;
    private String text;
    private String formattedText;
    private Date receivedDate;
    private Long threadId;
    private Operator operator;
    private List<String> providerIds;
    private List<Attachment> attachments;
    private List<Quote> quotes;
    private List<QuickReply> quickReplies;
    @Nullable
    private Settings settings;

    public String getUuid() {
        return uuid;
    }

    public String getText() {
        return text;
    }

    public String getFormattedText() {
        return formattedText;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public Long getThreadId() {
        return threadId;
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

    @Nullable
    public Settings getSettings() {
        return settings;
    }
}
