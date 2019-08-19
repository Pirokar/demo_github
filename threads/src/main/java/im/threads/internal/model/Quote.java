package im.threads.internal.model;

import android.support.annotation.Nullable;
import android.support.v4.util.ObjectsCompat;

public class Quote {
    private String phraseOwnerTitle;
    private final String text;
    private FileDescription fileDescription;
    private final long timeStamp;
    private boolean isFromConsult;
    @Nullable
    private String quotedPhraseConsultId;

    private String uuid;

    public Quote(String uuid, String phraseOwnerTitle, String text, FileDescription fileDescription, long timeStamp) {
        this.uuid = uuid;
        this.phraseOwnerTitle = phraseOwnerTitle;
        this.text = text;
        this.fileDescription = fileDescription;
        this.timeStamp = timeStamp;
    }

    public void setPhraseOwnerTitle(String phraseOwnerTitle) {
        this.phraseOwnerTitle = phraseOwnerTitle;
    }

    @Nullable
    public String getQuotedPhraseConsultId() {
        return quotedPhraseConsultId;
    }

    public void setQuotedPhraseConsultId(@Nullable String quotedPhraseConsultId) {
        this.quotedPhraseConsultId = quotedPhraseConsultId;
    }

    public boolean isFromConsult() {
        return isFromConsult;
    }

    public void setFromConsult(boolean fromConsult) {
        isFromConsult = fromConsult;
    }

    public void setFileDescription(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
    }

    public FileDescription getFileDescription() {
        return fileDescription;
    }

    public String getPhraseOwnerTitle() {
        return phraseOwnerTitle;
    }

    public String getText() {
        return text;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quote quote = (Quote) o;

        if (!ObjectsCompat.equals(text, quote.text)) return false;
        return ObjectsCompat.equals(fileDescription, quote.fileDescription);

    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (fileDescription != null ? fileDescription.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "phraseOwnerTitle='" + phraseOwnerTitle + '\'' +
                ", text='" + text + '\'' +
                ", fileDescription=" + fileDescription +
                ", timeStamp=" + timeStamp +
                ", isFromConsult=" + isFromConsult +
                ", quotedPhraseConsultId='" + quotedPhraseConsultId + '\'' +
                '}';
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean hasSameContent(Quote quote) {

        if (quote == null) {
            return false;
        }

        boolean hasSameContent = ObjectsCompat.equals(this.uuid, quote.uuid)
                && ObjectsCompat.equals(this.phraseOwnerTitle, quote.phraseOwnerTitle)
                && ObjectsCompat.equals(this.text, quote.text)
                && ObjectsCompat.equals(this.timeStamp, quote.timeStamp);

        if (this.fileDescription != null) {
            hasSameContent = hasSameContent && this.fileDescription.hasSameContent(quote.fileDescription);
        }

        return hasSameContent;
    }
}
