package im.threads.opengraph;

import android.text.TextUtils;

public class OGData {
    public String title;
    public String description;
    public String image;
    public String url;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(title)
                && TextUtils.isEmpty(description)
                && TextUtils.isEmpty(image)
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
                ", image:" + image +
                ", url:" + url + "]";
    }
}
