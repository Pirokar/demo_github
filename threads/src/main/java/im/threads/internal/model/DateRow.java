package im.threads.internal.model;

public class DateRow implements ChatItem, MediaAndFileItem {
    private final long date;

    @Override
    public long getTimeStamp() {
        return date;
    }

    public DateRow(long date) {
        this.date = date;
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
}
