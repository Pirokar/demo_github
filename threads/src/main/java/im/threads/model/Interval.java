package im.threads.model;

/**
 * Интервал расписания
 * Created by chybakut2004 on 21.04.17.
 */

public class Interval {

    private Integer weekDay;
    private Integer startTime;
    private Integer endTime;

    public Integer getWeekDay() {
        return weekDay;
    }

    /**
     * На сервере понедельник, это 1.
     * На клиенте воскресенье, это 1 (т.е. понедельник, это 2).
     * Переводит день недели в формат клиента.
     */
    public Integer getAndroidWeekDay() {
        if(weekDay == null) {
            return null;
        } else {
            int androidWeekDay = weekDay + 1;
            if(androidWeekDay == 8) {
                androidWeekDay = 1;
            }
            return androidWeekDay;
        }
    }

    public void setWeekDay(Integer weekDay) {
        this.weekDay = weekDay;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }
}
