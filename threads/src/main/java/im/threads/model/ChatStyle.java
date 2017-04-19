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

/**
 * Стиль чата.
 * Хранит в себе информацию о расцветках, иконках
 * и других кастомизациях чата.
 * Created by yuri on 08.11.2016.
 */

public class ChatStyle implements Serializable {

    public static final String CHAT_FRAGMENT_BUNDLE = "chatFragmentBundle";

    private static final String ARG_CLIENT_ID = "clientId";

    private static final String ARG_SET_WELCOME_SCREEN_STYLE = "setWelcomeScreenStyle";
    private static final String ARG_SET_PUSH_NOTIFICATION_STYLE = "setPushNotificationStyle";
    private static final String ARG_SET_CHAT_BODY_STYLE = "setChatBodyStyle";
    private static final String ARG_SET_CHAT_TITLE_STYLE = "setChatTitleStyle";
    private static final String ARG_STYLE = "style";

    private static final String ARG_WELCOME_SCREEN_LOGO_RES_ID = "welcomeScreenLogoResId";
    private static final String ARG_WELCOME_SCREEN_TEXT_COLOR_RES_ID = "welcomeScreenTextColorResId";
    private static final String ARG_WELCOME_SCREEN_TITLE_TEXT_RES_ID = "welcomeScreenTitleTextResId";
    private static final String ARG_WELCOME_SCREEN_SUBTITLE_TEXT_RES_ID = "welcomeScreenSubtitleTextResId";
    private static final String ARG_TITLE_SIZE_IN_SP = "titleSizeInSp";
    private static final String ARG_SUBTITLE_SIZE_IN_SP = "subtitleSizeInSp";
    private static final String ARG_DEF_PUSH_ICON_RES_ID = "defPushIconResId";
    private static final String ARG_DEF_TITLE_RES_ID = "defTitleResId";
    private static final String ARG_PUSH_BACKGROUND_COLOR_RES_ID = "pushBackgroundColorResId";
    private static final String ARG_NOUGAT_PUSH_ACCENT_COLOR_RES_ID = "nugatPushAccentColorResId";
    private static final String ARG_CHAT_BACKGROUND_COLOR_RES_ID = "chatBackgroundColor";
    private static final String ARG_CHAT_HIGHLIGHTING_COLOR_RES_ID = "chatHighlightingColor";
    private static final String ARG_CHAT_MESSAGE_INPUT_COLOR_RES_ID = "chatMessageInputColor";
    private static final String ARG_CHAT_MESSAGE_INPUT_HINT_TEXT_COLOR_RES_ID = "chatMessageInputHintTextColor";
    private static final String ARG_INCOMING_MESSAGE_BUBBLE_COLOR_RES_ID = "incomingMessageBubbleColor";
    private static final String ARG_OUTGOING_MESSAGE_BUBBLE_COLOR_RES_ID = "outgoingMessageBubbleColor";
    private static final String ARG_INCOMING_MESSAGE_TEXT_COLOR_RES_ID = "incomingMessageTextColor";
    private static final String ARG_OUTGOING_MESSAGE_TEXT_COLOR_RES_ID = "outgoingMessageTextColor";
    private static final String ARG_DEFAULT_INCOMING_MESSAGE_AVATAR_RES_ID = "defaultIncomingMessageAvatar";
    private static final String ARG_IMAGE_PLACEHOLDER_RES_ID = "imagePlaceholder";
    private static final String ARG_CHAT_BODY_ICONS_TINT_RES_ID = "chatBodyIconsTint";
    private static final String ARG_CONNECTION_MESSAGE_TEXT_COLOR_RES_ID = "connectionMessageTextColor";
    private static final String ARG_FILE_BROWSER_DIALOG_STYLE_RES_ID = "fileBrowserDialogStyleResId";
    private static final String ARG_CHAT_TITLE_TEXT_RES_ID = "chatTitleTextResId";
    private static final String ARG_CHAT_TOOLBAR_COLOR_RES_ID = "chatToolbarColorResId";
    private static final String ARG_CHAT_TOOLBAR_TEXT_COLOR_RES_ID = "chatToolbarTextColorResId";
    private static final String ARG_CHAT_STATUS_BAR_COLOR_RES_ID = "chatStatusBarColorResId";
    private static final String ARG_MENU_ITEM_TEXT_COLOR_RES_ID = "menuItemTextColorResId";
    private static final String ARG_CHAT_TOOLBAR_HINT_TEXT_COLOR = "chatToolbarHintTextColor";
    private static final String ARG_IS_GA_ENABLED = "isGAEnabled";

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
        Bundle welcomScreenStyle = b.getBundle(ARG_SET_WELCOME_SCREEN_STYLE);
        boolean isWeclomeExists = welcomScreenStyle != null;
        Bundle pushNotificationStyle = b.getBundle(ARG_SET_PUSH_NOTIFICATION_STYLE);
        boolean isPushNotificationStyleExists = pushNotificationStyle != null;
        Bundle chatBodyStyle = b.getBundle(ARG_SET_CHAT_BODY_STYLE);
        boolean isChatBodyStyleExists = chatBodyStyle != null;
        Bundle chatTitleStyle = b.getBundle(ARG_SET_CHAT_TITLE_STYLE);
        boolean isChatTitleStyleExists = chatTitleStyle != null;
        boolean isGaEnabled = b.getBoolean(ARG_IS_GA_ENABLED, true);
        return new ChatStyle(
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_CHAT_BACKGROUND_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_CHAT_HIGHLIGHTING_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_CHAT_MESSAGE_INPUT_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_CHAT_MESSAGE_INPUT_HINT_TEXT_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_INCOMING_MESSAGE_BUBBLE_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_OUTGOING_MESSAGE_BUBBLE_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_INCOMING_MESSAGE_TEXT_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_OUTGOING_MESSAGE_TEXT_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_CHAT_BODY_ICONS_TINT_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_CONNECTION_MESSAGE_TEXT_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_DEFAULT_INCOMING_MESSAGE_AVATAR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_IMAGE_PLACEHOLDER_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_FILE_BROWSER_DIALOG_STYLE_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_TITLE_TEXT_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_TOOLBAR_COLOR_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_TOOLBAR_TEXT_COLOR_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_STATUS_BAR_COLOR_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_MENU_ITEM_TEXT_COLOR_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_TOOLBAR_HINT_TEXT_COLOR) : INVALID,
                isGaEnabled,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt(ARG_DEF_PUSH_ICON_RES_ID) : INVALID,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt(ARG_DEF_TITLE_RES_ID) : INVALID,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt(ARG_PUSH_BACKGROUND_COLOR_RES_ID) : INVALID,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt(ARG_NOUGAT_PUSH_ACCENT_COLOR_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_LOGO_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_TITLE_TEXT_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_SUBTITLE_TEXT_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_TEXT_COLOR_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_TITLE_SIZE_IN_SP) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_SUBTITLE_SIZE_IN_SP) : INVALID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatStyle)) return false;

        ChatStyle style = (ChatStyle) o;

        return chatTitleTextResId == style.chatTitleTextResId &&
            chatToolbarColorResId == style.chatToolbarColorResId &&
            chatStatusBarColorResId == style.chatStatusBarColorResId &&
            chatToolbarTextColorResId == style.chatToolbarTextColorResId &&
            menuItemTextColorResId == style.menuItemTextColorResId &&
            chatToolbarHintTextColor == style.chatToolbarHintTextColor &&
            chatBackgroundColor == style.chatBackgroundColor &&
            chatHighlightingColor == style.chatHighlightingColor &&
            chatMessageInputColor == style.chatMessageInputColor &&
            chatMessageInputHintTextColor == style.chatMessageInputHintTextColor &&
            incomingMessageBubbleColor == style.incomingMessageBubbleColor &&
            outgoingMessageBubbleColor == style.outgoingMessageBubbleColor &&
            incomingMessageTextColor == style.incomingMessageTextColor &&
            outgoingMessageTextColor == style.outgoingMessageTextColor &&
            defaultIncomingMessageAvatar == style.defaultIncomingMessageAvatar &&
            imagePlaceholder == style.imagePlaceholder &&
            defPushIconResid == style.defPushIconResid &&
            nugatPushAccentColorResId == style.nugatPushAccentColorResId &&
            defTitleResId == style.defTitleResId &&
            isGAEnabled == style.isGAEnabled &&
            welcomeScreenLogoResId == style.welcomeScreenLogoResId &&
            welcomeScreenTitleTextResId == style.welcomeScreenTitleTextResId &&
            welcomeScreenSubtitleTextResId == style.welcomeScreenSubtitleTextResId &&
            welcomeScreenTextColorResId == style.welcomeScreenTextColorResId &&
            titleSizeInSp == style.titleSizeInSp &&
            subtitleSizeInSp == style.subtitleSizeInSp &&
            chatBodyIconsTint == style.chatBodyIconsTint &&
            connectionMessageTextColor == style.connectionMessageTextColor &&
            fileBrowserDialogStyleResId == style.fileBrowserDialogStyleResId &&
            pushBackgroundColorResId == style.pushBackgroundColorResId;
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
                ARG_CHAT_TITLE_TEXT_RES_ID + "=" + chatTitleTextResId +
                ", " + ARG_CHAT_TOOLBAR_COLOR_RES_ID + "=" + chatToolbarColorResId +
                ", " + ARG_CHAT_STATUS_BAR_COLOR_RES_ID + "=" + chatStatusBarColorResId +
                ", " + ARG_MENU_ITEM_TEXT_COLOR_RES_ID + "=" + menuItemTextColorResId +
                ", " + ARG_CHAT_TOOLBAR_HINT_TEXT_COLOR + "=" + chatToolbarHintTextColor +
                ", " + ARG_CHAT_TOOLBAR_TEXT_COLOR_RES_ID + "=" + chatToolbarTextColorResId +
                ", " + ARG_CHAT_BACKGROUND_COLOR_RES_ID + "=" + chatBackgroundColor +
                ", " + ARG_CHAT_HIGHLIGHTING_COLOR_RES_ID + "=" + chatHighlightingColor +
                ", " + ARG_CHAT_MESSAGE_INPUT_COLOR_RES_ID + "=" + chatMessageInputColor +
                ", " + ARG_CHAT_MESSAGE_INPUT_HINT_TEXT_COLOR_RES_ID + "=" + chatMessageInputHintTextColor +
                ", " + ARG_INCOMING_MESSAGE_BUBBLE_COLOR_RES_ID + "=" + incomingMessageBubbleColor +
                ", " + ARG_OUTGOING_MESSAGE_BUBBLE_COLOR_RES_ID + "=" + outgoingMessageBubbleColor +
                ", " + ARG_INCOMING_MESSAGE_TEXT_COLOR_RES_ID + "=" + incomingMessageTextColor +
                ", " + ARG_OUTGOING_MESSAGE_TEXT_COLOR_RES_ID + "=" + outgoingMessageTextColor +
                ", " + ARG_DEFAULT_INCOMING_MESSAGE_AVATAR_RES_ID + "=" + defaultIncomingMessageAvatar +
                ", " + ARG_IMAGE_PLACEHOLDER_RES_ID + "=" + imagePlaceholder +
                ", " + ARG_DEF_PUSH_ICON_RES_ID + "=" + defPushIconResid +
                ", " + ARG_NOUGAT_PUSH_ACCENT_COLOR_RES_ID + "=" + nugatPushAccentColorResId +
                ", " + ARG_DEF_TITLE_RES_ID + "=" + defTitleResId +
                ", " + ARG_IS_GA_ENABLED + "=" + isGAEnabled +
                ", " + ARG_WELCOME_SCREEN_LOGO_RES_ID + "=" + welcomeScreenLogoResId +
                ", " + ARG_WELCOME_SCREEN_TITLE_TEXT_RES_ID + "=" + welcomeScreenTitleTextResId +
                ", " + ARG_WELCOME_SCREEN_SUBTITLE_TEXT_RES_ID + "=" + welcomeScreenSubtitleTextResId +
                ", " + ARG_WELCOME_SCREEN_TEXT_COLOR_RES_ID + "=" + welcomeScreenTextColorResId +
                ", " + ARG_TITLE_SIZE_IN_SP + "=" + titleSizeInSp +
                ", " + ARG_SUBTITLE_SIZE_IN_SP + "=" + subtitleSizeInSp +
                ", " + ARG_CHAT_BODY_ICONS_TINT_RES_ID + "=" + chatBodyIconsTint +
                ", " + ARG_CONNECTION_MESSAGE_TEXT_COLOR_RES_ID + "=" + connectionMessageTextColor +
                ", " + ARG_FILE_BROWSER_DIALOG_STYLE_RES_ID + "=" + fileBrowserDialogStyleResId +
                ", " + ARG_PUSH_BACKGROUND_COLOR_RES_ID + "=" + pushBackgroundColorResId +
                '}';
    }

    public static class IntentBuilder {
        private static final String TAG = "BundleBuilder ";
        private Bundle b;
        private Context ctx;

        private IntentBuilder() {
        }

        public static IntentBuilder getBuilder(Context ctx, String clientId, String userName) {

            IntentBuilder builder = new IntentBuilder();
            builder.b = new Bundle();
            builder.b.putString(ARG_CLIENT_ID, clientId);
            builder.ctx = ctx.getApplicationContext();
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
            b.putBundle(ARG_SET_CHAT_TITLE_STYLE, bundle);
            bundle.putInt(ARG_CHAT_TITLE_TEXT_RES_ID, chatTitleTextResId);
            bundle.putInt(ARG_CHAT_TOOLBAR_COLOR_RES_ID, chatTitleBackgroundColorResId);
            bundle.putInt(ARG_CHAT_TOOLBAR_TEXT_COLOR_RES_ID, chatTitleWidgetsColorResId);
            bundle.putInt(ARG_CHAT_STATUS_BAR_COLOR_RES_ID, chatStatusBarColorResId);
            bundle.putInt(ARG_MENU_ITEM_TEXT_COLOR_RES_ID, menuItemTextColorResId);
            bundle.putInt(ARG_CHAT_TOOLBAR_HINT_TEXT_COLOR, chatToolbarHintTextColor);
            return this;
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
            b.putBundle(ARG_SET_CHAT_BODY_STYLE, bundle);
            bundle.putInt(ARG_CHAT_BACKGROUND_COLOR_RES_ID, chatBackgroundColor);
            bundle.putInt(ARG_CHAT_HIGHLIGHTING_COLOR_RES_ID, chatHighlightingColor);
            bundle.putInt(ARG_CHAT_MESSAGE_INPUT_COLOR_RES_ID, chatMessageInputBackgroundColor);
            bundle.putInt(ARG_CHAT_MESSAGE_INPUT_HINT_TEXT_COLOR_RES_ID, chatMessageHintInputTextColor);
            bundle.putInt(ARG_INCOMING_MESSAGE_BUBBLE_COLOR_RES_ID, incomingMessageBubbleColor);
            bundle.putInt(ARG_OUTGOING_MESSAGE_BUBBLE_COLOR_RES_ID, outgoingMessageBubbleColor);
            bundle.putInt(ARG_INCOMING_MESSAGE_TEXT_COLOR_RES_ID, incomingMessageTextColor);
            bundle.putInt(ARG_OUTGOING_MESSAGE_TEXT_COLOR_RES_ID, outgoingMessageTextColor);
            bundle.putInt(ARG_DEFAULT_INCOMING_MESSAGE_AVATAR_RES_ID, defaultIncomingMessageAvatar);
            bundle.putInt(ARG_IMAGE_PLACEHOLDER_RES_ID, imagePlaceholder);
            bundle.putInt(ARG_CHAT_BODY_ICONS_TINT_RES_ID, chatBodyIconsTint);
            bundle.putInt(ARG_CONNECTION_MESSAGE_TEXT_COLOR_RES_ID, connectionMessageTextColor);
            bundle.putInt(ARG_FILE_BROWSER_DIALOG_STYLE_RES_ID, fileBrowserDialogStyleResId);
            return this;
        }


        public IntentBuilder setPushNotificationStyle(@DrawableRes int defIconResid,
                                                      @StringRes int defTitleResId,
                                                      @ColorRes int pushBackgroundColorResId,
                                                      @ColorRes int nugatPushAccentColorResId) {
            Bundle bundle = new Bundle();
            b.putBundle(ARG_SET_PUSH_NOTIFICATION_STYLE, bundle);
            bundle.putInt(ARG_DEF_PUSH_ICON_RES_ID, defIconResid);
            bundle.putInt(ARG_DEF_TITLE_RES_ID, defTitleResId);
            bundle.putInt(ARG_PUSH_BACKGROUND_COLOR_RES_ID, pushBackgroundColorResId);
            bundle.putInt(ARG_NOUGAT_PUSH_ACCENT_COLOR_RES_ID, nugatPushAccentColorResId);
            return this;
        }

        public IntentBuilder setGoogleAnalyticsEnabled(boolean isEnabled) {
            b.putBoolean(ARG_IS_GA_ENABLED, isEnabled);
            return this;
        }

        public IntentBuilder setWelcomeScreenStyle(
                @DrawableRes int welcomeScreenLogoResId
                , @StringRes int welcomeScreenTitleTextResId
                , @StringRes int welcomeScreenSubtitleTextResId
                , @ColorRes int welcomeScreenTextColorResId
                , int titleSizeInSp
                , int subtitleSizeInSp) {
            Bundle bundle = new Bundle();
            b.putBundle(ARG_SET_WELCOME_SCREEN_STYLE, bundle);
            bundle.putInt(ARG_WELCOME_SCREEN_LOGO_RES_ID, welcomeScreenLogoResId);
            bundle.putInt(ARG_WELCOME_SCREEN_TEXT_COLOR_RES_ID, welcomeScreenTextColorResId);
            bundle.putInt(ARG_WELCOME_SCREEN_TITLE_TEXT_RES_ID, welcomeScreenTitleTextResId);
            bundle.putInt(ARG_WELCOME_SCREEN_SUBTITLE_TEXT_RES_ID, welcomeScreenSubtitleTextResId);
            bundle.putInt(ARG_TITLE_SIZE_IN_SP, titleSizeInSp);
            bundle.putInt(ARG_SUBTITLE_SIZE_IN_SP, subtitleSizeInSp);
            return this;
        }

        public Bundle buildBundle() {
            Bundle b = this.b;
            if (b.getBundle(ARG_SET_WELCOME_SCREEN_STYLE) == null)
                Log.e(TAG, "you must provide welcome screen  attributes. now using default");
            if (b.getBundle(ARG_SET_PUSH_NOTIFICATION_STYLE) == null)
                Log.e(TAG, "you must provide push notifications style. now using default");
            if (b.getBundle(ARG_SET_CHAT_BODY_STYLE) == null)
                Log.e(TAG, "you must chat body style. now using default");
            if (b.getBundle(ARG_SET_CHAT_TITLE_STYLE) == null)
                Log.e(TAG, "you must chat title style. now using default");
            b.putBoolean(ARG_STYLE, true);
            return b;
        }

        public Intent build() {
            Intent i = new Intent(ctx, ChatActivity.class);
            Bundle b = buildBundle();
            if (b.getString(ARG_CLIENT_ID) == null)
                throw new IllegalStateException("client id is obligatory");
            i.putExtra(CHAT_FRAGMENT_BUNDLE, b);
            return i;
        }
    }
}
