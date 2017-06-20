package im.threads.model;

import android.text.TextUtils;

import im.threads.utils.FileUtils;

/**
 * Created by yuri on 10.06.2016.
 * сообщение оператора
 */
public class ConsultPhrase implements ChatPhrase, IsOnlyImage, ConsultChatPhrase {
    private String avatarPath;
    private final String consultId;
    private final boolean sex;
    private final long timeStamp;
    private final String phrase;
    private final String messageId;
    private final String consultName;
    private boolean isAvatarVisible = true;
    private final Quote quote;
    private FileDescription fileDescription;
    private boolean isChosen;
    private boolean isRead;
    private final String status;
    //для поиска сообщений в чате
    private boolean found;

    private String backendId;

    public ConsultPhrase(
            FileDescription fileDescription
            , Quote quote
            , String consultName
            , String messageId
            , String phrase
            , long timeStamp
            , String consultId
            , String avatarPath
            , boolean isRead
            , String status
            , boolean sex
            , String backendId
    ) {
        this.fileDescription = fileDescription;
        this.quote = quote;
        this.consultName = consultName;
        this.messageId = messageId;
        this.phrase = phrase;
        this.timeStamp = timeStamp;
        this.consultId = consultId;
        this.avatarPath = avatarPath;
        this.isRead = isRead;
        this.status = status;
        this.sex = sex;
        this.backendId = backendId;
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

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getConsultId() {
        return consultId;
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

    public String getAvatarPath() {
        return avatarPath;
    }


    public boolean isAvatarVisible() {
        return isAvatarVisible;
    }

    public void setAvatarVisible(boolean avatarVisible) {
        isAvatarVisible = avatarVisible;
    }

    @Override
    public String getId() {
        return messageId;
    }

    public boolean hasFile() {
        return getFileDescription() != null ||
                (getQuote() != null && getQuote().getFileDescription() != null);
    }

    public boolean isOnlyImage() {
        return fileDescription != null
                && TextUtils.isEmpty(phrase)
                && quote == null
                && (FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.JPEG
                || FileUtils.getExtensionFromPath(fileDescription.getFilePath()) == FileUtils.PNG
                || FileUtils.getExtensionFromPath(fileDescription.getIncomingName()) == FileUtils.PNG
                || FileUtils.getExtensionFromPath(fileDescription.getIncomingName()) == FileUtils.JPEG);
    }

    @Override
    public String getPhraseText() {
        return phrase;
    }

    @Override
    public String toString() {
        return "ConsultPhrase{" +
                "messageId='" + messageId + '\'' +
                ", phrase='" + phrase + '\'' +
                '}';
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

        if (sex != that.sex) return false;
        if (consultId != null ? !consultId.equals(that.consultId) : that.consultId != null)
            return false;
        if (phrase != null ? !phrase.equals(that.phrase) : that.phrase != null) return false;
        /*if (!(messageId != null && that.messageId != null && messageId.equals(that.messageId))) {
            return false;
        }*/
        try {
            Long thisId = Long.parseLong(messageId);
            Long thatId = Long.parseLong(that.messageId);
            if (Math.abs(thisId - thatId) > 5) return false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (quote != null ? !quote.equals(that.quote) : that.quote != null) return false;
        if (fileDescription != null ? !fileDescription.equals(that.fileDescription) : that.fileDescription != null)
            return false;
        return status != null ? status.equals(that.status) : that.status == null;

    }

    @Override
    public int hashCode() {
        int result = consultId != null ? consultId.hashCode() : 0;
        result = 31 * result + (sex ? 1 : 0);
        result = 31 * result + (phrase != null ? phrase.hashCode() : 0);
        result = 31 * result + (quote != null ? quote.hashCode() : 0);
        result = 31 * result + (fileDescription != null ? fileDescription.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    public String getMessageId() {
        return messageId;
    }


    public Quote getQuote() {
        return quote;
    }

    public FileDescription getFileDescription() {
        return fileDescription;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getPhrase() {
        return phrase;
    }

    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }
}
