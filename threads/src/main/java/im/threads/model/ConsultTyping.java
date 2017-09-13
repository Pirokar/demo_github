package im.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public class ConsultTyping extends ConsultChatPhrase implements ChatItem {
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
        if (this == o) return true;
        if (!(o instanceof ConsultTyping)) return false;

        ConsultTyping that = (ConsultTyping) o;

        if (date != that.date) return false;
        if (consultId != null ? !consultId.equals(that.consultId) : that.consultId != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (date ^ (date >>> 32));
        result = 31 * result + (consultId != null ? consultId.hashCode() : 0);
        return result;
    }
}
