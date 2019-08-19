package im.threads.internal.formatters;

import android.content.Context;
import android.support.v4.util.ObjectsCompat;

import java.util.List;

import im.threads.R;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.utils.Tuple;

import static android.text.TextUtils.isEmpty;
import static im.threads.internal.utils.FileUtils.JPEG;
import static im.threads.internal.utils.FileUtils.PDF;
import static im.threads.internal.utils.FileUtils.PNG;
import static im.threads.internal.utils.FileUtils.getExtensionFromFileDescription;

public class NougatMessageFormatter {
    private Context ctx;
    List<ChatItem> unreadMessages;
    List<ChatItem> incomingPushes;

    public NougatMessageFormatter(Context ctx, List<ChatItem> unreadMessages, List<ChatItem> incomingPushes) {
        this.ctx = ctx;
        this.unreadMessages = unreadMessages;
        this.incomingPushes = incomingPushes;
    }

    public Tuple<Boolean, PushContents> getFormattedMessageAsPushContents() {
        int imagesCount = 0;
        int plainFilesCount = 0;
        int overallPhrasesCount = 0;
        boolean isNeedAnswer = false;
        String avatarPath = null;
        String imagePath = null;
        String phrase = "";
        String name = "";
        boolean sex = false;
        String docName = "";
        for (ChatItem ci : incomingPushes) {
            if (ci instanceof ConsultConnectionMessage || ci instanceof ConsultPhrase)
                unreadMessages.add(ci);
        }
        for (ChatItem ci : unreadMessages) {
            if (ci instanceof ConsultConnectionMessage) {
                ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                name = ccm.getName();
                overallPhrasesCount++;
                phrase = ConnectionPhrase.getConnectionPhrase(ctx, ccm);
                sex = ccm.getSex();
                avatarPath = ((ConsultConnectionMessage) ci).getAvatarPath();
            }
            if (ci instanceof ConsultPhrase) {
                ConsultPhrase consultPhrase = (ConsultPhrase) ci;
                isNeedAnswer = true;
                overallPhrasesCount++;
                if (!isEmpty(consultPhrase.getPhrase())) {
                    phrase = consultPhrase.getPhrase();
                }
                sex = consultPhrase.getSex();
                if (consultPhrase.getConsultName() != null) name = consultPhrase.getConsultName();
                if (consultPhrase.getFileDescription() != null) {
                    FileDescription fileDescription = consultPhrase.getFileDescription();
                    if (getExtensionFromFileDescription(fileDescription) == PNG
                            || getExtensionFromFileDescription(fileDescription) == JPEG) {
                        imagesCount++;
                        imagePath = fileDescription.getDownloadPath();
                    } else if (getExtensionFromFileDescription(fileDescription) == PDF) {
                        plainFilesCount++;
                        docName = fileDescription.getIncomingName();
                    }
                }
                if (consultPhrase.getQuote() != null
                        && consultPhrase.getQuote().getFileDescription() != null) {
                    FileDescription fileDescription = consultPhrase.getQuote().getFileDescription();
                    if (getExtensionFromFileDescription(fileDescription) == PNG
                            || getExtensionFromFileDescription(fileDescription) == JPEG) {
                        imagesCount++;
                        imagePath = fileDescription.getDownloadPath();
                    } else if (getExtensionFromFileDescription(fileDescription) == PDF) {
                        plainFilesCount++;
                        docName = fileDescription.getIncomingName();
                    }
                }
                avatarPath = consultPhrase.getAvatarPath();
            }
        }
        String titletext = name;
        if (imagesCount != 0 && plainFilesCount == 0) {
            if (isEmpty(phrase)) phrase = ctx.getString(R.string.threads_touch_to_look);
            String send = sex ? ctx.getString(R.string.threads_send_male) : ctx.getString(R.string.threads_send_female);
            titletext += " " +
                    send +
                    " " +
                    ctx.getResources().getQuantityString(R.plurals.threads_images, imagesCount, imagesCount);
        } else if (plainFilesCount != 0) {
            String send = sex ? ctx.getString(R.string.threads_send_male) : ctx.getString(R.string.threads_send_female);
            titletext += " " + send + " ";
            int total = plainFilesCount + imagesCount;
            titletext += ctx.getResources().getQuantityString(R.plurals.threads_files, total, total);
            if (isEmpty(phrase) && plainFilesCount == 1) phrase = docName;
            else if (isEmpty(phrase) && plainFilesCount != 1) {
                phrase = ctx.getString(R.string.threads_touch_to_download);
            }
        } else if (unreadMessages.size() > 1) {
            titletext = ctx.getResources().getQuantityString(R.plurals.threads_new_messages, overallPhrasesCount, overallPhrasesCount);
        }
        PushContents pushContents = new PushContents(
                titletext
                , phrase
                , !isEmpty(avatarPath)
                , imagesCount != 0
                , imagesCount
                , overallPhrasesCount
                , plainFilesCount != 0
                , avatarPath
                , imagePath);
        return new Tuple<>(isNeedAnswer, pushContents);
    }

    public static class PushContents {
        public final String titleText;
        public final String contentText;
        public final boolean hasAvatar;
        public final boolean hasImage;
        public final int imagesCount;
        public final int phrasesCount;
        public final boolean hasPlainFiles;
        public final String avatarPath;
        public final String lastImagePath;

        public PushContents(
                String titleText
                , String contentText
                , boolean hasAvatar
                , boolean hasImage
                , int imagesCount
                , int phrasesCount
                , boolean hasPlainFiles
                , String avatarPath
                , String lastImagePath) {
            this.titleText = titleText;
            this.contentText = contentText;
            this.hasAvatar = hasAvatar;
            this.hasImage = hasImage;
            this.imagesCount = imagesCount;
            this.phrasesCount = phrasesCount;
            this.hasPlainFiles = hasPlainFiles;
            this.avatarPath = avatarPath;
            this.lastImagePath = lastImagePath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PushContents)) return false;
            PushContents that = (PushContents) o;
            if (hasAvatar != that.hasAvatar) return false;
            if (hasImage != that.hasImage) return false;
            if (imagesCount != that.imagesCount) return false;
            if (phrasesCount != that.phrasesCount) return false;
            if (hasPlainFiles != that.hasPlainFiles) return false;
            if (!ObjectsCompat.equals(titleText, that.titleText))
                return false;
            if (!ObjectsCompat.equals(contentText, that.contentText))
                return false;
            if (!ObjectsCompat.equals(avatarPath, that.avatarPath))
                return false;
            return ObjectsCompat.equals(lastImagePath, that.lastImagePath);

        }

        @Override
        public int hashCode() {
            int result = titleText != null ? titleText.hashCode() : 0;
            result = 31 * result + (contentText != null ? contentText.hashCode() : 0);
            result = 31 * result + (hasAvatar ? 1 : 0);
            result = 31 * result + (hasImage ? 1 : 0);
            result = 31 * result + imagesCount;
            result = 31 * result + phrasesCount;
            result = 31 * result + (hasPlainFiles ? 1 : 0);
            result = 31 * result + (avatarPath != null ? avatarPath.hashCode() : 0);
            result = 31 * result + (lastImagePath != null ? lastImagePath.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PushContents{" +
                    "welocmeScreenTitleText='" + titleText + '\'' +
                    ", contentText='" + contentText + '\'' +
                    ", hasAvatar=" + hasAvatar +
                    ", hasImage=" + hasImage +
                    ", imagesCount=" + imagesCount +
                    ", phrasesCount=" + phrasesCount +
                    ", hasPlainFiles=" + hasPlainFiles +
                    ", avatarPath='" + avatarPath + '\'' +
                    ", lastImagePath='" + lastImagePath + '\'' +
                    '}';
        }
    }
}
