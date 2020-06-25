package im.threads.internal.model;

import android.text.TextUtils;

import androidx.core.util.ObjectsCompat;

import java.util.List;

import im.threads.internal.opengraph.OGData;
import im.threads.internal.utils.FileUtils;

/**
 * сообщение оператора
 */
public final class ConsultPhrase extends ConsultChatPhrase implements ChatPhrase {

    private final String uuid;
    private String providerId; //This this a mfms messageId required for read status updates
    private List<String> providerIds;
    private final boolean sex;
    private final long timeStamp;
    private final String phrase;
    private final String formattedPhrase;
    private final String consultName;
    private boolean isAvatarVisible = true;
    private final Quote quote;
    private FileDescription fileDescription;
    private boolean isChosen;
    private boolean isRead;
    private final String status;
    private List<QuickReply> quickReplies;
    //для поиска сообщений в чате
    private boolean found;

    public OGData ogData;
    public String ogUrl;

    public ConsultPhrase(String uuid, String providerId, List<String> providerIds, FileDescription fileDescription, Quote quote, String consultName,
                         String phrase, String formattedPhrase, long timeStamp, String consultId, String avatarPath,
                         boolean isRead, String status, boolean sex, List<QuickReply> quickReplies) {

        super(avatarPath, consultId);

        this.uuid = uuid;
        this.providerId = providerId;
        this.providerIds = providerIds;
        this.fileDescription = fileDescription;
        this.quote = quote;
        this.consultName = consultName;
        this.phrase = phrase;
        this.formattedPhrase = formattedPhrase;
        this.timeStamp = timeStamp;
        this.isRead = isRead;
        this.status = status;
        this.sex = sex;
        this.quickReplies = quickReplies;
    }

    public String getUuid() {
        return uuid;
    }

    public String getProviderId() {
        return providerId;
    }

    public List<String> getProviderIds() {
        return providerIds;
    }

    public String getStatus() {
        return status;
    }

    public List<QuickReply> getQuickReplies() {
        return quickReplies;
    }

    public boolean getSex() {
        return sex;
    }

    public void setFileDescription(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public long getDate() {
        return timeStamp;
    }

    public String getConsultName() {
        return consultName;
    }

    public boolean isAvatarVisible() {
        return isAvatarVisible;
    }

    public void setAvatarVisible(boolean avatarVisible) {
        isAvatarVisible = avatarVisible;
    }

    @Override
    public String getId() {
        return uuid;
    }

    public boolean isOnlyImage() {
        return TextUtils.isEmpty(phrase)
                && quote == null
                && FileUtils.isImage(fileDescription);
    }

    public boolean isOnlyDoc() {
        return TextUtils.isEmpty(phrase) && FileUtils.isDoc(fileDescription);
    }

    @Override
    public String getPhraseText() {
        return phrase;
    }

    @Override
    public boolean isHighlight() {
        return isChosen;
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
    public void setHighLighted(boolean isHighlighted) {
        isChosen = isHighlighted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsultPhrase)) return false;

        ConsultPhrase that = (ConsultPhrase) o;

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

    @Override
    public Quote getQuote() {
        return quote;
    }

    @Override
    public FileDescription getFileDescription() {
        return fileDescription;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public String getPhrase() {
        return phrase;
    }

    public String getFormattedPhrase() {
        return formattedPhrase;
    }

    public boolean hasSameContent(ConsultPhrase consultPhrase) {

        if (consultPhrase == null) {
            return false;
        }

        boolean hasSameContent = ObjectsCompat.equals(this.uuid, consultPhrase.uuid)
                && ObjectsCompat.equals(this.phrase, consultPhrase.phrase)
                && ObjectsCompat.equals(this.formattedPhrase, consultPhrase.formattedPhrase)
                && ObjectsCompat.equals(this.providerId, consultPhrase.providerId)
                && ObjectsCompat.equals(this.timeStamp, consultPhrase.timeStamp)
                && ObjectsCompat.equals(this.isRead, consultPhrase.isRead)
                && ObjectsCompat.equals(this.getAvatarPath(), consultPhrase.getAvatarPath())
                && ObjectsCompat.equals(this.consultId, consultPhrase.consultId)
                && ObjectsCompat.equals(this.consultName, consultPhrase.consultName)
                && ObjectsCompat.equals(this.sex, consultPhrase.sex)
                && ObjectsCompat.equals(this.status, consultPhrase.status);

        if (this.fileDescription != null) {
            hasSameContent = hasSameContent && this.fileDescription.hasSameContent(consultPhrase.fileDescription);
        }

        if (this.quote != null) {
            hasSameContent = hasSameContent && this.quote.hasSameContent(consultPhrase.quote);
        }

        return hasSameContent;
    }
}
