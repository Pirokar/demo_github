package com.sequenia.threads.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.sequenia.threads.utils.FileUtils;

/**
 * Created by yuri on 13.06.2016.
 */
public class FileDescription implements Parcelable {
    private static final String TAG = "FileDescription ";
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
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(from);
        dest.writeString(filePath);
        dest.writeString(downloadPath);
        dest.writeString(incomingName);
        dest.writeLong(size);
        dest.writeLong(timeStamp);
        dest.writeInt(downloadProgress);
    }

    public static final Parcelable.Creator<FileDescription> CREATOR = new Creator<FileDescription>() {
        @Override
        public FileDescription createFromParcel(Parcel source) {
            String from = source.readString();
            String filePath = source.readString();
            String downloadPath = source.readString();
            String incomingName = source.readString();
            long size = source.readLong();
            long timeStamp = source.readLong();
            int progress = source.readInt();
            FileDescription fd = new FileDescription(from, filePath, size, timeStamp);
            fd.setIncomingName(incomingName);
            fd.setDownloadPath(downloadPath);
            fd.setDownloadProgress(progress);
            return fd;
        }

        @Override
        public FileDescription[] newArray(int size) {
            return new FileDescription[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileDescription that = (FileDescription) o;

        if (size != that.size) return false;
        if (timeStamp != that.timeStamp) return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        if (downloadPath != null ? !downloadPath.equals(that.downloadPath) : that.downloadPath != null)
            return false;
        return incomingName != null ? incomingName.equals(that.incomingName) : that.incomingName == null;

    }

    @Override
    public int hashCode() {
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (downloadPath != null ? downloadPath.hashCode() : 0);
        result = 31 * result + (incomingName != null ? incomingName.hashCode() : 0);
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
        return result;
    }

    public String getFrom() {
        return from;
    }

    public boolean hasImage() {
        return FileUtils.getExtensionFromPath(incomingName) == FileUtils.PNG
                || FileUtils.getExtensionFromPath(incomingName) == FileUtils.JPEG
                || FileUtils.getExtensionFromPath(filePath) == FileUtils.JPEG
                || FileUtils.getExtensionFromPath(filePath) == FileUtils.PNG;
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
