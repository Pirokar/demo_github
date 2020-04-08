package im.threads.internal.opengraph;

import android.text.TextUtils;

public final class OGData {
    public final String title;
    public final String description;
    public final String image;
    public final String url;

    private OGData(String title, String description, String image, String url) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.url = url;
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

    public static final class Builder {
        private String title;
        private String description;
        private String image;
        private String url;

        public Builder() {
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public OGData build() {
            return new OGData(title, description, image, url);
        }
    }
}
