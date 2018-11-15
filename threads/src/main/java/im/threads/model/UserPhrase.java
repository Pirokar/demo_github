package im.threads.model;

import android.text.TextUtils;

import java.util.UUID;

import im.threads.opengraph.OGData;
import im.threads.utils.FileUtils;

/**
 * Created by yuri on 10.06.2016.
 */
public class UserPhrase implements ChatPhrase, IsOnlyImage {

    private String uuid;
    private String providerId; //This this a mfms messageId required for read status updates
    private final String phrase;
    private final boolean withFile;
    private MessageState sentState;
    private final Quote mQuote;
    private boolean isWithQuote;
    private long phraseTimeStamp;
    private FileDescription fileDescription;
    private boolean isChosen;
    public boolean isCopy = false;
    //для поиска сообщений в чате
    private boolean found;

    public OGData ogData;
    public String ogUrl;

    public UserPhrase(String uuid, String providerId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription, MessageState sentState) {
        this.uuid = uuid;
        this.providerId = providerId;
        this.phrase = phrase;
        this.mQuote = mQuote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.withFile = fileDescription != null;
        this.fileDescription = fileDescription;
        this.sentState = sentState;
    }

    public UserPhrase(String uuid, String providerId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription) {
        this(uuid, providerId, phrase, mQuote, phraseTimeStamp, fileDescription, MessageState.STATE_SENDING);
    }

    public UserPhrase(String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription) {
        this(UUID.randomUUID().toString(), "tempProviderId: " + UUID.randomUUID().toString(),
                phrase, mQuote, phraseTimeStamp, fileDescription, MessageState.STATE_SENDING);
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

        if (!TextUtils.isEmpty(uuid)) {
            return uuid.equals(that.uuid);
        } else {
            return false;
        }

//        if (fileDescription != null && that.fileDescription != null) {
//            return fileDescription.equals(that.fileDescription);
//        }
//
//        return !TextUtils.isEmpty(phrase) ? phrase.equals(that.phrase) : TextUtils.isEmpty(that.phrase);
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
//        result = 31 * result + (phrase != null ? phrase.hashCode() : 0);
        return result;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setTimeStamp(long timestamp) {
        this.phraseTimeStamp = timestamp;
    }

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }

    public Quote getQuote() {
        return mQuote;
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
        return uuid;
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
                && FileUtils.isImage(fileDescription);
    }

    @Override
    public String toString() {
        return "UserPhrase{" +
                "phrase='" + phrase + '\'' +
                ", isChosen=" + isChosen +
                '}' + "\n";
    }
}
