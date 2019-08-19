package im.threads.internal.model;

public final class BottomGalleryItem {
    private boolean isChosen;
    private String imagePath;

    public BottomGalleryItem(boolean isChosen, String imagePath) {
        this.isChosen = isChosen;
        this.imagePath = imagePath;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
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
