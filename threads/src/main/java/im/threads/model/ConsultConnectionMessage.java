package im.threads.model;

import com.google.gson.annotations.SerializedName;

public class ConsultConnectionMessage extends ConsultChatPhrase implements ChatItem {

    private String uuid;
    private String providerId; //This this a mfms messageId required for read status updates
    private final String type;
    private final String name;
    private final boolean sex;
    private final long date;
    private final String status;
    private String title;
    private boolean displayMessage;

    public ConsultConnectionMessage(String uuid, String providerId, String consultId, String type, String name, boolean sex, long date,
                                    String avatarPath, String status, String title, boolean displayMessage) {

        super(avatarPath, consultId);
        this.uuid = uuid;
        this.providerId = providerId;
        this.type = type;
        this.name = name;
        this.sex = sex;
        this.date = date;
        this.status = status;
        this.title = title;
        this.displayMessage = displayMessage;
    }

    public String getUuid() {
        return uuid;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

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

    public long getDate() {
        return date;
    }

    @SerializedName("display")
    public boolean isDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultConnectionMessage)) return false;
        if (uuid == null) return false;
        ConsultConnectionMessage that = (ConsultConnectionMessage) o;
        if (sex != that.sex) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        try {
            return uuid.equalsIgnoreCase(that.uuid);
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
}
