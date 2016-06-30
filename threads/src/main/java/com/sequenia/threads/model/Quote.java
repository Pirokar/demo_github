package com.sequenia.threads.model;

/**
 * Created by yuri on 13.06.2016.
 */
public class Quote {
    private String header;
    private String text;
    private long timeStamp;

    public Quote(String header, String text, long timeStamp) {
        this.header = header;
        this.text = text;
        this.timeStamp = timeStamp;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHeader() {
        return header;
    }

    public String getText() {
        return text;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "header='" + header + '\'' +
                ", text='" + text + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
