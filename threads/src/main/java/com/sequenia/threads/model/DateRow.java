package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public class DateRow implements ChatItem {
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
