package im.threads.model;

import android.text.TextUtils;

import java.util.UUID;

import im.threads.utils.FileUtils;

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
    //для поиска сообщений в чате
    private boolean found;

    private String backendId;


    public UserPhrase(String messageId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription, String backendId) {
        this.messageId = messageId == null ? "localID: " + UUID.randomUUID().toString() : messageId;
        this.phrase = phrase;
        this.withFile = fileDescription != null;
        this.mQuote = mQuote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.fileDescription = fileDescription;
        sentState = MessageState.STATE_SENDING;
        this.backendId = backendId;
    }

    public UserPhrase(String messageId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription, MessageState sentState, String backendId) {
        this.messageId = messageId == null ? "localID: " + UUID.randomUUID().toString() : messageId;
        this.phrase = phrase;
        this.withFile = fileDescription != null;
        this.mQuote = mQuote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.fileDescription = fileDescription;
        this.sentState = MessageState.STATE_WAS_READ;
        this.backendId = backendId;
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

    @Override
    public boolean isFound() {
        return found;
    }

    @Override
    public void setFound(boolean found) {
        this.found = found;
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

        if (backendId != null && that.backendId != null) {
            return backendId.equals(that.backendId);
        }

        if (messageId != null && that.messageId != null) {
            return messageId.equals(that.messageId);
        }

        if (fileDescription != null && that.fileDescription != null) {
            return fileDescription.equals(that.fileDescription);
        }

        return !TextUtils.isEmpty(phrase) ? phrase.equals(that.phrase) : TextUtils.isEmpty(that.phrase);
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

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }
}
