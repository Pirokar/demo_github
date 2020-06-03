package im.threads.internal.model;

import java.util.Date;

/**
 * Информация о расписании
 */
public final class ScheduleInfo implements ChatItem {

    private Long id;
    private String notification;
    private boolean sendDuringInactive;
    private long date;

    private Date startTime;
    private Date endTime;
    private Date serverTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isSendDuringInactive() {
        return sendDuringInactive;
    }

    @Override
    public long getTimeStamp() {
        return date;
    }

    /**
     * @return true, если в данный момент чат работает
     */
    public boolean isChatWorking() {
        return startTime.getTime() <= serverTime.getTime() && serverTime.getTime() <= endTime.getTime();
    }
}
