package im.threads.internal.formatters;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;

import java.util.ArrayList;
import java.util.List;

import im.threads.R;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.QuestionDTO;
import im.threads.internal.model.SimpleSystemMessage;
import im.threads.internal.model.Survey;
import im.threads.internal.utils.FileUtils;

public final class MessageFormatter {

    private MessageFormatter() {
    }

    public static MessageContent parseMessageContent(Context ctx, List<ChatItem> chatItems) {
        int imagesCount = 0;
        int plainFilesCount = 0;
        String avatarPath = null;
        String imagePath = null;
        String phrase = "";
        boolean sex = false;
        String docName = "";
        String consultName = null;
        boolean isNeedAnswer = false;
        List<ChatItem> unreadMessages = new ArrayList<>();
        for (ChatItem ci : chatItems) {
            if (ci instanceof ConsultConnectionMessage || ci instanceof ConsultPhrase || ci instanceof SimpleSystemMessage || ci instanceof Survey) {
                unreadMessages.add(ci);
            }
        }
        for (ChatItem ci : unreadMessages) {
            if (ci instanceof ConsultConnectionMessage) {
                ConsultConnectionMessage ccm = (ConsultConnectionMessage) ci;
                consultName = ccm.getName();
                phrase = ccm.getText();
                sex = ccm.getSex();
                avatarPath = ((ConsultConnectionMessage) ci).getAvatarPath();
            }
            if (ci instanceof SimpleSystemMessage) {
                SimpleSystemMessage ccm = (SimpleSystemMessage) ci;
                phrase = ccm.getText();
            }
            if (ci instanceof ConsultPhrase) {
                ConsultPhrase consultPhrase = (ConsultPhrase) ci;
                isNeedAnswer = true;
                if (!TextUtils.isEmpty(consultPhrase.getPhraseText())) {
                    phrase = consultPhrase.getPhraseText();
                }
                sex = consultPhrase.getSex();
                if (consultPhrase.getConsultName() != null) {
                    consultName = consultPhrase.getConsultName();
                }
                if (consultPhrase.getFileDescription() != null) {
                    FileDescription fileDescription = consultPhrase.getFileDescription();
                    if (FileUtils.isImage(fileDescription)) {
                        imagesCount++;
                        imagePath = fileDescription.getDownloadPath();
                    } else {
                        plainFilesCount++;
                        docName = fileDescription.getIncomingName();
                    }
                }
                if (consultPhrase.getQuote() != null && consultPhrase.getQuote().getFileDescription() != null) {
                    FileDescription fileDescription = consultPhrase.getQuote().getFileDescription();
                    if (FileUtils.isImage(fileDescription)) {
                        imagesCount++;
                        imagePath = fileDescription.getDownloadPath();
                    } else {
                        plainFilesCount++;
                        docName = fileDescription.getIncomingName();
                    }
                }
                avatarPath = consultPhrase.getAvatarPath();
            }
            if (ci instanceof Survey) {
                Survey ccm = (Survey) ci;
                final List<QuestionDTO> questions = ccm.getQuestions();
                if (questions != null && questions.size() > 0) {
                    phrase = questions.get(0).getText();
                }
            }
        }
        String titleText = consultName;
        if (plainFilesCount != 0) {
            String send = sex ? ctx.getString(R.string.threads_send_male) : ctx.getString(R.string.threads_send_female);
            titleText = consultName + " " + send + " ";
            int total = plainFilesCount + imagesCount;
            titleText += ctx.getResources().getQuantityString(R.plurals.threads_files, total, total);
            if (TextUtils.isEmpty(phrase)) {
                if (total == 1) {
                    phrase = docName;
                } else {
                    phrase = ctx.getString(R.string.threads_touch_to_download);
                }
            }
        } else if (imagesCount != 0) {
            String send = sex ? ctx.getString(R.string.threads_send_male) : ctx.getString(R.string.threads_send_female);
            titleText = consultName + " " + send + " " +
                    ctx.getResources().getQuantityString(R.plurals.threads_images, imagesCount, imagesCount);
            if (TextUtils.isEmpty(phrase)) {
                phrase = ctx.getString(R.string.threads_touch_to_look);
            }
        } else if (unreadMessages.size() > 1) {
            titleText = ctx.getResources().getQuantityString(R.plurals.threads_new_messages, unreadMessages.size(), unreadMessages.size());
        }
        return new MessageContent(
                titleText,
                phrase != null ? phrase : "",
                !TextUtils.isEmpty(avatarPath),
                imagesCount != 0,
                plainFilesCount != 0,
                imagesCount,
                unreadMessages.size(),
                avatarPath,
                imagePath,
                consultName,
                isNeedAnswer
        );
    }

    public static class MessageContent implements Parcelable {
        public static final Creator<MessageContent> CREATOR = new Creator<MessageContent>() {
            @Override
            public MessageContent createFromParcel(Parcel in) {
                return new MessageContent(in);
            }

            @Override
            public MessageContent[] newArray(int size) {
                return new MessageContent[size];
            }
        };
        public final String titleText;
        @NonNull
        public final String contentText;
        public final boolean hasAvatar;
        public final boolean hasImage;
        public final boolean hasPlainFiles;
        public final int imagesCount;
        public final int phrasesCount;
        public final String avatarPath;
        public final String lastImagePath;
        public final String consultName;
        public final boolean isNeedAnswer;

        MessageContent(
                String titleText,
                @NonNull String contentText,
                boolean hasAvatar,
                boolean hasImage,
                boolean hasPlainFiles,
                int imagesCount,
                int phrasesCount,
                String avatarPath,
                String lastImagePath,
                String consultName,
                boolean isNeedAnswer) {
            this.titleText = titleText;
            this.contentText = contentText;
            this.hasAvatar = hasAvatar;
            this.hasImage = hasImage;
            this.imagesCount = imagesCount;
            this.phrasesCount = phrasesCount;
            this.hasPlainFiles = hasPlainFiles;
            this.avatarPath = avatarPath;
            this.lastImagePath = lastImagePath;
            this.consultName = consultName;
            this.isNeedAnswer = isNeedAnswer;
        }

        protected MessageContent(Parcel in) {
            titleText = in.readString();
            contentText = in.readString();
            hasAvatar = in.readByte() != 0;
            hasImage = in.readByte() != 0;
            hasPlainFiles = in.readByte() != 0;
            imagesCount = in.readInt();
            phrasesCount = in.readInt();
            avatarPath = in.readString();
            lastImagePath = in.readString();
            consultName = in.readString();
            isNeedAnswer = in.readByte() != 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MessageContent that = (MessageContent) o;
            return hasAvatar == that.hasAvatar &&
                    hasImage == that.hasImage &&
                    imagesCount == that.imagesCount &&
                    phrasesCount == that.phrasesCount &&
                    hasPlainFiles == that.hasPlainFiles &&
                    isNeedAnswer == that.isNeedAnswer &&
                    ObjectsCompat.equals(titleText, that.titleText) &&
                    ObjectsCompat.equals(contentText, that.contentText) &&
                    ObjectsCompat.equals(avatarPath, that.avatarPath) &&
                    ObjectsCompat.equals(lastImagePath, that.lastImagePath) &&
                    ObjectsCompat.equals(consultName, that.consultName);
        }

        @Override
        public int hashCode() {
            return ObjectsCompat.hash(titleText, contentText, hasAvatar, hasImage, imagesCount, phrasesCount, hasPlainFiles, avatarPath, lastImagePath, consultName, isNeedAnswer);
        }

        @Override
        public String toString() {
            return "MessageContent{" +
                    "titleText='" + titleText + '\'' +
                    ", contentText='" + contentText + '\'' +
                    ", hasAvatar=" + hasAvatar +
                    ", hasImage=" + hasImage +
                    ", hasPlainFiles=" + hasPlainFiles +
                    ", imagesCount=" + imagesCount +
                    ", phrasesCount=" + phrasesCount +
                    ", avatarPath='" + avatarPath + '\'' +
                    ", lastImagePath='" + lastImagePath + '\'' +
                    ", consultName='" + consultName + '\'' +
                    ", isNeedAnswer=" + isNeedAnswer +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(titleText);
            dest.writeString(contentText);
            dest.writeByte((byte) (hasAvatar ? 1 : 0));
            dest.writeByte((byte) (hasImage ? 1 : 0));
            dest.writeByte((byte) (hasPlainFiles ? 1 : 0));
            dest.writeInt(imagesCount);
            dest.writeInt(phrasesCount);
            dest.writeString(avatarPath);
            dest.writeString(lastImagePath);
            dest.writeString(consultName);
            dest.writeByte((byte) (isNeedAnswer ? 1 : 0));
        }
    }
}
