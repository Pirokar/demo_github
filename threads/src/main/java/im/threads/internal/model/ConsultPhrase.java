package im.threads.internal.model;

import android.text.TextUtils;

import androidx.core.util.ObjectsCompat;

import java.util.List;

import im.threads.internal.formatters.SpeechStatus;
import im.threads.internal.opengraph.OGData;
import im.threads.internal.utils.FileUtils;

/**
 * сообщение оператора
 */
public final class ConsultPhrase extends ConsultChatPhrase implements ChatPhrase {

    private final String uuid;
    private final boolean sex;
    private final long timeStamp;
    private final String phrase;
    private final String formattedPhrase;
    private final String consultName;
    private final Quote quote;
    private final String status;
    public OGData ogData;
    public String ogUrl;
    private String providerId; //This this a mfms messageId required for read status updates
    private List<String> providerIds;
    private boolean isAvatarVisible = true;
    private FileDescription fileDescription;
    private boolean isChosen;
    private boolean isRead;
    private final Long threadId;
    private final List<QuickReply> quickReplies;
    private final boolean blockInput;
    //для поиска сообщений в чате
    private boolean found;
    private SpeechStatus speechStatus;

    public ConsultPhrase(String uuid, String providerId, List<String> providerIds, FileDescription fileDescription, Quote quote, String consultName,
                         String phrase, String formattedPhrase, long timeStamp, String consultId, String avatarPath,
                         boolean isRead, String status, boolean sex, Long threadId, List<QuickReply> quickReplies,
                         boolean blockInput, SpeechStatus speechStatus) {

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
        this.threadId = threadId;
        this.quickReplies = quickReplies;
        this.blockInput = blockInput;
        this.speechStatus = speechStatus;
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

    public boolean isBlockInput() {
        return blockInput;
    }

    public boolean getSex() {
        return sex;
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

    public SpeechStatus getSpeechStatus() {
        return speechStatus;
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
        return TextUtils.isEmpty(phrase)
                && !FileUtils.isImage(fileDescription)
                && !FileUtils.isVoiceMessage(fileDescription);
    }

    public boolean isVoiceMessage() {
        return speechStatus != SpeechStatus.UNKNOWN
                || FileUtils.isVoiceMessage(fileDescription);
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
    public Quote getQuote() {
        return quote;
    }

    @Override
    public FileDescription getFileDescription() {
        return fileDescription;
    }

    public void setFileDescription(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
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
                && ObjectsCompat.equals(this.status, consultPhrase.status)
                && ObjectsCompat.equals(this.threadId, consultPhrase.threadId)
                && ObjectsCompat.equals(this.blockInput, consultPhrase.blockInput);
        if (this.fileDescription != null) {
            hasSameContent = hasSameContent && this.fileDescription.hasSameContent(consultPhrase.fileDescription);
        }
        if (this.quote != null) {
            hasSameContent = hasSameContent && this.quote.hasSameContent(consultPhrase.quote);
        }
        return hasSameContent;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        if (otherItem instanceof ConsultPhrase) {
            return ObjectsCompat.equals(this.uuid, ((ConsultPhrase) otherItem).uuid);
        }
        return false;
    }

    @Override
    public Long getThreadId() {
        return threadId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsultPhrase that = (ConsultPhrase) o;
        return sex == that.sex &&
                timeStamp == that.timeStamp &&
                isAvatarVisible == that.isAvatarVisible &&
                isChosen == that.isChosen &&
                isRead == that.isRead &&
                found == that.found &&
                ObjectsCompat.equals(uuid, that.uuid) &&
                ObjectsCompat.equals(providerId, that.providerId) &&
                ObjectsCompat.equals(providerIds, that.providerIds) &&
                ObjectsCompat.equals(phrase, that.phrase) &&
                ObjectsCompat.equals(formattedPhrase, that.formattedPhrase) &&
                ObjectsCompat.equals(consultName, that.consultName) &&
                ObjectsCompat.equals(quote, that.quote) &&
                ObjectsCompat.equals(fileDescription, that.fileDescription) &&
                ObjectsCompat.equals(status, that.status) &&
                ObjectsCompat.equals(quickReplies, that.quickReplies) &&
                ObjectsCompat.equals(blockInput, that.blockInput) &&
                ObjectsCompat.equals(ogData, that.ogData) &&
                ObjectsCompat.equals(ogUrl, that.ogUrl) &&
                ObjectsCompat.equals(threadId, that.threadId);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(uuid, providerId, providerIds, sex, timeStamp, phrase, formattedPhrase, consultName, isAvatarVisible, quote, fileDescription, isChosen, isRead, status, quickReplies, blockInput, found, ogData, ogUrl, threadId);
    }
}
