package im.threads.model;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Информация о расписании
 * Created by chybakut2004 on 17.04.17.
 */

public class ScheduleInfo implements ChatItem {

    private Long id;
    private String notification;
    private List<Interval> intervals;
    private long date;

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

    public List<Interval> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<Interval> intervals) {
        this.intervals = intervals;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public long getTimeStamp() {
        return date;
    }

    /**
     * @return true, если в данный момент чат работает
     */
    public boolean isChatWorking() {
        if(intervals == null) {
            return false;
        }

        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar now = Calendar.getInstance(timeZone);

        // Поиск интервала для текущего дня недели
        for(int i = 0; i < intervals.size(); i++) {
            Interval dayInterval = intervals.get(i);
            Integer dayOfWeek = dayInterval.getAndroidWeekDay();

            if(dayOfWeek != null) {
                int nowDayOfWeek = now.get(Calendar.DAY_OF_WEEK);

                // Текущий день недели найден в расписании
                if(dayOfWeek == nowDayOfWeek) {
                    Integer start = dayInterval.getStartTime();
                    Integer end = dayInterval.getEndTime();

                    // Расписание корректно, указаны обе границы промежутка.
                    if (start != null && end != null) {
                        // Начало промежутка в текущем найденном дне
                        Calendar startDate = Calendar.getInstance(timeZone);
                        // Конец промежутка в текущем найденном дне
                        Calendar endDate = Calendar.getInstance(timeZone);

                        startDate.set(Calendar.HOUR_OF_DAY, 0);
                        startDate.set(Calendar.MINUTE, 0);
                        startDate.set(Calendar.SECOND, 0);

                        endDate.set(Calendar.HOUR_OF_DAY, 0);
                        endDate.set(Calendar.MINUTE, 0);
                        endDate.set(Calendar.SECOND, 0);

                        startDate.add(Calendar.SECOND, start);
                        endDate.add(Calendar.SECOND, end);

                        // Текущий день попадает в промежуток расписания
                        if(startDate.getTimeInMillis() <= now.getTimeInMillis() && now.getTimeInMillis() <= endDate.getTimeInMillis()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
