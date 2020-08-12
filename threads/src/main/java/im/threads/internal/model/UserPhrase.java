package im.threads.internal.model;

import android.text.TextUtils;

import androidx.core.util.ObjectsCompat;

import java.util.List;
import java.util.UUID;

import im.threads.internal.opengraph.OGData;
import im.threads.internal.utils.FileUtils;

public final class UserPhrase implements ChatPhrase {
    private String uuid;
    private String providerId; //This this a mfms messageId required for read status updates
    private List<String> providerIds;
    private final String phrase;
    private MessageState sentState;
    private final Quote mQuote;
    private long phraseTimeStamp;
    private FileDescription fileDescription;
    private boolean isChosen;
    private boolean isCopy = false;
    //для поиска сообщений в чате
    private boolean found;

    public OGData ogData;
    public String ogUrl;

    public UserPhrase(String uuid, String providerId, List<String> providerIds, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription, MessageState sentState) {
        this.uuid = uuid;
        this.providerId = providerId;
        this.providerIds = providerIds;
        this.phrase = phrase;
        this.mQuote = mQuote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.fileDescription = fileDescription;
        this.sentState = sentState;
    }

    public UserPhrase(String uuid, String providerId, String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription) {
        this(uuid, providerId, null, phrase, mQuote, phraseTimeStamp, fileDescription, MessageState.STATE_SENDING);
    }

    public UserPhrase(String phrase, Quote mQuote, long phraseTimeStamp, FileDescription fileDescription) {
        this(UUID.randomUUID().toString(), "tempProviderId: " + UUID.randomUUID().toString(), null,
                phrase, mQuote, phraseTimeStamp, fileDescription, MessageState.STATE_SENDING);
    }

    public boolean isCopy() {
        return isCopy;
    }

    public void setCopy(boolean copy) {
        isCopy = copy;
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
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
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

    public List<String> getProviderIds() {
        return providerIds;
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

    public void setFileDescription(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
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
        return TextUtils.isEmpty(phrase)
                && mQuote == null
                && FileUtils.isImage(fileDescription);
    }

    public boolean isOnlyDoc() {
        return TextUtils.isEmpty(phrase) && !FileUtils.isImage(fileDescription);
    }

    public boolean hasFile() {
        return fileDescription != null
                || (mQuote != null
                && mQuote.getFileDescription() != null);
    }

    @Override
    public String toString() {
        return "UserPhrase{" +
                "phrase='" + phrase + '\'' +
                ", isChosen=" + isChosen +
                '}' + "\n";
    }

    public boolean hasSameContent(UserPhrase userPhrase) {

        if (userPhrase == null) {
            return false;
        }

        boolean hasSameContent = ObjectsCompat.equals(this.uuid, userPhrase.uuid)
                && ObjectsCompat.equals(this.phrase, userPhrase.phrase)
                && ObjectsCompat.equals(this.providerId, userPhrase.providerId)
                && ObjectsCompat.equals(this.phraseTimeStamp, userPhrase.phraseTimeStamp)
                && ObjectsCompat.equals(this.sentState, userPhrase.sentState);

        if (this.fileDescription != null) {
            hasSameContent = hasSameContent && this.fileDescription.hasSameContent(userPhrase.fileDescription);
        }

        if (this.mQuote != null) {
            hasSameContent = hasSameContent && this.mQuote.hasSameContent(userPhrase.mQuote);
        }

        return hasSameContent;
    }
}
