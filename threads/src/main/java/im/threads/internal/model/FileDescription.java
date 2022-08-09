package im.threads.internal.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;

public final class FileDescription implements Parcelable {
    public static final Parcelable.Creator<FileDescription> CREATOR = new Creator<>() {
        @Override
        public FileDescription createFromParcel(Parcel source) {
            String state = source.readString();
            String errorCode = source.readString();
            String errorMessage = source.readString();
            String from = source.readString();
            Uri filePath = source.readParcelable(Uri.class.getClassLoader());
            String downloadPath = source.readString();
            String incomingName = source.readString();
            String originalPath = source.readString();
            String mimeType = source.readString();
            long size = source.readLong();
            long timeStamp = source.readLong();
            int progress = source.readInt();
            boolean selfie = source.readInt() == 1;
            FileDescription fd = new FileDescription(from, filePath, size, timeStamp);
            fd.setState(AttachmentStateEnum.valueOf(state));
            fd.setErrorCode(ErrorStateEnum.valueOf(errorCode));
            fd.setErrorMessage(errorMessage);
            fd.setIncomingName(incomingName);
            fd.setMimeType(mimeType);
            fd.setDownloadPath(downloadPath);
            fd.setOriginalPath(originalPath);
            fd.setDownloadProgress(progress);
            return fd;
        }

        @Override
        public FileDescription[] newArray(int size) {
            return new FileDescription[size];
        }
    };
    private long size;
    private String from;
    private Uri fileUri;
    private long timeStamp;
    private String downloadPath;
    private String originalPath;
    private String incomingName;
    private String mimeType = null;
    private int downloadProgress;
    private boolean downloadError = false;
    private AttachmentStateEnum state = AttachmentStateEnum.ANY;
    private ErrorStateEnum errorCode = ErrorStateEnum.ANY;
    private String errorMessage = "";

    public FileDescription(String from, Uri fileUri, long size, long timeStamp) {
        this.from = from;
        this.fileUri = fileUri;
        this.size = size;
        this.timeStamp = timeStamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String header) {
        this.from = header;
    }

    @Nullable
    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Nullable
    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    @Nullable
    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getIncomingName() {
        return incomingName;
    }

    public void setIncomingName(String incomingName) {
        this.incomingName = incomingName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public boolean isDownloadError() {
        return downloadError;
    }

    public void setDownloadError(boolean downloadError) {
        this.downloadError = downloadError;
    }

    public AttachmentStateEnum getState() {
        return state;
    }

    public void setState(AttachmentStateEnum state) {
        this.state = state;
    }

    public ErrorStateEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorStateEnum errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileDescription)) return false;

        FileDescription that = (FileDescription) o;

        if (size != that.size) return false;
        if (timeStamp != that.timeStamp) return false;
        if (!ObjectsCompat.equals(mimeType, that.mimeType)) return false;
        return ObjectsCompat.equals(from, that.from);

    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(size, timeStamp, mimeType, from);
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(state.getState());
        dest.writeString(errorCode.getState());
        dest.writeString(errorMessage);
        dest.writeString(from);
        dest.writeParcelable(fileUri, 0);
        dest.writeString(downloadPath);
        dest.writeString(incomingName);
        dest.writeString(originalPath);
        dest.writeString(mimeType);
        dest.writeLong(size);
        dest.writeLong(timeStamp);
        dest.writeInt(downloadProgress);
    }

    @Override
    public String toString() {
        return "FileDescription{" +
                "from='" + from + '\'' +
                ", filePath='" + fileUri + '\'' +
                ", downloadPath='" + downloadPath + '\'' +
                ", incomingName='" + incomingName + '\'' +
                ", size=" + size +
                ", timeStamp=" + timeStamp +
                ", downloadProgress=" + downloadProgress +
                ", state=" + state.getState() +
                ", errorCode=" + errorCode.getState() +
                ", errorMessage=" + errorMessage +
                '}';
    }

    public boolean hasSameContent(FileDescription fileDescription) {
        if (fileDescription == null) {
            return false;
        }
        return ObjectsCompat.equals(this.from, fileDescription.from)
                && ObjectsCompat.equals(this.fileUri, fileDescription.fileUri)
                && ObjectsCompat.equals(this.timeStamp, fileDescription.timeStamp)
                && ObjectsCompat.equals(this.downloadPath, fileDescription.downloadPath)
                && ObjectsCompat.equals(this.size, fileDescription.size)
                && ObjectsCompat.equals(this.incomingName, fileDescription.incomingName)
                && ObjectsCompat.equals(this.mimeType, fileDescription.mimeType)
                && ObjectsCompat.equals(this.downloadProgress, fileDescription.downloadProgress);
    }
}
