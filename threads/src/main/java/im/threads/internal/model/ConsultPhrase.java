package im.threads.internal.model;

import android.text.TextUtils;

import im.threads.internal.opengraph.OGData;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.ObjectUtils;

/**
 * Created by yuri on 10.06.2016.
 * сообщение оператора
 */
public class ConsultPhrase extends ConsultChatPhrase  implements ChatPhrase, IsOnlyImage {

    private final String uuid;
    private String providerId; //This this a mfms messageId required for read status updates
    private final boolean sex;
    private final long timeStamp;
    private final String phrase;
    private final String consultName;
    private boolean isAvatarVisible = true;
    private final Quote quote;
    private FileDescription fileDescription;
    private boolean isChosen;
    private boolean isRead;
    private final String status;
    //для поиска сообщений в чате
    private boolean found;

    public OGData ogData;
    public String ogUrl;

    public ConsultPhrase(String uuid, String providerId, FileDescription fileDescription, Quote quote, String consultName,
                         String phrase, long timeStamp, String consultId, String avatarPath,
                         boolean isRead, String status, boolean sex) {

        super(avatarPath, consultId);

        this.uuid = uuid;
        this.providerId = providerId;
        this.fileDescription = fileDescription;
        this.quote = quote;
        this.consultName = consultName;
        this.phrase = phrase;
        this.timeStamp = timeStamp;
        this.isRead = isRead;
        this.status = status;
        this.sex = sex;
    }

    public String getUuid() {
        return uuid;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getStatus() {
        return status;
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

    public boolean hasFile() {
        return getFileDescription() != null ||
                (getQuote() != null && getQuote().getFileDescription() != null);
    }

    @Override
    public boolean isOnlyImage() {
        return fileDescription != null
                && TextUtils.isEmpty(phrase)
                && quote == null
                && FileUtils.isImage(fileDescription);
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

//        if (sex != that.sex) {
//            return false;
//        }
//
//        if (consultId != null ? !consultId.equals(that.consultId) : that.consultId != null) {
//            return false;
//        }
//        if (!TextUtils.isEmpty(phrase) ? !phrase.equals(that.phrase) : !TextUtils.isEmpty(that.phrase))  {
//            return false;
//        }
//
//        if (quote != null ? !quote.equals(that.quote) : that.quote != null) {
//            return false;
//        }
//
//        if (fileDescription != null && that.fileDescription != null) {
//            return fileDescription.equals(that.fileDescription);
//        }
//
//        return status != null ? status.equals(that.status) : that.status == null;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
//        int result = consultId != null ? consultId.hashCode() : 0;
//        result = 31 * result + (sex ? 1 : 0);
//        result = 31 * result + (phrase != null ? phrase.hashCode() : 0);
//        result = 31 * result + (quote != null ? quote.hashCode() : 0);
//        result = 31 * result + (fileDescription != null ? fileDescription.hashCode() : 0);
//        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
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

    public boolean hasSameContent(ConsultPhrase consultPhrase) {

        if (consultPhrase == null) {
            return false;
        }

        boolean hasSameContent = ObjectUtils.areEqual(this.uuid, consultPhrase.uuid)
                && ObjectUtils.areEqual(this.phrase, consultPhrase.phrase)
                && ObjectUtils.areEqual(this.providerId, consultPhrase.providerId)
                && ObjectUtils.areEqual(this.timeStamp, consultPhrase.timeStamp)
                && ObjectUtils.areEqual(this.isRead, consultPhrase.isRead)
                && ObjectUtils.areEqual(this.getAvatarPath(), consultPhrase.getAvatarPath())
                && ObjectUtils.areEqual(this.consultId, consultPhrase.consultId)
                && ObjectUtils.areEqual(this.consultName, consultPhrase.consultName)
                && ObjectUtils.areEqual(this.sex, consultPhrase.sex)
                && ObjectUtils.areEqual(this.status, consultPhrase.status);

        if (this.fileDescription != null) {
            hasSameContent = hasSameContent && this.fileDescription.hasSameContent(consultPhrase.fileDescription);
        }

        if (this.quote != null) {
            hasSameContent = hasSameContent && this.quote.hasSameContent(consultPhrase.quote);
        }

        return hasSameContent;
    }
}
