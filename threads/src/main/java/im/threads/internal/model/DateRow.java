package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

public final class DateRow implements ChatItem, MediaAndFileItem {
    private final long date;

    public DateRow(long date) {
        this.date = date;
    }

    @Override
    public long getTimeStamp() {
        return date;
    }

    public long getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "DateRow{" +
                "date=" + date +
                '}';
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        return otherItem instanceof DateRow;
    }

    @Override
    public Long getThreadId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRow dateRow = (DateRow) o;
        return date == dateRow.date;
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(date);
    }
}
