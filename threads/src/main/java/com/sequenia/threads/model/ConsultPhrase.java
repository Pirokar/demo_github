package com.sequenia.threads.model;

import java.util.UUID;

/**
 * Created by yuri on 10.06.2016.
 */
public class ConsultPhrase implements ChatPhrase {
    private final String avatarPath;
    private final String consultId;
    private final long timeStamp;
    private final String phrase;
    private final String messageId;
    private final String consultName;
    private boolean isAvatarVisible = true;
    private final Quote quote;
    private FileDescription fileDescription;
    private boolean isChosen;

    public ConsultPhrase(FileDescription fileDescription, Quote quote, String consultName, String messageId, String phrase, long timeStamp, String consultId, String avatarPath) {
        this.fileDescription = fileDescription;
        this.quote = quote;
        this.consultName = consultName;
        this.messageId = messageId;
        this.phrase = phrase;
        this.timeStamp = timeStamp;
        this.consultId = consultId;
        this.avatarPath = avatarPath;
    }

    public void setFileDescription(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
    }

    public String getConsultId() {
        return consultId;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public long getDate() {
        return timeStamp;
    }

    public String getConsultName() {
        return consultName;
    }

    public String getAvatarPath() {
        return avatarPath;
    }


    public boolean isAvatarVisible() {
        return isAvatarVisible;
    }

    public void setAvatarVisible(boolean avatarVisible) {
        isAvatarVisible = avatarVisible;
    }

    @Override
    public String getId() {
        return messageId;
    }

    public boolean hasFile() {
        return getFileDescription() != null ||
                (getQuote() != null && getQuote().getFileDescription() != null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultPhrase)) return false;

        ConsultPhrase that = (ConsultPhrase) o;

        if (timeStamp != that.timeStamp) return false;
        if (consultId != null ? !consultId.equals(that.consultId) : that.consultId != null)
            return false;
        if (phrase != null ? !phrase.equals(that.phrase) : that.phrase != null) return false;
        if (messageId != null ? !messageId.equals(that.messageId) : that.messageId != null)
            return false;
        if (consultName != null ? !consultName.equals(that.consultName) : that.consultName != null)
            return false;
        if (quote != null ? !quote.equals(that.quote) : that.quote != null) return false;
        return fileDescription != null ? fileDescription.equals(that.fileDescription) : that.fileDescription == null;

    }

    @Override
    public int hashCode() {
        int result = consultId != null ? consultId.hashCode() : 0;
        result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
        result = 31 * result + (phrase != null ? phrase.hashCode() : 0);
        result = 31 * result + (messageId != null ? messageId.hashCode() : 0);
        result = 31 * result + (consultName != null ? consultName.hashCode() : 0);
        result = 31 * result + (quote != null ? quote.hashCode() : 0);
        result = 31 * result + (fileDescription != null ? fileDescription.hashCode() : 0);
        return result;
    }

    @Override
    public String getPhraseText() {
        return phrase;
    }

    @Override
    public String toString() {
        return "ConsultPhrase{" +
                "avatarPath='" + avatarPath + '\'' +
                ", consultId='" + consultId + '\'' +
                ", timeStamp=" + timeStamp +
                ", phrase='" + phrase + '\'' +
                ", messageId='" + messageId + '\'' +
                ", consultName='" + consultName + '\'' +
                ", isAvatarVisible=" + isAvatarVisible +
                ", quote=" + quote +
                ", fileDescription=" + fileDescription +
                ", isChosen=" + isChosen +
                '}';
    }

    public String getMessageId() {
        return messageId;
    }


    public Quote getQuote() {
        return quote;
    }

    public FileDescription getFileDescription() {
        return fileDescription;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getPhrase() {
        return phrase;
    }
}
