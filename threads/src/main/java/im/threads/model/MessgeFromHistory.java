package im.threads.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import im.threads.utils.DateHelper;

/**
 * Created by Admin on 25.05.2017.
 */

public class MessgeFromHistory implements ChatItem {

    private Long id;
    private String providerId;
    private Long threadId;
    private Operator operator;
    private Client client;
    private String receivedDate;
    private Channel channel;
    private boolean read;
    private String text;
    private List<Attachment> attachments;
    private List<MessgeFromHistory> quotes;
    private String type;

    public static List<MessgeFromHistory> getListMessageFromServerResponse(String response) {
        List<MessgeFromHistory> list = null;
        try {
            if (response != null) {
                Type listType = new TypeToken<ArrayList<MessgeFromHistory>>() {
                }.getType();
                list = new Gson().fromJson(response, listType);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
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

    public List<MessgeFromHistory> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<MessgeFromHistory> quotes) {
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
}
