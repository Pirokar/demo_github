package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

import java.util.Calendar;
import java.util.Objects;

public final class SearchingConsult implements ChatItem {
    private long date;

    public SearchingConsult() {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        this.date = c.getTimeInMillis();
    }

    public long getTimeStamp() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        return otherItem instanceof SearchingConsult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchingConsult that = (SearchingConsult) o;
        return date == that.date;
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(date);
    }

    @Override
    public String toString() {
        return "SearchingConsult{" +
                "date=" + date +
                '}';
    }
}
