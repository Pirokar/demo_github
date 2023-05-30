package im.threads.business.models;

import androidx.core.util.ObjectsCompat;

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
    public boolean isTheSameItem(ChatItem otherItem) {
        return otherItem instanceof ConsultTyping;
    }

    @Override
    public Long getThreadId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsultTyping that = (ConsultTyping) o;
        return date == that.date;
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(date);
    }
}
