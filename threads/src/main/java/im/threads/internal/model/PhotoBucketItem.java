package im.threads.internal.model;

public class PhotoBucketItem {
    private final String bucketName;
    private final String bucketSize;
    private final String imagePath;

    public PhotoBucketItem(String bucketName, String bucketSize, String imagePath) {
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

    public String getImagePath() {
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
