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

import im.threads.R;
import im.threads.activities.ChatActivity;
import im.threads.utils.PrefUtils;

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
    private static final String ARG_SET_SCHEDULE_MESSAGE_STYLE = "setScheduleMessageStyle";
    private static final String ARG_STYLE = "style";

    private static final String ARG_WELCOME_SCREEN_LOGO_RES_ID = "welcomeScreenLogoResId";
    private static final String ARG_WELCOME_SCREEN_TEXT_COLOR_RES_ID = "welcomeScreenTextColorResId";
    private static final String ARG_WELCOME_SCREEN_TITLE_TEXT_RES_ID = "welcomeScreenTitleTextResId";
    private static final String ARG_WELCOME_SCREEN_SUBTITLE_TEXT_RES_ID = "welcomeScreenSubtitleTextResId";
    private static final String ARG_WELCOME_SCREEN_TITLE_SIZE_IN_SP = "welcomeScreenTitleSizeInSp";
    private static final String ARG_WELCOME_SCREEN_SUBTITLE_SIZE_IN_SP = "welcomeScreenSubtitleSizeInSp";
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
    private static final String ARG_FILES_AND_MEDIA_SCREEN_BACKGROUND_COLOR_RES_ID = "filesAndMediaScreenBackgroundColor";
    private static final String ARG_FILE_BROWSER_DIALOG_STYLE_RES_ID = "fileBrowserDialogStyleResId";
    private static final String ARG_CHAT_TITLE_TEXT_RES_ID = "chatTitleTextResId";
    private static final String ARG_CHAT_TOOLBAR_COLOR_RES_ID = "chatToolbarColorResId";
    private static final String ARG_CHAT_TOOLBAR_TEXT_COLOR_RES_ID = "chatToolbarTextColorResId";
    private static final String ARG_CHAT_STATUS_BAR_COLOR_RES_ID = "chatStatusBarColorResId";
    private static final String ARG_MENU_ITEM_TEXT_COLOR_RES_ID = "menuItemTextColorResId";
    private static final String ARG_CHAT_TOOLBAR_HINT_TEXT_COLOR = "chatToolbarHintTextColor";
    private static final String ARG_SCHEDULE_MESSAGE_ICON_RES_ID = "scheduleMessageIconResId";
    private static final String ARG_SCHEDULE_MESSAGE_TEXT_COLOR_RES_ID = "scheduleMessageTextColorResId";
    private static final String ARG_IS_GA_ENABLED = "isGAEnabled";
    private static final String ARG_SHOW_CONSULT_SEARCHING = "showConsultSearching";
    private static final String ARG_SHOW_BACK_BUTTON = "showBackButton";
    private static final String ARG_INPUT_TEXT_COLOR_RES_ID = "inputTextColor";
    private static final String ARG_INPUT_TEXT_FONT_PATH = "inputTextFont";
    private static final String SETTING_HISTORY_LOADING_COUNT = "setting@historyLoadingCount";
    private static final String SETTING_CAN_SHOW_SPECIALIST_INFO = "setting@canShowSpecialistInfo";

    public static final int INVALID = -1;

    public static final int DEFAULT_HISTORY_LOADING_COUNT = 50;
    public static final boolean DEFAULT_CAN_SHOW_SPECIALIST_INFO = true;

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
    public final int welcomeScreenTitleSizeInSp;
    public final int welcomeScreenSubtitleSizeInSp;
    @ColorRes
    public final int chatBodyIconsTint;
    @ColorRes
    public final int connectionMessageTextColor;
    @ColorRes
    public final int filesAndMediaScreenBackgroundColor;
    @StyleRes
    public final int fileBrowserDialogStyleResId;
    @ColorRes
    public final int pushBackgroundColorResId;
    @ColorRes
    public final int scheduleMessageTextColorResId;
    @DrawableRes
    public final int scheduleMessageIconResId;
    public final boolean showConsultSearching;
    public final boolean showBackButton;
    public final String inputTextFont;
    @ColorRes
    public final int inputTextColor;
    public int historyLoadingCount;
    public boolean canShowSpecialistInfo;

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
                     int filesAndMediaScreenBackgroundColor,
                     int defaultIncomingMessageAvatar,
                     int imagePlaceholder,
                     int fileBrowserDialogStyleResId,
                     int chatTitleTextResId,
                     int chatToolbarColorResId,
                     int chatToolbarTextColorResId,
                     int chatStatusBarColorResId,
                     int menuItemTextColorResId,
                     int chatToolbarHintTextColor,
                     boolean showBackButton,
                     boolean showConsultSearching,
                     boolean isGAEnabled,
                     int defPushIconResid,
                     int defTitleResId,
                     int pushBackgroundColorResId,
                     int nugatPushAccentColorResId,
                     int welcomeScreenLogoResId,
                     int welcomeScreenTitleTextResId,
                     int welcomeScreenSubtitleTextResId,
                     int welcomeScreenTextColorResId,
                     int welcomeScreenTitleSizeInSp,
                     int welcomeScreenSubtitleSizeInSp,
                     int scheduleMessageTextColorResId,
                     int scheduleMessageIconResId,
                     String inputTextFont,
                     int inputTextColor,
                     int historyLoadingCount,
                     boolean canShowSpecialistInfo) {
        this.chatToolbarColorResId = chatToolbarColorResId == INVALID ? R.color.teal_009688 : chatToolbarColorResId;
        this.chatTitleTextResId = chatTitleTextResId;
        this.chatToolbarTextColorResId = chatToolbarTextColorResId == INVALID ? R.color.white : chatToolbarTextColorResId;
        this.chatBackgroundColor = chatBackgroundColor == INVALID ? R.color.blue_eff3f8 : chatBackgroundColor;
        this.chatMessageInputColor = chatMessageInputColor;
        this.incomingMessageBubbleColor = incomingMessageBubbleColor == INVALID ? R.color.white : incomingMessageBubbleColor;
        this.outgoingMessageBubbleColor = outgoingMessageBubbleColor == INVALID ? R.color.blue_3598dc : outgoingMessageBubbleColor;
        this.incomingMessageTextColor = incomingMessageTextColor == INVALID ? R.color.black : incomingMessageTextColor;
        this.outgoingMessageTextColor = outgoingMessageTextColor == INVALID ? R.color.white : outgoingMessageTextColor;
        this.defaultIncomingMessageAvatar = defaultIncomingMessageAvatar;
        this.imagePlaceholder = imagePlaceholder;
        this.defPushIconResid = defPushIconResid;
        this.defTitleResId = defTitleResId;
        this.isGAEnabled = isGAEnabled;
        this.welcomeScreenLogoResId = welcomeScreenLogoResId;
        this.welcomeScreenTitleTextResId = welcomeScreenTitleTextResId;
        this.welcomeScreenSubtitleTextResId = welcomeScreenSubtitleTextResId;
        this.welcomeScreenTextColorResId = welcomeScreenTextColorResId == INVALID ? R.color.black : welcomeScreenTextColorResId ;
        this.welcomeScreenTitleSizeInSp = welcomeScreenTitleSizeInSp;
        this.welcomeScreenSubtitleSizeInSp = welcomeScreenSubtitleSizeInSp;
        this.chatBodyIconsTint = chatBodyIconsTint == INVALID ? R.color.blue_0F87FF : chatBodyIconsTint;
        this.connectionMessageTextColor = connectionMessageTextColor == INVALID ? R.color.blue_grey_607d8b : connectionMessageTextColor;
        this.filesAndMediaScreenBackgroundColor = filesAndMediaScreenBackgroundColor;
        this.pushBackgroundColorResId = pushBackgroundColorResId;
        this.nugatPushAccentColorResId = nugatPushAccentColorResId;
        this.chatStatusBarColorResId = chatStatusBarColorResId == INVALID ? R.color.teal_004D40 : chatStatusBarColorResId;
        this.fileBrowserDialogStyleResId = fileBrowserDialogStyleResId;
        this.chatMessageInputHintTextColor = chatMessageInputHintTextColor;
        this.menuItemTextColorResId = menuItemTextColorResId;
        this.chatToolbarHintTextColor = chatToolbarHintTextColor;
        this.chatHighlightingColor = chatHighlightingColor == INVALID ? R.color.blue_transparent_700F87FF : chatHighlightingColor;
        this.scheduleMessageTextColorResId = scheduleMessageTextColorResId;
        this.scheduleMessageIconResId = scheduleMessageIconResId;
        this.showConsultSearching = showConsultSearching;
        this.showBackButton = showBackButton;
        this.inputTextFont = inputTextFont;
        this.inputTextColor = inputTextColor;
        this.historyLoadingCount = historyLoadingCount;
        this.canShowSpecialistInfo = canShowSpecialistInfo;
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
        Bundle scheduleMessageStyle = b.getBundle(ARG_SET_SCHEDULE_MESSAGE_STYLE);
        boolean isScheduleMessageStyleExists = scheduleMessageStyle != null;
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
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_FILES_AND_MEDIA_SCREEN_BACKGROUND_COLOR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_DEFAULT_INCOMING_MESSAGE_AVATAR_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_IMAGE_PLACEHOLDER_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_FILE_BROWSER_DIALOG_STYLE_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_TITLE_TEXT_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_TOOLBAR_COLOR_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_TOOLBAR_TEXT_COLOR_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_STATUS_BAR_COLOR_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_MENU_ITEM_TEXT_COLOR_RES_ID) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getInt(ARG_CHAT_TOOLBAR_HINT_TEXT_COLOR) : INVALID,
                isChatTitleStyleExists ? chatTitleStyle.getBoolean(ARG_SHOW_BACK_BUTTON) : false,
                isChatBodyStyleExists ? chatBodyStyle.getBoolean(ARG_SHOW_CONSULT_SEARCHING) : true,
                isGaEnabled,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt(ARG_DEF_PUSH_ICON_RES_ID) : INVALID,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt(ARG_DEF_TITLE_RES_ID) : INVALID,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt(ARG_PUSH_BACKGROUND_COLOR_RES_ID) : INVALID,
                isPushNotificationStyleExists ? pushNotificationStyle.getInt(ARG_NOUGAT_PUSH_ACCENT_COLOR_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_LOGO_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_TITLE_TEXT_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_SUBTITLE_TEXT_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_TEXT_COLOR_RES_ID) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_TITLE_SIZE_IN_SP) : INVALID,
                isWeclomeExists ? welcomScreenStyle.getInt(ARG_WELCOME_SCREEN_SUBTITLE_SIZE_IN_SP) : INVALID,
                isScheduleMessageStyleExists ? scheduleMessageStyle.getInt(ARG_SCHEDULE_MESSAGE_TEXT_COLOR_RES_ID) : INVALID,
                isScheduleMessageStyleExists ? scheduleMessageStyle.getInt(ARG_SCHEDULE_MESSAGE_ICON_RES_ID) : INVALID,
                isChatBodyStyleExists ? chatBodyStyle.getString(ARG_INPUT_TEXT_FONT_PATH) : null,
                isChatBodyStyleExists ? chatBodyStyle.getInt(ARG_INPUT_TEXT_COLOR_RES_ID) : INVALID,
                b.getInt(SETTING_HISTORY_LOADING_COUNT, DEFAULT_HISTORY_LOADING_COUNT),
                b.getBoolean(SETTING_CAN_SHOW_SPECIALIST_INFO, DEFAULT_CAN_SHOW_SPECIALIST_INFO)
        );
    }

    @Override
    public String toString() {
        return "ChatStyle{" +
                "chatTitleTextResId=" + chatTitleTextResId +
                ", chatToolbarColorResId=" + chatToolbarColorResId +
                ", chatStatusBarColorResId=" + chatStatusBarColorResId +
                ", menuItemTextColorResId=" + menuItemTextColorResId +
                ", chatToolbarTextColorResId=" + chatToolbarTextColorResId +
                ", chatToolbarHintTextColor=" + chatToolbarHintTextColor +
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
                ", welcomeScreenTitleSizeInSp=" + welcomeScreenTitleSizeInSp +
                ", welcomeScreenSubtitleSizeInSp=" + welcomeScreenSubtitleSizeInSp +
                ", chatBodyIconsTint=" + chatBodyIconsTint +
                ", connectionMessageTextColor=" + connectionMessageTextColor +
                ", filesAndMediaScreenBackgroundColor=" + filesAndMediaScreenBackgroundColor +
                ", fileBrowserDialogStyleResId=" + fileBrowserDialogStyleResId +
                ", pushBackgroundColorResId=" + pushBackgroundColorResId +
                ", scheduleMessageTextColorResId=" + scheduleMessageTextColorResId +
                ", scheduleMessageIconResId=" + scheduleMessageIconResId +
                ", showConsultSearching=" + showConsultSearching +
                ", showBackButton=" + showBackButton +
                ", inputTextFont='" + inputTextFont + '\'' +
                ", inputTextColor=" + inputTextColor +
                ", historyLoadingCount=" + historyLoadingCount +
                ", canShowSpecialistInfo=" + canShowSpecialistInfo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatStyle chatStyle = (ChatStyle) o;

        if (chatTitleTextResId != chatStyle.chatTitleTextResId) return false;
        if (chatToolbarColorResId != chatStyle.chatToolbarColorResId) return false;
        if (chatStatusBarColorResId != chatStyle.chatStatusBarColorResId) return false;
        if (menuItemTextColorResId != chatStyle.menuItemTextColorResId) return false;
        if (chatToolbarTextColorResId != chatStyle.chatToolbarTextColorResId) return false;
        if (chatToolbarHintTextColor != chatStyle.chatToolbarHintTextColor) return false;
        if (chatBackgroundColor != chatStyle.chatBackgroundColor) return false;
        if (chatHighlightingColor != chatStyle.chatHighlightingColor) return false;
        if (chatMessageInputColor != chatStyle.chatMessageInputColor) return false;
        if (chatMessageInputHintTextColor != chatStyle.chatMessageInputHintTextColor) return false;
        if (incomingMessageBubbleColor != chatStyle.incomingMessageBubbleColor) return false;
        if (outgoingMessageBubbleColor != chatStyle.outgoingMessageBubbleColor) return false;
        if (incomingMessageTextColor != chatStyle.incomingMessageTextColor) return false;
        if (outgoingMessageTextColor != chatStyle.outgoingMessageTextColor) return false;
        if (defaultIncomingMessageAvatar != chatStyle.defaultIncomingMessageAvatar) return false;
        if (imagePlaceholder != chatStyle.imagePlaceholder) return false;
        if (defPushIconResid != chatStyle.defPushIconResid) return false;
        if (nugatPushAccentColorResId != chatStyle.nugatPushAccentColorResId) return false;
        if (defTitleResId != chatStyle.defTitleResId) return false;
        if (isGAEnabled != chatStyle.isGAEnabled) return false;
        if (welcomeScreenLogoResId != chatStyle.welcomeScreenLogoResId) return false;
        if (welcomeScreenTitleTextResId != chatStyle.welcomeScreenTitleTextResId) return false;
        if (welcomeScreenSubtitleTextResId != chatStyle.welcomeScreenSubtitleTextResId)
            return false;
        if (welcomeScreenTextColorResId != chatStyle.welcomeScreenTextColorResId) return false;
        if (welcomeScreenTitleSizeInSp != chatStyle.welcomeScreenTitleSizeInSp) return false;
        if (welcomeScreenSubtitleSizeInSp != chatStyle.welcomeScreenSubtitleSizeInSp) return false;
        if (chatBodyIconsTint != chatStyle.chatBodyIconsTint) return false;
        if (connectionMessageTextColor != chatStyle.connectionMessageTextColor) return false;
        if (filesAndMediaScreenBackgroundColor != chatStyle.filesAndMediaScreenBackgroundColor)
            return false;
        if (fileBrowserDialogStyleResId != chatStyle.fileBrowserDialogStyleResId) return false;
        if (pushBackgroundColorResId != chatStyle.pushBackgroundColorResId) return false;
        if (scheduleMessageTextColorResId != chatStyle.scheduleMessageTextColorResId) return false;
        if (scheduleMessageIconResId != chatStyle.scheduleMessageIconResId) return false;
        if (showConsultSearching != chatStyle.showConsultSearching) return false;
        if (showBackButton != chatStyle.showBackButton) return false;
        if (inputTextColor != chatStyle.inputTextColor) return false;
        if (historyLoadingCount != chatStyle.historyLoadingCount) return false;
        if (canShowSpecialistInfo != chatStyle.canShowSpecialistInfo) return false;
        return inputTextFont.equals(chatStyle.inputTextFont);

    }

    @Override
    public int hashCode() {
        int result = chatTitleTextResId;
        result = 31 * result + chatToolbarColorResId;
        result = 31 * result + chatStatusBarColorResId;
        result = 31 * result + menuItemTextColorResId;
        result = 31 * result + chatToolbarTextColorResId;
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
        result = 31 * result + welcomeScreenTitleSizeInSp;
        result = 31 * result + welcomeScreenSubtitleSizeInSp;
        result = 31 * result + chatBodyIconsTint;
        result = 31 * result + connectionMessageTextColor;
        result = 31 * result + filesAndMediaScreenBackgroundColor;
        result = 31 * result + fileBrowserDialogStyleResId;
        result = 31 * result + pushBackgroundColorResId;
        result = 31 * result + scheduleMessageTextColorResId;
        result = 31 * result + scheduleMessageIconResId;
        result = 31 * result + (showConsultSearching ? 1 : 0);
        result = 31 * result + (showBackButton ? 1 : 0);
        result = 31 * result + inputTextFont.hashCode();
        result = 31 * result + inputTextColor;
        result = 31 * result + historyLoadingCount;
        result = 31 * result + (canShowSpecialistInfo ? 1 : 0);
        return result;
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
                @ColorRes int chatToolbarColorResId,
                @ColorRes int chatToolbarTextColorResId,
                @ColorRes int chatStatusBarColorResId,
                @ColorRes int menuItemTextColorResId,
                @ColorRes int chatToolbarHintTextColor,
                boolean showBackButton) {
            Bundle bundle = new Bundle();
            b.putBundle(ARG_SET_CHAT_TITLE_STYLE, bundle);
            bundle.putInt(ARG_CHAT_TITLE_TEXT_RES_ID, chatTitleTextResId);
            bundle.putInt(ARG_CHAT_TOOLBAR_COLOR_RES_ID, chatToolbarColorResId);
            bundle.putInt(ARG_CHAT_TOOLBAR_TEXT_COLOR_RES_ID, chatToolbarTextColorResId);
            bundle.putInt(ARG_CHAT_STATUS_BAR_COLOR_RES_ID, chatStatusBarColorResId);
            bundle.putInt(ARG_MENU_ITEM_TEXT_COLOR_RES_ID, menuItemTextColorResId);
            bundle.putInt(ARG_CHAT_TOOLBAR_HINT_TEXT_COLOR, chatToolbarHintTextColor);
            bundle.putBoolean(ARG_SHOW_BACK_BUTTON, showBackButton);
            return this;
        }

        @Deprecated
        public IntentBuilder setChatBodyStyle(
                @ColorRes int chatBackgroundColor,
                @ColorRes int chatHighlightingColor,
                @ColorRes int chatMessageInputHintTextColor,
                @ColorRes int chatMessageInputColor,
                @ColorRes int incomingMessageBubbleColor,
                @ColorRes int outgoingMessageBubbleColor,
                @ColorRes int incomingMessageTextColor,
                @ColorRes int outgoingMessageTextColor,
                @ColorRes int chatBodyIconsTint,
                @ColorRes int connectionMessageTextColor,
                @ColorRes int filesAndMediaScreenBackgroundColor,
                @DrawableRes int defaultIncomingMessageAvatar,
                @DrawableRes int imagePlaceholder,
                @StyleRes int fileBrowserDialogStyleResId,
                boolean showConsultSearching) {
            Bundle bundle = new Bundle();
            b.putBundle(ARG_SET_CHAT_BODY_STYLE, bundle);
            bundle.putInt(ARG_CHAT_BACKGROUND_COLOR_RES_ID, chatBackgroundColor);
            bundle.putInt(ARG_CHAT_HIGHLIGHTING_COLOR_RES_ID, chatHighlightingColor);
            bundle.putInt(ARG_CHAT_MESSAGE_INPUT_COLOR_RES_ID, chatMessageInputColor);
            bundle.putInt(ARG_CHAT_MESSAGE_INPUT_HINT_TEXT_COLOR_RES_ID, chatMessageInputHintTextColor);
            bundle.putInt(ARG_INCOMING_MESSAGE_BUBBLE_COLOR_RES_ID, incomingMessageBubbleColor);
            bundle.putInt(ARG_OUTGOING_MESSAGE_BUBBLE_COLOR_RES_ID, outgoingMessageBubbleColor);
            bundle.putInt(ARG_INCOMING_MESSAGE_TEXT_COLOR_RES_ID, incomingMessageTextColor);
            bundle.putInt(ARG_OUTGOING_MESSAGE_TEXT_COLOR_RES_ID, outgoingMessageTextColor);
            bundle.putInt(ARG_DEFAULT_INCOMING_MESSAGE_AVATAR_RES_ID, defaultIncomingMessageAvatar);
            bundle.putInt(ARG_IMAGE_PLACEHOLDER_RES_ID, imagePlaceholder);
            bundle.putInt(ARG_CHAT_BODY_ICONS_TINT_RES_ID, chatBodyIconsTint);
            bundle.putInt(ARG_CONNECTION_MESSAGE_TEXT_COLOR_RES_ID, connectionMessageTextColor);
            bundle.putInt(ARG_FILES_AND_MEDIA_SCREEN_BACKGROUND_COLOR_RES_ID, filesAndMediaScreenBackgroundColor);
            bundle.putInt(ARG_FILE_BROWSER_DIALOG_STYLE_RES_ID, fileBrowserDialogStyleResId);
            bundle.putBoolean(ARG_SHOW_CONSULT_SEARCHING, showConsultSearching);
            bundle.putInt(ARG_INPUT_TEXT_COLOR_RES_ID, -1);
            bundle.putString(ARG_INPUT_TEXT_FONT_PATH, null);
            return this;
        }

        public IntentBuilder setChatBodyStyle(
                @ColorRes int chatBackgroundColor,
                @ColorRes int chatHighlightingColor,
                @ColorRes int chatMessageInputHintTextColor,
                @ColorRes int chatMessageInputColor,
                @ColorRes int incomingMessageBubbleColor,
                @ColorRes int outgoingMessageBubbleColor,
                @ColorRes int incomingMessageTextColor,
                @ColorRes int outgoingMessageTextColor,
                @ColorRes int chatBodyIconsTint,
                @ColorRes int connectionMessageTextColor,
                @ColorRes int filesAndMediaScreenBackgroundColor,
                @DrawableRes int defaultIncomingMessageAvatar,
                @DrawableRes int imagePlaceholder,
                @StyleRes int fileBrowserDialogStyleResId,
                boolean showConsultSearching,
                @ColorRes int inputTextColor,
                String inputTextFont) {
            Bundle bundle = new Bundle();
            b.putBundle(ARG_SET_CHAT_BODY_STYLE, bundle);
            bundle.putInt(ARG_CHAT_BACKGROUND_COLOR_RES_ID, chatBackgroundColor);
            bundle.putInt(ARG_CHAT_HIGHLIGHTING_COLOR_RES_ID, chatHighlightingColor);
            bundle.putInt(ARG_CHAT_MESSAGE_INPUT_COLOR_RES_ID, chatMessageInputColor);
            bundle.putInt(ARG_CHAT_MESSAGE_INPUT_HINT_TEXT_COLOR_RES_ID, chatMessageInputHintTextColor);
            bundle.putInt(ARG_INCOMING_MESSAGE_BUBBLE_COLOR_RES_ID, incomingMessageBubbleColor);
            bundle.putInt(ARG_OUTGOING_MESSAGE_BUBBLE_COLOR_RES_ID, outgoingMessageBubbleColor);
            bundle.putInt(ARG_INCOMING_MESSAGE_TEXT_COLOR_RES_ID, incomingMessageTextColor);
            bundle.putInt(ARG_OUTGOING_MESSAGE_TEXT_COLOR_RES_ID, outgoingMessageTextColor);
            bundle.putInt(ARG_DEFAULT_INCOMING_MESSAGE_AVATAR_RES_ID, defaultIncomingMessageAvatar);
            bundle.putInt(ARG_IMAGE_PLACEHOLDER_RES_ID, imagePlaceholder);
            bundle.putInt(ARG_CHAT_BODY_ICONS_TINT_RES_ID, chatBodyIconsTint);
            bundle.putInt(ARG_CONNECTION_MESSAGE_TEXT_COLOR_RES_ID, connectionMessageTextColor);
            bundle.putInt(ARG_FILES_AND_MEDIA_SCREEN_BACKGROUND_COLOR_RES_ID, filesAndMediaScreenBackgroundColor);
            bundle.putInt(ARG_FILE_BROWSER_DIALOG_STYLE_RES_ID, fileBrowserDialogStyleResId);
            bundle.putBoolean(ARG_SHOW_CONSULT_SEARCHING, showConsultSearching);
            bundle.putInt(ARG_INPUT_TEXT_COLOR_RES_ID, inputTextColor);
            bundle.putString(ARG_INPUT_TEXT_FONT_PATH, inputTextFont);
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

        public IntentBuilder setScheduleMessageStyle(
                @DrawableRes int scheduleMessageIconResId,
                @ColorRes int scheduleMessageTextColor
        ) {
            Bundle bundle = new Bundle();
            b.putBundle(ARG_SET_SCHEDULE_MESSAGE_STYLE, bundle);
            bundle.putInt(ARG_SCHEDULE_MESSAGE_ICON_RES_ID, scheduleMessageIconResId);
            bundle.putInt(ARG_SCHEDULE_MESSAGE_TEXT_COLOR_RES_ID, scheduleMessageTextColor);
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
                , int welcomeScreenTitleSizeInSp
                , int welcomeScreenSubtitleSizeInSp) {
            Bundle bundle = new Bundle();
            b.putBundle(ARG_SET_WELCOME_SCREEN_STYLE, bundle);
            bundle.putInt(ARG_WELCOME_SCREEN_LOGO_RES_ID, welcomeScreenLogoResId);
            bundle.putInt(ARG_WELCOME_SCREEN_TEXT_COLOR_RES_ID, welcomeScreenTextColorResId);
            bundle.putInt(ARG_WELCOME_SCREEN_TITLE_TEXT_RES_ID, welcomeScreenTitleTextResId);
            bundle.putInt(ARG_WELCOME_SCREEN_SUBTITLE_TEXT_RES_ID, welcomeScreenSubtitleTextResId);
            bundle.putInt(ARG_WELCOME_SCREEN_TITLE_SIZE_IN_SP, welcomeScreenTitleSizeInSp);
            bundle.putInt(ARG_WELCOME_SCREEN_SUBTITLE_SIZE_IN_SP, welcomeScreenSubtitleSizeInSp);
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
            if (b.getBundle(ARG_SET_SCHEDULE_MESSAGE_STYLE) == null)
                Log.e(TAG, "you must provide chat schedule message style. now using default");
            b.putBoolean(ARG_STYLE, true);
            return b;
        }

        public IntentBuilder setHistoryLoadingCount(Integer count) {
            if (count != null && count > 0) {
                b.putInt(SETTING_HISTORY_LOADING_COUNT, count);
            }
            return this;
        }

        public IntentBuilder setCanShowSpecialistInfo(boolean show) {
            b.putBoolean(SETTING_CAN_SHOW_SPECIALIST_INFO, show);
            return this;
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

    public static ChatStyle getStyleFromBundleWithThrow(Context context, Bundle bundle) {
        if (bundle != null) {
            if (bundle.getString("clientId") == null && PrefUtils.getClientID(context).equals(""))
                throw new IllegalStateException("you must provide valid client id," +
                        "\r\n it is now null or it'ts length < 5");
            if (bundle.getBoolean("style", false)) {
                ChatStyle style = ChatStyle.styleFromBundle(bundle);
                PrefUtils.setIncomingStyle(context, style);
            }
            if (bundle.getString("userName") != null)
                PrefUtils.setUserName(context, bundle.getString("userName"));
        }
        return PrefUtils.getIncomingStyle(context);
    }
}
