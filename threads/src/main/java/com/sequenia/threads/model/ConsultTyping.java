package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public class ConsultTyping implements ChatItem {
    private final long date;

    public long getTimeStamp() {
        return date;
    }

    public ConsultTyping(long date, String avatarPath) {
        this.date = date;
        this.avatarPath = avatarPath;
    }

    private final String avatarPath;


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
