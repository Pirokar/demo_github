package com.sequenia.threads.model;

/**
 * Created by yuri on 13.06.2016.
 */
public class Quote {
    private final String phraseOwnerTitle;
    private final String text;
    private final FileDescription fileDescription;
    private final long timeStamp;

    public Quote(String phraseOwnerTitle, String text, FileDescription fileDescription, long timeStamp) {
        this.phraseOwnerTitle = phraseOwnerTitle;
        this.text = text;
        this.fileDescription = fileDescription;
        this.timeStamp = timeStamp;
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
    public String toString() {
        return "Quote{" +
                "phraseOwnerTitle='" + phraseOwnerTitle + '\'' +
                ", text='" + text + '\'' +
                ", fileDescription=" + fileDescription +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
