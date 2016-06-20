package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public class SearchingConsult implements ChatItem {
    private final long date;

    public SearchingConsult(long date) {
        this.date = date;
    }

    public long getTimeStamp() {
        return date;
    }

    @Override
    public String toString() {
        return "SearchingConsult{" +
                "date=" + date +
                '}';
    }
}
