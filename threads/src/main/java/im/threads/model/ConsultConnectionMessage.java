package im.threads.model;

public class ConsultConnectionMessage extends ConsultChatPhrase implements ChatItem {
    private final String type;
    private final String name;
    private final boolean sex;
    private final long date;
    private final String status;
    private String title;
    private final String messageId;
    private boolean displayMessage;

    public String getName() {
        return name;
    }

    public boolean getSex() {
        return sex;
    }

    @Override
    public long getTimeStamp() {
        return date;
    }

    public ConsultConnectionMessage(
            String consultId
            , String type
            , String name
            , boolean sex
            , long date
            , String avatarPath
            , String status
            , String title
            , String messageId
            , boolean displayMessage) {
        super(avatarPath, consultId);
        this.type = type;
        this.name = name;
        this.sex = sex;
        this.date = date;
        this.status = status;
        this.title = title;
        this.messageId = messageId;
        this.displayMessage = displayMessage;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getConnectionType() {
        return type;
    }

    public boolean isSex() {
        return sex;
    }

    public boolean isDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultConnectionMessage)) return false;
        if (messageId == null) return false;
        ConsultConnectionMessage that = (ConsultConnectionMessage) o;
        if (sex != that.sex) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        try {
            return messageId.equalsIgnoreCase(that.messageId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return true;

    }

    @Override
    public int hashCode() {
        int result = consultId != null ? consultId.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (sex ? 1 : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    public long getDate() {
        return date;
    }
}
