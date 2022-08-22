package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import im.threads.business.models.ChatItem;

public final class ConsultConnectionMessage extends ConsultChatPhrase implements ChatItem, SystemMessage {

    private final String type;
    private final String name;
    private final boolean sex;
    private final long date;
    private final String status;
    private final String uuid;
    private String providerId; //This this a mfms messageId required for read status updates
    private final List<String> providerIds;
    private final String title;
    private final String orgUnit;
    private final String role;
    @SerializedName("display")
    private final boolean displayMessage;
    private final String text;
    private final Long threadId;

    /** Используется в старой БД. */
    @Deprecated
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
            String text,
            Long threadId
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
        this.role = null;
        this.displayMessage = displayMessage;
        this.text = text;
        this.threadId = threadId;
    }

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
            String role,
            boolean displayMessage,
            String text,
            Long threadId
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
        this.role = role;
        this.displayMessage = displayMessage;
        this.text = text;
        this.threadId = threadId;
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

    public String getRole() {
        return role;
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

    public boolean isDisplayMessage() {
        return displayMessage;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        if (otherItem instanceof ConsultConnectionMessage) {
            return ObjectsCompat.equals(this.uuid, ((ConsultConnectionMessage) otherItem).uuid);
        }
        return false;
    }

    @Override
    public Long getThreadId() {
        return threadId;
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
                ObjectsCompat.equals(role, that.role) &&
                ObjectsCompat.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(uuid, providerId, providerIds, type, name, sex, date, status,
                title, orgUnit, role, displayMessage, text);
    }
}
