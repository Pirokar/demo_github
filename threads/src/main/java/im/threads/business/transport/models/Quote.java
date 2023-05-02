package im.threads.business.transport.models;

import java.util.Date;
import java.util.List;

public class Quote {
    private long id;
    private Date receivedDate;
    private String uuid;
    private String text;
    private List<Attachment> attachments;
    private Operator operator;

    public long getId() {
        return id;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public String getUuid() {
        return uuid;
    }

    public String getText() {
        return text;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Operator getOperator() {
        return operator;
    }
}
