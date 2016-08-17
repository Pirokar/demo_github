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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultTyping)) return false;

        ConsultTyping that = (ConsultTyping) o;

        if (date != that.date) return false;
        if (consultId != null ? !consultId.equals(that.consultId) : that.consultId != null)
            return false;
        return avatarPath != null ? avatarPath.equals(that.avatarPath) : that.avatarPath == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (date ^ (date >>> 32));
        result = 31 * result + (consultId != null ? consultId.hashCode() : 0);
        result = 31 * result + (avatarPath != null ? avatarPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConsultTyping{" +
                "date=" + date +
                ", avatarPath='" + avatarPath + '\'' +
                '}';
    }
}
