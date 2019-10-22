package im.threads.internal.formatters;

import android.content.Context;
import android.support.v4.util.ObjectsCompat;

import java.util.List;
import java.util.ListIterator;

import im.threads.R;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.utils.Tuple;

import static android.text.TextUtils.isEmpty;
import static im.threads.internal.utils.FileUtils.JPEG;
import static im.threads.internal.utils.FileUtils.PDF;
import static im.threads.internal.utils.FileUtils.PNG;
import static im.threads.internal.utils.FileUtils.UNKNOWN;
import static im.threads.internal.utils.FileUtils.getExtensionFromFileDescription;

public final class MarshmallowPushMessageFormatter {
    private Context ctx;
    private List<ChatItem> unreadMessages;
    private List<ChatItem> incomingPushes;

    public MarshmallowPushMessageFormatter(
            Context ctx
            , List<ChatItem> unreadChatItems
            , List<ChatItem> incomingPushes) {
        this.ctx = ctx;
        this.unreadMessages = unreadChatItems;
        this.incomingPushes = incomingPushes;
    }

    public Tuple<Boolean, PushContents> getFormattedMessageAsPushContents() {
        int imagesCount = 0;
        int plainFilesCount = 0;
        String contentDescription = "";
        if (unreadMessages == null || incomingPushes == null) return null;
        String consultName = null;
        String phrase = null;
        boolean isNeedAnswer = false;
        for (int i = 0; i < incomingPushes.size(); i++) {
            if (incomingPushes.get(i) instanceof ConsultPhrase
                    || incomingPushes.get(i) instanceof ConsultConnectionMessage)
                unreadMessages.add(incomingPushes.get(i));
        }

        for (ChatItem ci : unreadMessages) {
            if (ci instanceof ConsultPhrase) {
                isNeedAnswer = true;
                if (!isEmpty(((ConsultPhrase) ci).getPhrase())) {
                    phrase = ((ConsultPhrase) ci).getPhrase();
                }
            } else if (ci instanceof ConsultConnectionMessage) {
                String connectionPhrase = ConnectionPhrase.getConnectionPhrase(ctx, (ConsultConnectionMessage) ci);
                if (!isEmpty(connectionPhrase)) {
                    phrase = connectionPhrase;
                }
            }
        }
        ListIterator<ChatItem> itemListIterator = unreadMessages.listIterator(unreadMessages.size());
        while (itemListIterator.hasPrevious()) {
            ChatItem ci = itemListIterator.previous();
            if (ci instanceof ConsultPhrase) {
                if (isEmpty(consultName)) consultName = ((ConsultPhrase) ci).getConsultName();
            }
            if (ci instanceof ConsultConnectionMessage) {
                if (isEmpty(consultName)) consultName = ((ConsultConnectionMessage) ci).getName();
            }
        }
        if (phrase == null) phrase = "";
        if (consultName == null) consultName = ": ";
        for (ChatItem ci : unreadMessages) {
            if (ci instanceof ConsultPhrase) {
                ConsultPhrase consultPhrase = (ConsultPhrase) ci;
                if (getExtensionFromFileDescription(consultPhrase.getFileDescription()) == PNG
                        || getExtensionFromFileDescription(consultPhrase.getFileDescription()) == JPEG) {
                    imagesCount++;
                } else if (getExtensionFromFileDescription(consultPhrase.getFileDescription()) != UNKNOWN) {
                    plainFilesCount++;
                }
                if (consultPhrase.getQuote() != null) {
                    if (getExtensionFromFileDescription(consultPhrase.getQuote().getFileDescription()) == PNG
                            || getExtensionFromFileDescription(consultPhrase.getQuote().getFileDescription()) == JPEG) {
                        imagesCount++;
                    } else if (getExtensionFromFileDescription(consultPhrase.getQuote().getFileDescription()) == PDF) {
                        plainFilesCount++;
                    }
                }
            }
        }
        if (imagesCount != 0 && plainFilesCount == 0) {
            contentDescription = ctx.getResources().getQuantityString(R.plurals.threads_images, imagesCount, imagesCount);
        } else if (plainFilesCount != 0) {
            if (plainFilesCount == 1 && imagesCount == 0) {
                String fileName = null;
                for (ChatItem ci : unreadMessages) {
                    if (ci instanceof ConsultPhrase) {
                        ConsultPhrase cp = (ConsultPhrase) ci;
                        if (cp.getFileDescription() != null) {
                            fileName = cp.getFileDescription().getIncomingName();
                        } else if (null != cp.getQuote() && null != cp.getQuote().getFileDescription()) {
                            fileName = cp.getQuote().getFileDescription().getIncomingName();
                        }
                    }
                }
                contentDescription = fileName;
            } else {
                int total = plainFilesCount + imagesCount;
                contentDescription = ctx.getResources().getQuantityString(R.plurals.threads_files, total, total);
            }
        }
        if (phrase.length() > 0) {
            if (imagesCount != 0 || plainFilesCount != 0) {
                contentDescription += " | " + phrase;
            }
        }
        if (imagesCount == 0 && plainFilesCount == 0) contentDescription = phrase;
        return new Tuple<>(isNeedAnswer, new PushContents(
                consultName
                , contentDescription
                , imagesCount != 0 || plainFilesCount != 0
                , imagesCount != 0 && plainFilesCount == 0));
    }


    public static class PushContents {
        public final String consultName;
        public final String contentDescription;
        public final boolean isWithAttachments;
        public final boolean isOnlyImages;

        PushContents(String consultName, String contentDescription, boolean isWithAttachments, boolean isOnlyImages) {
            this.consultName = consultName;
            this.contentDescription = contentDescription;
            this.isWithAttachments = isWithAttachments;
            this.isOnlyImages = isOnlyImages;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PushContents)) return false;

            PushContents that = (PushContents) o;

            if (isWithAttachments != that.isWithAttachments) return false;
            if (isOnlyImages != that.isOnlyImages) return false;
            if (!ObjectsCompat.equals(consultName, that.consultName))
                return false;
            return ObjectsCompat.equals(contentDescription, that.contentDescription);

        }

        @Override
        public int hashCode() {
            int result = consultName != null ? consultName.hashCode() : 0;
            result = 31 * result + (contentDescription != null ? contentDescription.hashCode() : 0);
            result = 31 * result + (isWithAttachments ? 1 : 0);
            result = 31 * result + (isOnlyImages ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PushContents{" +
                    "consultName='" + consultName + '\'' +
                    ", contentDescription='" + contentDescription + '\'' +
                    ", isWithAttachments=" + isWithAttachments +
                    ", isOnlyImages=" + isOnlyImages +
                    '}';
        }
    }
}
