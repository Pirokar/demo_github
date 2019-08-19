package im.threads.internal.model;

import android.support.v4.util.ObjectsCompat;

public final class ConsultTyping extends ConsultChatPhrase implements ChatItem {
    private long date;

    @Override
    public long getTimeStamp() {
        return date;
    }

    public ConsultTyping(String consultId, long date, String avatarPath) {
        super(avatarPath, consultId);
        this.date = date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConsultTyping)) {
            return false;
        }

        ConsultTyping that = (ConsultTyping) o;

        if (date != that.date) return false;
        return ObjectsCompat.equals(consultId, that.consultId);
    }

    @Override
    public int hashCode() {
        int result = (int) (date ^ (date >>> 32));
        result = 31 * result + (consultId != null ? consultId.hashCode() : 0);
        return result;
    }
}
