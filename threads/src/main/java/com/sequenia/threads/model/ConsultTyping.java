package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public class ConsultTyping implements ChatItem {
    private final long date;
    private final String consultId;
    private final String avatarPath;

    public long getTimeStamp() {
        return date;
    }

    public ConsultTyping(String consultId, long date, String avatarPath) {
        this.consultId = consultId;
        this.date = date;
        this.avatarPath = avatarPath;
    }

    public long getDate() {
        return date;
    }

    public String getConsultId() {
        return consultId;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    @Override
    public String toString() {
        return "ConsultTyping{" +
                "date=" + date +
                ", avatarPath='" + avatarPath + '\'' +
                '}';
    }
}
