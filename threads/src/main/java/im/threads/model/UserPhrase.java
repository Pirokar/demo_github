package im.threads.model;

import android.text.TextUtils;

import im.threads.utils.FileUtils;

import java.util.UUID;

import static im.threads.utils.FileUtils.getExtensionFromPath;

/**
 * Created by yuri on 10.06.2016.
 */
public class UserPhrase implements ChatPhrase, IsOnlyImage {
    private String messageId;
    private final String phrase;
    private final boolean withFile;
    private MessageState sentState;
    private final Quote mQuote;
    private boolean isWithQuote;
    private final long phraseTimeStamp;
    private FileDescription fileDescription;
    private boolean isChosen;
    public boolean isCopy = false;


    public UserPhrase(String messageId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription) {
        this.messageId = messageId == null ? "localID: " + UUID.randomUUID().toString() : messageId;
        this.phrase = phrase;
        this.withFile = fileDescription != null;
        this.mQuote = mQuote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.fileDescription = fileDescription;
        sentState = MessageState.STATE_SENDING;
    }

    public UserPhrase(String messageId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription, MessageState sentState) {
        this.messageId = messageId == null ? "localID: " + UUID.randomUUID().toString() : messageId;
        this.phrase = phrase;
        this.withFile = fileDescription != null;
        this.mQuote = mQuote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.fileDescription = fileDescription;
        this.sentState = MessageState.STATE_WAS_READ;
    }

    public boolean isWithPhrase() {
        return !TextUtils.isEmpty(phrase);
    }

    public boolean isCopy() {
        return isCopy;
    }

    public void setCopy(boolean copy) {
        isCopy = copy;
    }

    public boolean hasFile() {
        return fileDescription != null
                || (mQuote != null
                && mQuote.getFileDescription() != null);
    }

    @Override
    public boolean isHighlight() {
        return isChosen;
    }

    @Override
    public void setHighLighted(boolean isHighlight) {
        setChosen(isHighlight);
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(phrase);
    }


    public boolean hasQuote() {
        return mQuote != null;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPhrase)) return false;

        UserPhrase that = (UserPhrase) o;

        if (messageId != null ? !messageId.equals(that.messageId) : that.messageId != null)
            return false;
        return phrase != null ? phrase.equals(that.phrase) : that.phrase == null;

    }

    @Override
    public int hashCode() {
        int result = messageId != null ? messageId.hashCode() : 0;
        result = 31 * result + (phrase != null ? phrase.hashCode() : 0);
        return result;
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
        return fileDescription != null || (mQuote != null && mQuote.getFileDescription() != null);
    }

    public void setFileDescription(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
    }

    public boolean isWithQuote() {
        return mQuote != null;
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

    public boolean isOnlyImage() {
        return fileDescription != null
                && TextUtils.isEmpty(phrase)
                && mQuote == null
                && (getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.JPEG
                || getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.PNG
                || getExtensionFromPath(fileDescription.getIncomingName()) == FileUtils.PNG
                || getExtensionFromPath(fileDescription.getIncomingName()) == FileUtils.JPEG);
    }

    @Override
    public String toString() {
        return "UserPhrase{" +
                "phrase='" + phrase + '\'' +
                ", isChosen=" + isChosen +
                '}' + "\n";
    }
}
