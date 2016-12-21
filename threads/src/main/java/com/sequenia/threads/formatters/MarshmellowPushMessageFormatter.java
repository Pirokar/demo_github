package com.sequenia.threads.formatters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;

import com.sequenia.threads.R;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.utils.Tuple;

import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static com.sequenia.threads.utils.FileUtils.JPEG;
import static com.sequenia.threads.utils.FileUtils.PDF;
import static com.sequenia.threads.utils.FileUtils.PNG;
import static com.sequenia.threads.utils.FileUtils.getExtensionFromFileDescription;

/**
 * Created by yuri on 12.09.2016.
 */
public class MarshmellowPushMessageFormatter {
    private Context ctx;
    List<ChatItem> unreadMessages;
    List<ChatItem> incomingPushes;

    public MarshmellowPushMessageFormatter(
            Context ctx
            , List<ChatItem> unreadChatItems
            , List<ChatItem> incomingPushes) {
        this.ctx = ctx;
        this.unreadMessages = unreadChatItems;
        this.incomingPushes = incomingPushes;
    }

    /**
     * @return tuple, where bool value means that messages contain not only system messages(that does't need any reply)
     * and spannable with text line
     */
    public Tuple<Boolean, SpannableStringBuilder> getFormattedMessageAsSpannable() {
        int imagesCount = 0;
        int plainFilesCount = 0;
        int messagesCount = 0;
        if (unreadMessages == null || incomingPushes == null) return null;
        SpannableStringBuilder sp = SpannableStringBuilder.valueOf("");
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
                if (consultName == null) consultName = ((ConsultPhrase) ci).getConsultName() + ": ";
                if (!isEmpty(((ConsultPhrase) ci).getPhrase())) {
                    phrase = ((ConsultPhrase) ci).getPhrase();
                }
            } else if (ci instanceof ConsultConnectionMessage) {
                if (consultName == null)
                    consultName = ((ConsultConnectionMessage) ci).getName() + ": ";
                if (!isEmpty(getConnectionPhrase((ConsultConnectionMessage) ci))) {
                    phrase = getConnectionPhrase((ConsultConnectionMessage) ci);
                }
            }
        }
        if (phrase == null) phrase = "";
        if (consultName == null) consultName = ": ";
        sp.append(consultName);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, consultName.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        messagesCount = unreadMessages.size();
        for (ChatItem ci : unreadMessages) {
            if (ci instanceof ConsultPhrase) {
                ConsultPhrase consultPhrase = (ConsultPhrase) ci;
                if (getExtensionFromFileDescription(consultPhrase.getFileDescription()) == PNG
                        || getExtensionFromFileDescription(consultPhrase.getFileDescription()) == JPEG) {
                    imagesCount++;
                } else if (getExtensionFromFileDescription(consultPhrase.getFileDescription()) == PDF) {
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
            ImagesPlurals plurals = new ImagesPlurals(Locale.getDefault());
            Drawable d = ctx.getResources().getDrawable(R.drawable.ic_images_12dp);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            ImageSpan imageSpan = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
            sp.setSpan(imageSpan, consultName.length() - 1, consultName.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            if (imagesCount == 1) sp.append(new SpannableString(" " + plurals.getForQuantity(1)));
            else {
                sp.append(new SpannableString(" " + imagesCount + " " + plurals.getForQuantity(imagesCount)));
            }
        } else if (plainFilesCount != 0) {
            FilesPlurals filesPlurals = new FilesPlurals(Locale.getDefault());
            ImagesPlurals plurals = new ImagesPlurals(Locale.getDefault());
            Drawable d = ctx.getResources().getDrawable(R.drawable.ic_attach_file_gray_12dp);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            ImageSpan imageSpan = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
            sp.setSpan(imageSpan, consultName.length() - 1, consultName.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
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

                if (fileName == null) fileName = "";
                sp.append(new SpannableString(" " + fileName));
            } else {
                sp.append(new SpannableString(
                        " " +
                                (plainFilesCount
                                        + imagesCount) + " "
                                + filesPlurals.getForQuantity(plainFilesCount + imagesCount)));
            }
        }
        if (phrase.length() > 0) {
            if (imagesCount == 0 && plainFilesCount == 0) {
                sp.append(new SpannableString(phrase));
            } else {
                sp.append(new SpannableString(" | " + phrase));
            }
        }
        return new Tuple<>(isNeedAnswer, sp);
    }

    String getConnectionPhrase(ConsultConnectionMessage ccm) {
        return new ConnectionPhrase(ctx).getConnectionPhrase(ccm);
    }

    public Tuple<Boolean, PushContents> getFormattedMessageAsPushContents() {
        int imagesCount = 0;
        int plainFilesCount = 0;
        int messagesCount = 0;
        String contentDesciprion = "";
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
                // if (consultName == null) consultName = ((ConsultPhrase) ci).getConsultName();
                if (!isEmpty(((ConsultPhrase) ci).getPhrase())) {
                    phrase = ((ConsultPhrase) ci).getPhrase();
                }
            } else if (ci instanceof ConsultConnectionMessage) {
               /* if (consultName == null)
                    consultName = ((ConsultConnectionMessage) ci).getName();*/
                if (!isEmpty(getConnectionPhrase((ConsultConnectionMessage) ci))) {
                    phrase = getConnectionPhrase((ConsultConnectionMessage) ci);
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
        messagesCount = unreadMessages.size();
        for (ChatItem ci : unreadMessages) {
            if (ci instanceof ConsultPhrase) {
                ConsultPhrase consultPhrase = (ConsultPhrase) ci;
                if (getExtensionFromFileDescription(consultPhrase.getFileDescription()) == PNG
                        || getExtensionFromFileDescription(consultPhrase.getFileDescription()) == JPEG) {
                    imagesCount++;
                } else if (getExtensionFromFileDescription(consultPhrase.getFileDescription()) == PDF) {
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
            ImagesPlurals plurals = new ImagesPlurals(Locale.getDefault());
            if (imagesCount == 1) contentDesciprion = plurals.getForQuantity(1);
            else {
                contentDesciprion = imagesCount + " " + plurals.getForQuantity(imagesCount);
            }
        } else if (plainFilesCount != 0) {
            FilesPlurals filesPlurals = new FilesPlurals(Locale.getDefault());
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
                contentDesciprion = fileName;
            } else {
                contentDesciprion = (plainFilesCount + imagesCount) + " " + filesPlurals.getForQuantity(plainFilesCount + imagesCount);
            }
        }
        if (phrase.length() > 0) {
            if (imagesCount == 0 && plainFilesCount == 0) {

            } else {
                contentDesciprion += " | " + phrase;
            }
        }
        if (imagesCount == 0 && plainFilesCount == 0) contentDesciprion = phrase;
        return new Tuple<>(isNeedAnswer, new PushContents(
                consultName
                , contentDesciprion
                , imagesCount != 0 || plainFilesCount != 0
                , imagesCount != 0 && plainFilesCount == 0));
    }


    public static class PushContents {
        public final String consultName;
        public final String contentDescription;
        public final boolean isWithAttachments;
        public final boolean isOnlyImages;

        public PushContents(String consultName, String contentDescription, boolean isWithAttachments, boolean isOnlyImages) {
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
            if (consultName != null ? !consultName.equals(that.consultName) : that.consultName != null)
                return false;
            return contentDescription != null ? contentDescription.equals(that.contentDescription) : that.contentDescription == null;

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
