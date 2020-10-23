package im.threads.internal.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import androidx.core.util.ObjectsCompat;

public final class ConsultConnectionMessage extends ConsultChatPhrase implements ChatItem, SystemMessage {

    private final String type;
    private final String name;
    private final boolean sex;
    private final long date;
    private final String status;
    private String uuid;
    private String providerId; //This this a mfms messageId required for read status updates
    private List<String> providerIds;
    private String title;
    private String orgUnit;
    private boolean displayMessage;
    private String text;

    public ConsultConnectionMessage(
            String uuid,
            String providerId,
            List<String> providerIds,
            String consultId,
            String type,
            String name,
            boolean sex,
            long date,
            String avatarPath,
            String status,
            String title,
            String orgUnit,
            boolean displayMessage,
            String text
    ) {
        super(avatarPath, consultId);
        this.uuid = uuid;
        this.providerId = providerId;
        this.providerIds = providerIds;
        this.type = type;
        this.name = name;
        this.sex = sex;
        this.date = date;
        this.status = status;
        this.title = title;
        this.orgUnit = orgUnit;
        this.displayMessage = displayMessage;
        this.text = text;
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

    public String getText() {
        return text;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        if (otherItem instanceof ConsultConnectionMessage) {
            return this.uuid.equals(((ConsultConnectionMessage) otherItem).uuid);
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsultConnectionMessage that = (ConsultConnectionMessage) o;
        return sex == that.sex &&
                date == that.date &&
                displayMessage == that.displayMessage &&
                ObjectsCompat.equals(uuid, that.uuid) &&
                ObjectsCompat.equals(providerId, that.providerId) &&
                ObjectsCompat.equals(providerIds, that.providerIds) &&
                ObjectsCompat.equals(type, that.type) &&
                ObjectsCompat.equals(name, that.name) &&
                ObjectsCompat.equals(status, that.status) &&
                ObjectsCompat.equals(title, that.title) &&
                ObjectsCompat.equals(orgUnit, that.orgUnit) &&
                ObjectsCompat.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(uuid, providerId, providerIds, type, name, sex, date, status, title, orgUnit, displayMessage, text);
    }
}
