package com.sequenia.threads.model;

import java.util.UUID;

/**
 * Created by yuri on 10.06.2016.
 */
public class ConsultPhrase implements ChatPhrase {
    private final String avatarPath;
    private final String filePath;
    private final long timeStamp;
    private final String phrase;
    private final String messageId;
    private int downloadingProgress;
    private final String consultName;
    private boolean isAvatarVisible = true;
    private final Quote quote;
    private final FileDescription fileDescription;

    public ConsultPhrase(String avatarPath, String filePath, long timeStamp, String phrase, String messageId, String consultName, Quote quote, FileDescription fileDescription) {
        this.avatarPath = avatarPath;
        this.filePath = filePath;
        this.timeStamp = timeStamp;
        this.phrase = phrase;
        this.messageId = messageId == null ? UUID.randomUUID().toString() : messageId;
        this.consultName = consultName;
        this.quote = quote;
        this.fileDescription = fileDescription;

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

    public int getDownloadingProgress() {
        return downloadingProgress;
    }

    public void setDownloadingProgress(int downloadingProgress) {
        this.downloadingProgress = downloadingProgress;
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
                ", filePath='" + filePath + '\'' +
                ", timeStamp=" + timeStamp +
                ", phrase='" + phrase + '\'' +
                ", messageId=" + messageId +
                ", downloadingProgress=" + downloadingProgress +
                ", consultName='" + consultName + '\'' +
                ", isAvatarVisible=" + isAvatarVisible +
                ", quote=" + quote +
                ", fileDescription=" + fileDescription +
                '}';
    }

    public String getMessageId() {
        return messageId;
    }

    public String getFilePath() {
        return filePath;
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
