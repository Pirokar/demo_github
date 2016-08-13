package com.sequenia.threads.model;

import java.util.UUID;

/**
 * Created by yuri on 10.06.2016.
 */
public class UserPhrase implements ChatPhrase {
    private String messageId;
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
    public UserPhrase(String messageId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription,MessageState sentState) {
        this.messageId = messageId == null ? UUID.randomUUID().toString() : messageId;
        this.phrase = phrase;
        this.withFile = fileDescription != null;
        this.mQuote = mQuote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.fileDescription = fileDescription;
        this.sentState = MessageState.STATE_SENT_AND_SERVER_RECEIVED;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPhrase)) return false;

        UserPhrase that = (UserPhrase) o;

        if (phrase != null ? !phrase.equals(that.phrase) : that.phrase != null) return false;
        if (mQuote != null ? !mQuote.equals(that.mQuote) : that.mQuote != null) return false;
        return fileDescription != null ? fileDescription.equals(that.fileDescription) : that.fileDescription == null;

    }

    @Override
    public int hashCode() {
        int result = phrase != null ? phrase.hashCode() : 0;
        result = 31 * result + (mQuote != null ? mQuote.hashCode() : 0);
        result = 31 * result + (fileDescription != null ? fileDescription.hashCode() : 0);
        return result;
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
