package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public class UserPhrase implements ChatPhrase {
    private final long messageId;
    private final String phrase;
    private final boolean withFile;
    private MessageState sentState;
    private final Quote mQuote;
    private boolean isWithQuote;
    private final long phraseTimeStamp;
    private final FileDescription fileDescription;
    private final String filePath;


    public UserPhrase(long messageId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription, String filePath) {
        this.messageId = messageId;
        this.phrase = phrase;
        this.withFile = filePath != null;
        this.mQuote = mQuote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.fileDescription = fileDescription;
        this.filePath = filePath;
    }

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }

    public Quote getQuote() {
        return mQuote;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getMessageId() {
        return messageId;
    }

    public String getPhrase() {
        return phrase;
    }

    public boolean isWithFile() {
        return withFile;
    }

    public boolean isWithQuote() {
        return isWithQuote;
    }

    public MessageState getSentState() {
        return sentState;
    }

    public void setSentState(MessageState sentState) {
        this.sentState = sentState;
    }

    public FileDescription getFileDescription() {
        return fileDescription;
    }

    @Override
    public long getId() {
        return messageId;
    }

    @Override
    public String getPhraseText() {
        return phrase;
    }

    @Override
    public String toString() {
        return "UserPhrase{" +
                "messageId=" + messageId +
                ", phrase='" + phrase + '\'' +
                ", withFile=" + withFile +
                ", sentState=" + sentState +
                ", mQuote=" + mQuote +
                ", isWithQuote=" + isWithQuote +
                ", phraseTimeStamp=" + phraseTimeStamp +
                ", fileDescription=" + fileDescription +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
