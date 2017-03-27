package im.threads.model;

import android.support.annotation.Nullable;

/**
 * Created by yuri on 13.06.2016.
 */
public class Quote {
    private String phraseOwnerTitle;
    private final String text;
    private FileDescription fileDescription;
    private final long timeStamp;
    private boolean isFromConsult;
    @Nullable private String quotedPhraseId;

    public Quote(String phraseOwnerTitle, String text, FileDescription fileDescription, long timeStamp) {
        this.phraseOwnerTitle = phraseOwnerTitle;
        this.text = text;
        this.fileDescription = fileDescription;
        this.timeStamp = timeStamp;
    }

    public void setPhraseOwnerTitle(String phraseOwnerTitle) {
        this.phraseOwnerTitle = phraseOwnerTitle;
    }

    @Nullable
    public String getQuotedPhraseId() {
        return quotedPhraseId;
    }

    public void setQuotedPhraseId(@Nullable String quotedPhraseId) {
        this.quotedPhraseId = quotedPhraseId;
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
                ", quotedPhraseId='" + quotedPhraseId + '\'' +
                '}';
    }
}
