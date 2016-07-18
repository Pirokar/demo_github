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
