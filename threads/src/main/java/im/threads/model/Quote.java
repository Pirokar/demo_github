package im.threads.model;

import androidx.annotation.Nullable;

import im.threads.utils.ObjectUtils;

/**
 * Created by yuri on 13.06.2016.
 */
public class Quote {
    private String phraseOwnerTitle;
    private final String text;
    private FileDescription fileDescription;
    private final long timeStamp;
    private boolean isFromConsult;
    @Nullable private String quotedPhraseConsultId;

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

        if (text != null ? !text.equals(quote.text) : quote.text != null) return false;
        return fileDescription != null ? fileDescription.equals(quote.fileDescription) : quote.fileDescription == null;

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

        boolean hasSameContent = ObjectUtils.areEqual(this.uuid, quote.uuid)
                && ObjectUtils.areEqual(this.phraseOwnerTitle, quote.phraseOwnerTitle)
                && ObjectUtils.areEqual(this.text, quote.text)
                && ObjectUtils.areEqual(this.timeStamp, quote.timeStamp);

        if (this.fileDescription != null) {
            hasSameContent = hasSameContent && this.fileDescription.hasSameContent(quote.fileDescription);
        }

        return hasSameContent;
    }
}
