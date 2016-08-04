package com.sequenia.threads.model;

import java.util.UUID;

/**
 * Created by yuri on 10.06.2016.
 */
public class UserPhrase implements ChatPhrase {
    private final String messageId;
    private final String phrase;
    private final boolean withFile;
    private MessageState sentState;
    private final Quote mQuote;
    private boolean isWithQuote;
    private final long phraseTimeStamp;
    private FileDescription fileDescription;
    private boolean isChosen;


    public UserPhrase(String messageId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription) {
        this.messageId = messageId == null ? UUID.randomUUID().toString() : messageId;
        this.phrase = phrase;
        this.withFile = fileDescription != null;
        this.mQuote = mQuote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.fileDescription = fileDescription;
        sentState = MessageState.STATE_SENT;
    }

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }

    public Quote getQuote() {
        return mQuote;
    }


    public String getMessageId() {
        return messageId;
    }

    public String getPhrase() {
        return phrase;
    }

    public boolean isWithFile() {
        return withFile;
    }

    public void setFileDescription(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
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
    public String getId() {
        return messageId;
    }

    @Override
    public String getPhraseText() {
        return phrase;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    @Override
    public String toString() {
        return "UserPhrase{" +
                "messageId='" + messageId + '\'' +
                ", phrase='" + phrase + '\'' +
                ", withFile=" + withFile +
                ", sentState=" + sentState +
                ", mQuote=" + mQuote +
                ", isWithQuote=" + isWithQuote +
                ", phraseTimeStamp=" + phraseTimeStamp +
                ", fileDescription=" + fileDescription +
                ", isChosen=" + isChosen +
                '}';
    }
}
