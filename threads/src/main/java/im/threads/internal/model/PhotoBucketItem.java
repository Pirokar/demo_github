package im.threads.internal.model;

import android.net.Uri;

public final class PhotoBucketItem {
    private final String bucketName;
    private final String bucketSize;
    private final Uri imagePath;

    public PhotoBucketItem(String bucketName, String bucketSize, Uri imagePath) {
        this.bucketName = bucketName;
        this.bucketSize = bucketSize;
        this.imagePath = imagePath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getBucketSize() {
        return bucketSize;
    }

    public Uri getImagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        return "PhotoBucketItem{" +
                "bucketName='" + bucketName + '\'' +
                ", bucketSize='" + bucketSize + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
