package im.threads.internal.model;

import android.net.Uri;

public final class BottomGalleryItem {
    private boolean isChosen;
    private Uri imagePath;

    public BottomGalleryItem(boolean isChosen, Uri imagePath) {
        this.isChosen = isChosen;
        this.imagePath = imagePath;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public Uri getImagePath() {
        return imagePath;
    }

    public void setImagePath(Uri imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "BottomGalleryItem{" +
                "isChosen=" + isChosen +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
