package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

import java.util.List;

import im.threads.internal.utils.DateHelper;

public final class MessageFromHistory implements ChatItem {
    private String uuid;
    private String providerId; //This this a mfms messageId required for read status updates
    private List<String> providerIds;
    private String clientId;
    private Long threadId;
    private Operator operator;
    private Client client;
    private String receivedDate;
    private Channel channel;
    private boolean read;
    private String formattedText;
    private String text;
    private List<Attachment> attachments;
    private List<QuickReply> quickReplies;
    private List<MessageFromHistory> quotes;
    private String type;
    private boolean display;

    //SURVEY
    private Long hideAfter;

    //SURVEY ANSWERED
    private Long sendingId;
    private Long questionId;
    private Integer rate;
    private Integer scale;
    private boolean simple;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String id) {
        this.uuid = id;
    }

    public String getProviderId() {
        return providerId;
    }

    public List<String> getProviderIds() {
        return providerIds;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getFormattedText() {
        return formattedText;
    }

    public void setFormattedText(String formattedText) {
        this.formattedText = formattedText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public List<QuickReply> getQuickReplies() {
        return quickReplies;
    }

    public void setQuickReplies(List<QuickReply> quickReplies) {
        this.quickReplies = quickReplies;
    }

    public List<MessageFromHistory> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<MessageFromHistory> quotes) {
        this.quotes = quotes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public long getTimeStamp() {
        return DateHelper.getMessageTimestampFromDateString(receivedDate);
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public Long getSendingId() {
        return sendingId;
    }

    public void setSendingId(Long sendingId) {
        this.sendingId = sendingId;
    }

    public Long getHideAfter() {
        return hideAfter;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        if (otherItem instanceof MessageFromHistory) {
            return ObjectsCompat.equals(this.uuid, ((MessageFromHistory) otherItem).uuid);
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageFromHistory that = (MessageFromHistory) o;
        return read == that.read &&
                display == that.display &&
                simple == that.simple &&
                ObjectsCompat.equals(uuid, that.uuid) &&
                ObjectsCompat.equals(providerId, that.providerId) &&
                ObjectsCompat.equals(providerIds, that.providerIds) &&
                ObjectsCompat.equals(clientId, that.clientId) &&
                ObjectsCompat.equals(threadId, that.threadId) &&
                ObjectsCompat.equals(operator, that.operator) &&
                ObjectsCompat.equals(client, that.client) &&
                ObjectsCompat.equals(receivedDate, that.receivedDate) &&
                ObjectsCompat.equals(channel, that.channel) &&
                ObjectsCompat.equals(formattedText, that.formattedText) &&
                ObjectsCompat.equals(text, that.text) &&
                ObjectsCompat.equals(attachments, that.attachments) &&
                ObjectsCompat.equals(quickReplies, that.quickReplies) &&
                ObjectsCompat.equals(quotes, that.quotes) &&
                ObjectsCompat.equals(type, that.type) &&
                ObjectsCompat.equals(hideAfter, that.hideAfter) &&
                ObjectsCompat.equals(sendingId, that.sendingId) &&
                ObjectsCompat.equals(questionId, that.questionId) &&
                ObjectsCompat.equals(rate, that.rate) &&
                ObjectsCompat.equals(scale, that.scale);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(uuid, providerId, providerIds, clientId, threadId, operator, client, receivedDate, channel, read, formattedText, text, attachments, quickReplies, quotes, type, display, hideAfter, sendingId, questionId, rate, scale, simple);
    }
}
