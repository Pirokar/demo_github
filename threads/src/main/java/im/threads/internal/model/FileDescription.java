package im.threads.internal.model;

import android.os.Parcel;
import android.os.Parcelable;

import im.threads.internal.utils.ObjectUtils;

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
    private boolean downloadError = false;

    public FileDescription(String from, String filePath, long size, long timeStamp) {
        this.from = from;
        this.filePath = filePath;
        this.size = size;
        this.timeStamp = timeStamp;
    }

    public boolean isDownlodadError() {
        return downloadError;
    }

    public void setDownlodadError(boolean downloadError) {
        this.downloadError = downloadError;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileDescription)) return false;

        FileDescription that = (FileDescription) o;

        if (size != that.size) return false;
        if (timeStamp != that.timeStamp) return false;
        return from != null ? from.equals(that.from) : that.from == null;

    }

    @Override
    public int hashCode() {
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
        return result;
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

    public String getFrom() {
        return from;
    }

    @Override
    public String toString() {
        return "FileDescription{" +
                "from='" + from + '\'' +
                ", filePath='" + filePath + '\'' +
                ", downloadPath='" + downloadPath + '\'' +
                ", incomingName='" + incomingName + '\'' +
                ", size=" + size +
                ", timeStamp=" + timeStamp +
                ", downloadProgress=" + downloadProgress +
                '}';
    }

    public boolean hasSameContent(FileDescription fileDescription) {

        if (fileDescription == null) {
            return false;
        }

        return ObjectUtils.areEqual(this.from, fileDescription.from)
                && ObjectUtils.areEqual(this.filePath, fileDescription.filePath)
                && ObjectUtils.areEqual(this.timeStamp, fileDescription.timeStamp)
                && ObjectUtils.areEqual(this.downloadPath, fileDescription.downloadPath)
                && ObjectUtils.areEqual(this.size, fileDescription.size)
                && ObjectUtils.areEqual(this.incomingName, fileDescription.incomingName)
                && ObjectUtils.areEqual(this.downloadProgress, fileDescription.downloadProgress);
    }
}
