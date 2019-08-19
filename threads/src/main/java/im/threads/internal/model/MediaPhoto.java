package im.threads.internal.model;

public final class MediaPhoto {
    private String imagePath;
    private String bucketName;
    private boolean isChecked;

    public MediaPhoto(String imagePath, String bucketName) {
        this.imagePath = imagePath;
        this.bucketName = bucketName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
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
                "imagePath='" + imagePath + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }
}
