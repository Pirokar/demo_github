package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

import im.threads.business.models.ChatItem;

public class OperatorLookupStarted implements ChatItem {

    private final long timeStamp;

    public OperatorLookupStarted(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        return otherItem instanceof OperatorLookupStarted;
    }

    @Override
    public Long getThreadId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperatorLookupStarted that = (OperatorLookupStarted) o;
        return timeStamp == that.timeStamp;
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(timeStamp);
    }
}
