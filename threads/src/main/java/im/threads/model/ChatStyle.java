package im.threads.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.util.Log;

import java.io.Serializable;

import im.threads.activities.ChatActivity;

import static im.threads.fragments.ChatFragment.TAG_GAENABLED;

/**
 * Created by yuri on 08.11.2016.
 */

public class ChatStyle implements Serializable {

    public static final String CHAT_FRAGMENT_BUNDLE = "chatFragmentBundle";

    public static final int INVALID = -1;
    @StringRes
    public final int chatTitleTextResId;
    @ColorRes
    public final int chatToolbarColorResId;
    @ColorRes
    public final int chatStatusBarColorResId;
    @ColorRes
    public final int menuItemTextColorResId;
    @ColorRes
    public final int chatToolbarTextColorResId;
    @ColorRes
    public final int chatToolbarHintTextColor;
    @ColorRes
    public final int chatBackgroundColor;
    @ColorRes
    public final int chatHighlightingColor;
    @ColorRes
    public final int chatMessageInputColor;
    @ColorRes
    public final int chatMessageInputHintTextColor;
    @ColorRes
    public final int incomingMessageBubbleColor;
    @ColorRes
    public final int outgoingMessageBubbleColor;
    @ColorRes
    public final int incomingMessageTextColor;
    @ColorRes
    public final int outgoingMessageTextColor;
    @DrawableRes
    public final int defaultIncomingMessageAvatar;
    @DrawableRes
    public final int imagePlaceholder;
    @DrawableRes
    public final int defPushIconResid;
    @ColorRes
    public final int nugatPushAccentColorResId;
    @StringRes
    public final int defTitleResId;
    public final boolean isGAEnabled;
    @DrawableRes
    public final int welcomeScreenLogoResId;
    @StringRes
    public final int welcomeScreenTitleTextResId;
    @StringRes
    public final int welcomeScreenSubtitleTextResId;
    @ColorRes
    public final int welcomeScreenTextColorResId;
    public final int titleSizeInSp;
    public final int subtitleSizeInSp;
    @ColorRes
    public final int chatBodyIconsTint;
    @ColorRes
    public final int connectionMessageTextColor;
    @StyleRes
    public final int fileBrowserDialogStyleResId;
    @ColorRes
    public final int pushBackgroundColorResId;

    public ChatStyle(int chatBackgroundColor,
                     int chatHighlightingColor,
                     int chatMessageInputColor,
                     int chatMessageInputHintTextColor,
                     int incomingMessageBubbleColor,
                     int outgoingMessageBubbleColor,
                     int incomingMessageTextColor,
                     int outgoingMessageTextColor,
                     int chatBodyIconsTint,
                     int connectionMessageTextColor,
                     int defaultIncomingMessageAvatar,
                     int imagePlaceholder,
                     int fileBrowserDialogStyleResId,
                     int chatTitleTextResId,
                     int chatToolbarColorResId,
                     int chatToolbarTextColorResId,
                     int chatStatusBarColorResId,
                     int menuItemTextColorResId,
                     int chatToolbarHintTextColor,
                     boolean isGAEnabled,
                     int defPushIconResid,
                     int defTitleResId,
                     int pushBackgroundColorResId,
                     int nugatPushAccentColorResId,
                     int welcomeScreenLogoResId,
                     int welcomeScreenTitleTextResId,
                     int welcomeScreenSubtitleTextResId,
                     int welcomeScreenTextColorResId,
                     int titleSizeInSp,
                     int subtitleSizeInSp) {
        this.chatTitleTextResId = chatTitleTextResId;
        this.chatToolbarColorResId = chatToolbarColorResId;
        this.chatToolbarTextColorResId = chatToolbarTextColorResId;
        this.chatBackgroundColor = chatBackgroundColor;
        this.chatMessageInputColor = chatMessageInputColor;
        this.incomingMessageBubbleColor = incomingMessageBubbleColor;
        this.outgoingMessageBubbleColor = outgoingMessageBubbleColor;
        this.incomingMessageTextColor = incomingMessageTextColor;
        this.outgoingMessageTextColor = outgoingMessageTextColor;
        this.defaultIncomingMessageAvatar = defaultIncomingMessageAvatar;
        this.imagePlaceholder = imagePlaceholder;
        this.defPushIconResid = defPushIconResid;
        this.defTitleResId = defTitleResId;
        this.isGAEnabled = isGAEnabled;
        this.welcomeScreenLogoResId = welcomeScreenLogoResId;
        this.welcomeScreenTitleTextResId = welcomeScreenTitleTextResId;
        this.welcomeScreenSubtitleTextResId = welcomeScreenSubtitleTextResId;
        this.welcomeScreenTextColorResId = welcomeScreenTextColorResId;
        this.titleSizeInSp = titleSizeInSp;
        this.subtitleSizeInSp = subtitleSizeInSp;
        this.chatBodyIconsTint = chatBodyIconsTint;
        this.connectionMessageTextColor = connectionMessageTextColor;
        this.pushBackgroundColorResId = pushBackgroundColorResId;
        this.nugatPushAccentColorResId = nugatPushAccentColorResId;
        this.chatStatusBarColorResId = chatStatusBarColorResId;
        this.fileBrowserDialogStyleResId = fileBrowserDialogStyleResId;
        this.chatMessageInputHintTextColor = chatMessageInputHintTextColor;
        this.menuItemTextColorResId = menuItemTextColorResId;
        this.chatToolbarHintTextColor = chatToolbarHintTextColor;
        this.chatHighlightingColor = chatHighlightingColor;
    }

    public static ChatStyle styleFromBundle(Bundle b) {
        Bundle welcomScreenStyle = b.getBundle("setWelcomeScreenStyle");
        boolean isWeclomeExists = welcomScreenStyle != null;
        Bundle pushNotificationStyle = b.getBundle("setPushNotificationStyle");
        boolean isPushNotificationStyleExists = pushNotificationStyle != null;
        Bundle chatBodyStyle = b.getBundle("setChatBodyStyle");
        boolean isChatBodyStyleExists = chatBodyStyle != null;
        Bundle chatTitleStyle = b.getBundle("setChatTitleStyle");
        boolean isChatTitleStyleExists = chatTitleStyle != null;
        boolean isGaEnabled = b.getBoolean(TAG_GAENABLED, true);
        return new ChatStyle(
                isChatBodyStyleExists ? chatBodyStyle.getInt("chatBackgroundColor") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("chatHighlightingColor") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("chatMessageInputBackgroundColor") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("chatMessageHintInputTextColor") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("incomingMessageBubbleColor") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("outgoingMessageBubbleColor") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("incomingMessageTextColor") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("outgoingMessageTextColor") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("chatBodyIconsTint") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("connectionMessageTextColor") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("defaultAvatar") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("imagePlaceholder") : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt("dialogStyleResId") : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt("chatTitleTextResId") : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt("chatTitleBackgroundColorResId") : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt("chatTitleWidgetsColorResId") : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt("chatStatusBarColorResId") : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt("menuItemTextColorResId") : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt("chatToolbarHintTextColor") : INVALID,
                isGaEnabled,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt("defIconResid") : INVALID,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt("defTitleResId") : INVALID,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt("pushBackgroundColorResId") : INVALID,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt("nugatPushAccentColorResId") : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt("welcomeScreenLogoResId") : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt("welcomeScreenTitleTextResId") : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt("welcomeScreenSubtitleTextResId") : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt("welcomeScreenTextColorResId") : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt("titleSizeInSp") : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt("subtitleSizeInSp") : INVALID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatStyle)) return false;

        ChatStyle style = (ChatStyle) o;

        if (chatTitleTextResId != style.chatTitleTextResId) return false;
        if (chatToolbarColorResId != style.chatToolbarColorResId) return false;
        if (chatStatusBarColorResId != style.chatStatusBarColorResId) return false;
        if (chatToolbarTextColorResId != style.chatToolbarTextColorResId) return false;
        if (menuItemTextColorResId != style.menuItemTextColorResId) return false;
        if (chatToolbarHintTextColor != style.chatToolbarHintTextColor) return false;
        if (chatBackgroundColor != style.chatBackgroundColor) return false;
        if (chatHighlightingColor != style.chatHighlightingColor) return false;
        if (chatMessageInputColor != style.chatMessageInputColor) return false;
        if (chatMessageInputHintTextColor != style.chatMessageInputHintTextColor) return false;
        if (incomingMessageBubbleColor != style.incomingMessageBubbleColor) return false;
        if (outgoingMessageBubbleColor != style.outgoingMessageBubbleColor) return false;
        if (incomingMessageTextColor != style.incomingMessageTextColor) return false;
        if (outgoingMessageTextColor != style.outgoingMessageTextColor) return false;
        if (defaultIncomingMessageAvatar != style.defaultIncomingMessageAvatar) return false;
        if (imagePlaceholder != style.imagePlaceholder) return false;
        if (defPushIconResid != style.defPushIconResid) return false;
        if (nugatPushAccentColorResId != style.nugatPushAccentColorResId) return false;
        if (defTitleResId != style.defTitleResId) return false;
        if (isGAEnabled != style.isGAEnabled) return false;
        if (welcomeScreenLogoResId != style.welcomeScreenLogoResId) return false;
        if (welcomeScreenTitleTextResId != style.welcomeScreenTitleTextResId) return false;
        if (welcomeScreenSubtitleTextResId != style.welcomeScreenSubtitleTextResId) return false;
        if (welcomeScreenTextColorResId != style.welcomeScreenTextColorResId) return false;
        if (titleSizeInSp != style.titleSizeInSp) return false;
        if (subtitleSizeInSp != style.subtitleSizeInSp) return false;
        if (chatBodyIconsTint != style.chatBodyIconsTint) return false;
        if (connectionMessageTextColor != style.connectionMessageTextColor) return false;
        if (fileBrowserDialogStyleResId != style.fileBrowserDialogStyleResId) return false;
        return pushBackgroundColorResId == style.pushBackgroundColorResId;

    }

    @Override
    public int hashCode() {
        int result = chatTitleTextResId;
        result = 31 * result + chatToolbarColorResId;
        result = 31 * result + chatStatusBarColorResId;
        result = 31 * result + chatToolbarTextColorResId;
        result = 31 * result + menuItemTextColorResId;
        result = 31 * result + chatToolbarHintTextColor;
        result = 31 * result + chatBackgroundColor;
        result = 31 * result + chatHighlightingColor;
        result = 31 * result + chatMessageInputColor;
        result = 31 * result + chatMessageInputHintTextColor;
        result = 31 * result + incomingMessageBubbleColor;
        result = 31 * result + outgoingMessageBubbleColor;
        result = 31 * result + incomingMessageTextColor;
        result = 31 * result + outgoingMessageTextColor;
        result = 31 * result + defaultIncomingMessageAvatar;
        result = 31 * result + imagePlaceholder;
        result = 31 * result + defPushIconResid;
        result = 31 * result + nugatPushAccentColorResId;
        result = 31 * result + defTitleResId;
        result = 31 * result + (isGAEnabled ? 1 : 0);
        result = 31 * result + welcomeScreenLogoResId;
        result = 31 * result + welcomeScreenTitleTextResId;
        result = 31 * result + welcomeScreenSubtitleTextResId;
        result = 31 * result + welcomeScreenTextColorResId;
        result = 31 * result + titleSizeInSp;
        result = 31 * result + subtitleSizeInSp;
        result = 31 * result + chatBodyIconsTint;
        result = 31 * result + connectionMessageTextColor;
        result = 31 * result + fileBrowserDialogStyleResId;
        result = 31 * result + pushBackgroundColorResId;
        return result;
    }

    @Override
    public String toString() {
        return "ChatStyle{" +
                "chatTitleTextResId=" + chatTitleTextResId +
                ", chatToolbarColorResId=" + chatToolbarColorResId +
                ", chatStatusBarColorResId=" + chatStatusBarColorResId +
                ", menuItemTextColorResId=" + menuItemTextColorResId +
                ", chatToolbarHintTextColor=" + chatToolbarHintTextColor +
                ", chatToolbarTextColorResId=" + chatToolbarTextColorResId +
                ", chatBackgroundColor=" + chatBackgroundColor +
                ", chatHighlightingColor=" + chatHighlightingColor +
                ", chatMessageInputColor=" + chatMessageInputColor +
                ", chatMessageInputHintTextColor=" + chatMessageInputHintTextColor +
                ", incomingMessageBubbleColor=" + incomingMessageBubbleColor +
                ", outgoingMessageBubbleColor=" + outgoingMessageBubbleColor +
                ", incomingMessageTextColor=" + incomingMessageTextColor +
                ", outgoingMessageTextColor=" + outgoingMessageTextColor +
                ", defaultIncomingMessageAvatar=" + defaultIncomingMessageAvatar +
                ", imagePlaceholder=" + imagePlaceholder +
                ", defPushIconResid=" + defPushIconResid +
                ", nugatPushAccentColorResId=" + nugatPushAccentColorResId +
                ", defTitleResId=" + defTitleResId +
                ", isGAEnabled=" + isGAEnabled +
                ", welcomeScreenLogoResId=" + welcomeScreenLogoResId +
                ", welcomeScreenTitleTextResId=" + welcomeScreenTitleTextResId +
                ", welcomeScreenSubtitleTextResId=" + welcomeScreenSubtitleTextResId +
                ", welcomeScreenTextColorResId=" + welcomeScreenTextColorResId +
                ", titleSizeInSp=" + titleSizeInSp +
                ", subtitleSizeInSp=" + subtitleSizeInSp +
                ", chatBodyIconsTint=" + chatBodyIconsTint +
                ", connectionMessageTextColor=" + connectionMessageTextColor +
                ", fileBrowserDialogStyleResId=" + fileBrowserDialogStyleResId +
                ", pushBackgroundColorResId=" + pushBackgroundColorResId +
                '}';
    }

    public static class IntentBuilder {
        private static final String TAG = "BundleBuilder ";
        private Bundle b;
        static IntentBuilder builder;
        private Context ctx;

        private IntentBuilder() {
        }

        public static IntentBuilder getBuilder(Context ctx, String clientId, String userName) {

            builder = new IntentBuilder();
            builder.b = new Bundle();
            builder.b.putString("clientId", clientId);
            builder.ctx = ctx;
            builder.b.putString("userName", userName);
            return builder;
        }

        public IntentBuilder setChatTitleStyle(
                @StringRes int chatTitleTextResId,
                @ColorRes int chatTitleBackgroundColorResId,
                @ColorRes int chatTitleWidgetsColorResId,
                @ColorRes int chatStatusBarColorResId,
                @ColorRes int menuItemTextColorResId,
                @ColorRes int chatToolbarHintTextColor) {
            Bundle bundle = new Bundle();
            builder.b.putBundle("setChatTitleStyle", bundle);
            bundle.putInt("chatTitleTextResId", chatTitleTextResId);
            bundle.putInt("chatTitleBackgroundColorResId", chatTitleBackgroundColorResId);
            bundle.putInt("chatTitleWidgetsColorResId", chatTitleWidgetsColorResId);
            bundle.putInt("chatStatusBarColorResId", chatStatusBarColorResId);
            bundle.putInt("menuItemTextColorResId", menuItemTextColorResId);
            bundle.putInt("chatToolbarHintTextColor", chatToolbarHintTextColor);
            return builder;
        }

        public IntentBuilder setChatBodyStyle(
                @ColorRes int chatBackgroundColor,
                @ColorRes int chatHighlightingColor,
                @ColorRes int chatMessageHintInputTextColor,
                @ColorRes int chatMessageInputBackgroundColor,
                @ColorRes int incomingMessageBubbleColor,
                @ColorRes int outgoingMessageBubbleColor,
                @ColorRes int incomingMessageTextColor,
                @ColorRes int outgoingMessageTextColor,
                @ColorRes int chatBodyIconsTint,
                @ColorRes int connectionMessageTextColor,
                @DrawableRes int defaultIncomingMessageAvatar,
                @DrawableRes int imagePlaceholder,
                @StyleRes int fileBrowserDialogStyleResId) {
            Bundle bundle = new Bundle();
            builder.b.putBundle("setChatBodyStyle", bundle);
            bundle.putInt("chatBackgroundColor", chatBackgroundColor);
            bundle.putInt("chatHighlightingColor", chatHighlightingColor);
            bundle.putInt("chatMessageInputBackgroundColor", chatMessageInputBackgroundColor);
            bundle.putInt("chatMessageHintInputTextColor", chatMessageHintInputTextColor);
            bundle.putInt("incomingMessageBubbleColor", incomingMessageBubbleColor);
            bundle.putInt("outgoingMessageBubbleColor", outgoingMessageBubbleColor);
            bundle.putInt("incomingMessageTextColor", incomingMessageTextColor);
            bundle.putInt("outgoingMessageTextColor", outgoingMessageTextColor);
            bundle.putInt("defaultAvatar", defaultIncomingMessageAvatar);
            bundle.putInt("imagePlaceholder", imagePlaceholder);
            bundle.putInt("chatBodyIconsTint", chatBodyIconsTint);
            bundle.putInt("connectionMessageTextColor", connectionMessageTextColor);
            bundle.putInt("dialogStyleResId", fileBrowserDialogStyleResId);
            return builder;
        }


        public IntentBuilder setPushNotificationStyle(@DrawableRes int defIconResid,
                                                      @StringRes int defTitleResId,
                                                      @ColorRes int pushBackgroundColorResId,
                                                      @ColorRes int nugatPushAccentColorResId) {
            Bundle bundle = new Bundle();
            b.putBundle("setPushNotificationStyle", bundle);
            bundle.putInt("defIconResid", defIconResid);
            bundle.putInt("defTitleResId", defTitleResId);
            bundle.putInt("pushBackgroundColorResId", pushBackgroundColorResId);
            bundle.putInt("nugatPushAccentColorResId", nugatPushAccentColorResId);
            return builder;
        }

        public IntentBuilder setGoogleAnalyticsEnabled(boolean isEnabled) {
            builder.b.putBoolean(TAG_GAENABLED, isEnabled);
            return builder;
        }

        public IntentBuilder setWelcomeScreenStyle(
                @DrawableRes int welcomeScreenLogoResId
                , @StringRes int welcomeScreenTitleTextResId
                , @StringRes int welcomeScreenSubtitleTextResId
                , @ColorRes int welcomeScreenTextColorResId
                , int titleSizeInSp
                , int subtitleSizeInSp) {
            Bundle bundle = new Bundle();
            b.putBundle("setWelcomeScreenStyle", bundle);
            bundle.putInt("welcomeScreenLogoResId", welcomeScreenLogoResId);
            bundle.putInt("welcomeScreenTextColorResId", welcomeScreenTextColorResId);
            bundle.putInt("welcomeScreenTitleTextResId", welcomeScreenTitleTextResId);
            bundle.putInt("welcomeScreenSubtitleTextResId", welcomeScreenSubtitleTextResId);
            bundle.putInt("titleSizeInSp", titleSizeInSp);
            bundle.putInt("subtitleSizeInSp", subtitleSizeInSp);
            return this;
        }

        public Bundle buildBundle() {
            Bundle b = builder.b;
            if (b.getBundle("setWelcomeScreenStyle") == null)
                Log.e(TAG, "you must provide welcome screen  attributes. now using default");
            if (b.getBundle("setPushNotificationStyle") == null)
                Log.e(TAG, "you must provide push notifications style. now using default");
            if (b.getBundle("setChatBodyStyle") == null)
                Log.e(TAG, "you must chat body style. now using default");
            if (b.getBundle("setChatTitleStyle") == null)
                Log.e(TAG, "you must chat title style. now using default");
            b.putBoolean("style", true);
            builder = null;
            return b;
        }

        public Intent build() {
            Intent i = new Intent(ctx, ChatActivity.class);
            Bundle b = buildBundle();
            if (b.getString("clientId") == null)
                throw new IllegalStateException("client id is obligatory");
            i.putExtra(CHAT_FRAGMENT_BUNDLE, b);
            return i;
        }
    }
}
