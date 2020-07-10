package im.threads.internal.model;

import android.net.Uri;

public final class MediaPhoto {
    private final Uri imageUri;
    private final String displayName;
    private final String bucketName;
    private boolean isChecked;

    public MediaPhoto(Uri imageUri, String displayName, String bucketName) {
        this.imageUri = imageUri;
        this.displayName = displayName;
        this.bucketName = bucketName;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public String toString() {
        return "MediaPhoto{" +
                "imagePath='" + imageUri + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }
}
