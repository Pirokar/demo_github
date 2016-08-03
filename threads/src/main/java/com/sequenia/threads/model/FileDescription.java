package com.sequenia.threads.model;

/**
 * Created by yuri on 13.06.2016.
 */
public class FileDescription {
    private String from;
    private String filePath;
    private String downloadPath;
    private String incomingName;
    private final long size;
    private long timeStamp;
    private int downloadProgress;

    public FileDescription(String from, String filePath, long size, long timeStamp) {
        this.from = from;
        this.filePath = filePath;
        this.size = size;
        this.timeStamp = timeStamp;
    }

    public String getIncomingName() {
        return incomingName;
    }

    public void setIncomingName(String incomingName) {
        this.incomingName = incomingName;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public void setFrom(String header) {
        this.from = header;
    }

    public void setFilePath(String text) {
        this.filePath = text;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFileSentTo() {
        return from;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "FileDescription{" +
                "header='" + from + '\'' +
                ", filePath='" + filePath + '\'' +
                ", downloadPath='" + downloadPath + '\'' +
                ", incomingName='" + incomingName + '\'' +
                ", size=" + size +
                ", timeStamp=" + timeStamp +
                ", downloadProgress=" + downloadProgress +
                '}';
    }
}
