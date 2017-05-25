package im.threads.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Admin on 25.05.2017.
 */

public class MessgeFromHistory implements ChatItem {


    {
        "id": 15,
            "providerId": "140446819000",
            "threadId": 2,

        "receivedDate": "2017-05-25T08:03:43Z",

        "read": false,
            "text": "",
            "attachments": [
        {
            "result": "https://datastore.threads.im/files/79a28402-5cf4-4c59-8bce-aa7dd84b0951",
                "optional": {
            "type": "image/jpg",
                    "name": "gurman1492760778487.jpg",
                    "size": 1769998
        }
        }
    ],
        "quotes": []
    }





    private Long id;
    private String providerId;
    private Long threadId;
    private Operator operator;
    private String receivedDate;
    private Channel channel;
    private boolean read;
    private String text;
    private List<Attachment> attachments;
    private List<Quote> quotes;
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Client getOperator() {
        return operator;
    }

    public void setOperator(Client client) {
        this.operator = client;
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

    public List<Quote> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<Quote> quotes) {
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        try {
            date = sdf.parse(receivedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
}
