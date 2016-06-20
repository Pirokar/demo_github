package com.sequenia.threads.model;

/**
 * Created by yuri on 16.06.2016.
 */
public class Space implements ChatItem {
    private final int height;
    private final long timeStamp;

    public Space(int height, long timeStamp) {
        this.height = height;
        this.timeStamp = timeStamp;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "Space{" +
                "height=" + height +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
