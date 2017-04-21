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
