package com.sequenia.threads.model;

import java.util.UUID;

/**
 * Created by yuri on 10.06.2016.
 */
public class ConsultPhrase implements ChatPhrase {
    private final String avatarPath;
    private final long timeStamp;
    private final String phrase;
    private final String messageId;
    private final String consultName;
    private boolean isAvatarVisible = true;
    private final Quote quote;
    private final FileDescription fileDescription;
    private boolean isChosen;

    public ConsultPhrase(String avatarPath, long timeStamp, String phrase, String messageId, String consultName, Quote quote, FileDescription fileDescription) {
        this.avatarPath = avatarPath;
        this.timeStamp = timeStamp;
        this.phrase = phrase;
        this.messageId = messageId == null ? UUID.randomUUID().toString() : messageId;
        this.consultName = consultName;
        this.quote = quote;
        this.fileDescription = fileDescription;

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

    @Override
    public String getPhraseText() {
        return phrase;
    }

    @Override
    public String toString() {
        return "ConsultPhrase{" +
                "avatarPath='" + avatarPath + '\'' +
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
