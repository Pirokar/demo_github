package im.threads.internal.model;

public class SearchingConsult implements ChatItem {
    private long date;

    public SearchingConsult(long date) {
        this.date = date;
    }

    public long getTimeStamp() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchingConsult)) return false;
        SearchingConsult that = (SearchingConsult) o;
        return date == that.date;
    }

    @Override
    public int hashCode() {
        return (int) (date ^ (date >>> 32));
    }

    @Override
    public String toString() {
        return "SearchingConsult{" +
                "date=" + date +
                '}';
    }
}
