package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public class ConsultPhrase implements ChatPhrase {
    private final String avatarPath;
    private final String filePath;
    private final long timeStamp;
    private final String consultPhrase;
    private final long messageId;
    private int downloadingProgress;
    private final String consultName;
    private boolean isAvatarVisible = true;
    private final Quote quote;
    private final FileDescription fileDescription;

    public ConsultPhrase(String avatarPath, String filePath, long timeStamp, String consultPhrase, long messageId, String consultName, Quote quote, FileDescription fileDescription) {
        this.avatarPath = avatarPath;
        this.filePath = filePath;
        this.timeStamp = timeStamp;
        this.consultPhrase = consultPhrase;
        this.messageId = messageId;
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
    public long getId() {
        return messageId;
    }

    @Override
    public String getPhraseText() {
        return consultPhrase;
    }

    @Override
    public String toString() {
        return "ConsultPhrase{" +
                "avatarPath='" + avatarPath + '\'' +
                ", filePath='" + filePath + '\'' +
                ", timeStamp=" + timeStamp +
                ", consultPhrase='" + consultPhrase + '\'' +
                ", messageId=" + messageId +
                ", downloadingProgress=" + downloadingProgress +
                ", consultName='" + consultName + '\'' +
                ", isAvatarVisible=" + isAvatarVisible +
                ", quote=" + quote +
                ", fileDescription=" + fileDescription +
                '}';
    }

    public long getMessageId() {
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

    public String getConsultPhrase() {
        return consultPhrase;
    }
}
