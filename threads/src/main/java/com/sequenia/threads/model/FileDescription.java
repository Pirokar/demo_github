package com.sequenia.threads.model;

/**
 * Created by yuri on 13.06.2016.
 */
public class FileDescription {
    private final String header;
    private final String text;
    private final long timeStamp;

    public FileDescription(String header, String text, long timeStamp) {
        this.header = header;
        this.text = text;
        this.timeStamp = timeStamp;
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
        return "FileDescription{" +
                "header='" + header + '\'' +
                ", text='" + text + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
