package im.threads.business.models;

import androidx.core.util.ObjectsCompat;

public final class Space implements ChatItem {
    private final int height;
    private final long timeStamp;

    public Space(int height, long timeStamp) {
        this.height = height;
        this.timeStamp = timeStamp;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        return otherItem instanceof Space;
    }

    @Override
    public Long getThreadId() {
        return null;
    }

    @Override
    public String toString() {
        return "Space{" +
                "height=" + height +
                ", timeStamp=" + timeStamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Space space = (Space) o;
        return height == space.height &&
                timeStamp == space.timeStamp;
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(height, timeStamp);
    }
}
