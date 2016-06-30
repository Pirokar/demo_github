package com.sequenia.threads.model;

/**
 * Created by yuri on 13.06.2016.
 */
public class FileDescription {
    private String header;
    private String path;
    private long timeStamp;
    private int downloadProgress;

    public FileDescription(String header, String path, long timeStamp) {
        this.header = header;
        this.path = path;
        this.timeStamp = timeStamp;
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setPath(String text) {
        this.path = text;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getHeader() {
        return header;
    }

    public String getPath() {
        return path;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "FileDescription{" +
                "header='" + header + '\'' +
                ", path='" + path + '\'' +
                ", timeStamp=" + timeStamp +
                ", downloadProgress=" + downloadProgress +
                '}';
    }
}
