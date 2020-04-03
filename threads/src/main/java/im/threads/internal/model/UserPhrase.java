package im.threads.internal.model;

import android.support.v4.util.ObjectsCompat;
import android.text.TextUtils;

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
    private final Quote quote;
    private long phraseTimeStamp;
    private FileDescription fileDescription;
    private boolean isChosen;
    //для поиска сообщений в чате
    private boolean found;

    public OGData ogData;
    public String ogUrl;

    public UserPhrase(String uuid,
                      String providerId,
                      List<String> providerIds,
                      String phrase,
                      Quote quote,
                      long phraseTimeStamp,
                      FileDescription fileDescription,
                      MessageState sentState) {
        this.uuid = uuid;
        this.providerId = providerId;
        this.providerIds = providerIds;
        this.phrase = phrase;
        this.quote = quote;
        this.phraseTimeStamp = phraseTimeStamp;
        this.fileDescription = fileDescription;
        this.sentState = sentState;
    }

    public UserPhrase(String uuid,
                      String providerId,
                      String phrase,
                      Quote quote,
                      long phraseTimeStamp,
                      FileDescription fileDescription,
                      MessageState sentState) {
        this(uuid, providerId, null, phrase, quote, phraseTimeStamp, fileDescription, sentState);
    }

    public UserPhrase(String phrase,
                      Quote quote,
                      long phraseTimeStamp,
                      FileDescription fileDescription,
                      MessageState sentState) {
        this(UUID.randomUUID().toString(), "tempProviderId: " + UUID.randomUUID().toString(), phrase, quote, phraseTimeStamp, fileDescription, sentState);
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
        return quote;
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
        return TextUtils.isEmpty(phrase) &&
                quote == null &&
                FileUtils.isImage(fileDescription);
    }

    public boolean isOnlyDoc() {
        return TextUtils.isEmpty(phrase) && FileUtils.isDoc(fileDescription);
    }

    public boolean hasFile() {
        return fileDescription != null
                || (quote != null
                && quote.getFileDescription() != null);
    }

    @Override
    public String toString() {
        return "UserPhrase{" +
                "phrase='" + phrase + '\'' +
                ", isChosen=" + isChosen +
                '}' + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserPhrase)) {
            return false;
        }
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
        if (this.quote != null) {
            hasSameContent = hasSameContent && this.quote.hasSameContent(userPhrase.quote);
        }
        return hasSameContent;
    }
}
