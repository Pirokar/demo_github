package im.threads.model;

/**
 * Created by chybakut2004 on 17.04.17.
 */

public class ScheduleInfo implements ChatItem {

    private long date;

    public ScheduleInfo(long date) {
        this.date = date;
    }

    @Override
    public long getTimeStamp() {
        return date;
    }
}
