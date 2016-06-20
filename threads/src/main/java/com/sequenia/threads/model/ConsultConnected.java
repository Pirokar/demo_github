package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public class ConsultConnected implements ChatItem {
    private final String name;
    private final boolean sex;
    private final long date;
    private final String avatarPath;

    public String getName() {
        return name;
    }

    public  boolean getSex() {
        return sex;
    }

    @Override
    public long getTimeStamp() {
        return date;
    }

    public ConsultConnected(String name, boolean sex, long date, String avatarPath) {
        this.name = name;
        this.sex = sex;
        this.date = date;
        this.avatarPath = avatarPath;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    @Override
    public String toString() {
        return "ConsultConnected{" +
                "name='" + name + '\'' +
                ", sex=" + sex +
                ", date=" + date +
                ", avatarPath='" + avatarPath + '\'' +
                '}';
    }
}
