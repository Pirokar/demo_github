package im.threads.internal.model;

import android.support.v4.util.ObjectsCompat;

import com.google.gson.annotations.SerializedName;

public final class ConsultConnectionMessage extends ConsultChatPhrase implements ChatItem {

    private String uuid;
    private String providerId; //This this a mfms messageId required for read status updates
    private final String type;
    private final String name;
    private final boolean sex;
    private final long date;
    private final String status;
    private String title;
    private String orgUnit;
    private boolean displayMessage;

    public ConsultConnectionMessage(String uuid, String providerId, String consultId, String type, String name, boolean sex, long date,
                                    String avatarPath, String status, String title, String orgUnit, boolean displayMessage) {

        super(avatarPath, consultId);
        this.uuid = uuid;
        this.providerId = providerId;
        this.type = type;
        this.name = name;
        this.sex = sex;
        this.date = date;
        this.status = status;
        this.title = title;
        this.orgUnit = orgUnit;
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

    public String getOrgUnit() {
        return orgUnit;
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
        if (!ObjectsCompat.equals(type, that.type)) return false;
        if (!ObjectsCompat.equals(name, that.name)) return false;
        return uuid.equalsIgnoreCase(that.uuid);
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
