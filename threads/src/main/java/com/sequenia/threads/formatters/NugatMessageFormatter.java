package com.sequenia.threads.formatters;

import android.content.Context;
import android.text.TextUtils;

import com.sequenia.threads.R;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.utils.Tuple;

import java.util.List;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static com.sequenia.threads.utils.FileUtils.JPEG;
import static com.sequenia.threads.utils.FileUtils.PDF;
import static com.sequenia.threads.utils.FileUtils.PNG;
import static com.sequenia.threads.utils.FileUtils.getExtensionFromFileDescription;

/**
 * Created by yuri on 14.09.2016.
 */
public class NugatMessageFormatter {
    private Context ctx;
    List<ChatItem> unreadMessages;
    List<ChatItem> incomingPushes;

    public NugatMessageFormatter(Context ctx, List<ChatItem> unreadMessages, List<ChatItem> incomingPushes) {
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
        ConnectionPhrase connectionPhrase = new ConnectionPhrase(ctx);
        for (ChatItem ci : incomingPushes) {
            if (ci instanceof ConsultConnectionMessage || ci instanceof ConsultPhrase)
                unreadMessages.add(ci);
        }
        for (ChatItem ci : unreadMessages) {
            if (ci instanceof ConsultConnectionMessage) {
                ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                name = ccm.getName();
                overallPhrasesCount++;
                phrase = connectionPhrase.getConnectionPhrase(ccm);
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
            ImagesPlurals imagesPlurals = new ImagesPlurals(Locale.getDefault());
            if (imagesCount != 0 && plainFilesCount == 0) {
                if (isEmpty(phrase)) phrase = ctx.getString(R.string.touch_to_look);
                String send = sex ? ctx.getString(R.string.send_male) : ctx.getString(R.string.send_female);
                titletext += " " + send + " ";
                if (imagesCount == 1) {
                    titletext += imagesPlurals.getForQuantity(1).toLowerCase();
                } else {
                    titletext += imagesCount + " " + imagesPlurals.getForQuantity(imagesCount);
                }
            }
        } else if (plainFilesCount != 0) {
            FilesPlurals filesPlurals
                    = new FilesPlurals(Locale.getDefault());
            String send = sex ? ctx.getString(R.string.send_male) : ctx.getString(R.string.send_female);
            titletext += " " + send + " ";
            if ((imagesCount + plainFilesCount) == 1) {
                titletext += filesPlurals.getForQuantity(1).toLowerCase();
            } else {
                titletext += (plainFilesCount + imagesCount) + " " + filesPlurals.getForQuantity((imagesCount + plainFilesCount));
            }
            if (isEmpty(phrase) && plainFilesCount == 1) phrase = docName;
            else if (isEmpty(phrase) && plainFilesCount != 1) {
                phrase = ctx.getString(R.string.touch_to_download);
            }
        } else if (plainFilesCount == 0 && imagesCount == 0 && unreadMessages.size() > 1) {
            titletext = overallPhrasesCount +
                    " "
                    + ctx.getString(R.string.new_) +
                    " "
                    + new MessagesPlurals(Locale.getDefault())
                    .getForQuantity(overallPhrasesCount);
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
            if (titleText != null ? !titleText.equals(that.titleText) : that.titleText != null)
                return false;
            if (contentText != null ? !contentText.equals(that.contentText) : that.contentText != null)
                return false;
            if (avatarPath != null ? !avatarPath.equals(that.avatarPath) : that.avatarPath != null)
                return false;
            return lastImagePath != null ? lastImagePath.equals(that.lastImagePath) : that.lastImagePath == null;

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
