package im.threads.internal.model;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
    private boolean active;

    private long serverTimeDiff;

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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public void calculateServerTimeDiff() {
        serverTimeDiff = getCurrentUtcTime() - serverTime.getTime();
    }

    /**
     * @return true, если в данный момент чат работает
     */
    public boolean isChatWorking() {
        if (startTime == null || endTime == null || serverTime == null) {
            return active;
        }
        long currentServerTime = getCurrentUtcTime() - serverTimeDiff;
        if (active) {
            //Next unavailability not started yet
            // всегда true т.к. startTime - это дата и время старта ближайшего интервала неактивности чата
            if (currentServerTime < startTime.getTime()) {
                return true;
            }

            //Next unavailability started
            if (currentServerTime > startTime.getTime() && currentServerTime < endTime.getTime()) {
                return false;
            }

            //Next unavailability ended
            if (currentServerTime > endTime.getTime()) {
                return true;
            }
        } else {
            // всегда true т.к. endTime - это дата и время окончания ближайшего(или текущего) интервала неактивности чата
            if (currentServerTime < endTime.getTime()) {
                return false;
            }

            //Unavailability ended, next unavailability not started yet
            if (currentServerTime > endTime.getTime() && currentServerTime < startTime.getTime()) {
                return true;
            }

            //Next unavailability started
            if (currentServerTime > startTime.getTime()) {
                return true;
            }
        }
        return true;
    }

    private long getCurrentUtcTime() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
    }
}
