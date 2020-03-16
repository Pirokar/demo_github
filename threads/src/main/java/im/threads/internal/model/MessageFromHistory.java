package im.threads.internal.model;

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
    private String text;
    private List<Attachment> attachments;
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

}
