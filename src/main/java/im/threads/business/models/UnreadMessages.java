package im.threads.business.models;

import android.content.Context;

import androidx.core.util.ObjectsCompat;

import im.threads.R;

public final class UnreadMessages implements ChatItem {
    private long timeStamp;
    private int count;

    public UnreadMessages(long timeStamp, int count) {
        this.timeStamp = timeStamp;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public String getMessage(Context context) {
        return context.getResources().getQuantityString(R.plurals.ecc_unread_messages, count, count);
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        return otherItem instanceof UnreadMessages;
    }

    @Override
    public Long getThreadId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnreadMessages that = (UnreadMessages) o;
        return timeStamp == that.timeStamp &&
                count == that.count;
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(timeStamp, count);
    }
}
