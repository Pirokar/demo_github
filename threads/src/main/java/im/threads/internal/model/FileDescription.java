package im.threads.internal.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.ObjectsCompat;

public final class FileDescription implements Parcelable {
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

    public boolean isDownloadError() {
        return downloadError;
    }

    public void setDownloadError(boolean downloadError) {
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
        return ObjectsCompat.equals(from, that.from);

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
        return ObjectsCompat.equals(this.from, fileDescription.from)
                && ObjectsCompat.equals(this.filePath, fileDescription.filePath)
                && ObjectsCompat.equals(this.timeStamp, fileDescription.timeStamp)
                && ObjectsCompat.equals(this.downloadPath, fileDescription.downloadPath)
                && ObjectsCompat.equals(this.size, fileDescription.size)
                && ObjectsCompat.equals(this.incomingName, fileDescription.incomingName)
                && ObjectsCompat.equals(this.downloadProgress, fileDescription.downloadProgress);
    }
}
