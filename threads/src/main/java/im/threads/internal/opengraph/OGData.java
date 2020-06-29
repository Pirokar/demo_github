package im.threads.internal.opengraph;

import android.text.TextUtils;

public final class OGData {
    private String title;
    private String description;
    private String imageUrl;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(title)
                && TextUtils.isEmpty(description)
                && TextUtils.isEmpty(imageUrl)
                && TextUtils.isEmpty(url);
    }

    public boolean areTextsEmpty() {
        return TextUtils.isEmpty(title)
                && TextUtils.isEmpty(description)
                && TextUtils.isEmpty(url);
    }

    public String toString() {
        return "Open Graph Data:" +
                "[title:" + title +
                ", desc:" + description +
                ", imageUrl:" + imageUrl +
                ", url:" + url + "]";
    }

}
