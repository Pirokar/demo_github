package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public class ConsultConnectionMessage implements ChatItem {
    private final String consultId;
    private final String type;
    private final String name;
    private final boolean sex;
    private final long date;
    private final String avatarPath;
    public static final String TYPE_JOINED = "CONSULT_CONNECTED";
    public static final String TYPE_LEFT = "TYPE_LEFT";

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

    public ConsultConnectionMessage(String consultId, String type, String name, boolean sex, long date, String avatarPath) {
        this.consultId = consultId;
        this.type = type;
        this.name = name;
        this.sex = sex;
        this.date = date;
        this.avatarPath = avatarPath;
    }

    public String getConsultId() {
        return consultId;
    }

    public String getType() {
        return type;
    }

    public String getAvatarPath() {
        return avatarPath;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultConnectionMessage)) return false;

        ConsultConnectionMessage that = (ConsultConnectionMessage) o;

        if (sex != that.sex) return false;
        if (date != that.date) return false;
        if (consultId != null ? !consultId.equals(that.consultId) : that.consultId != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return avatarPath != null ? avatarPath.equals(that.avatarPath) : that.avatarPath == null;

    }

    @Override
    public int hashCode() {
        int result = consultId != null ? consultId.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (sex ? 1 : 0);
        result = 31 * result + (int) (date ^ (date >>> 32));
        result = 31 * result + (avatarPath != null ? avatarPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConsultConnectionMessage{" +
                "consultId='" + consultId + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                ", date=" + date +
                ", avatarPath='" + avatarPath + '\'' +
                '}';
    }
}
