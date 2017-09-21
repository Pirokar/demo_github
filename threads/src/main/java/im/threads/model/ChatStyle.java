package im.threads.model;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.text.TextUtils;

import java.io.Serializable;

import im.threads.R;
import im.threads.utils.PrefUtils;

/**
 * Стиль чата.
 * Хранит в себе информацию о расцветках, иконках
 * и других кастомизациях чата.
 * Created by yuri on 08.11.2016.
 */

public class ChatStyle implements Serializable {

    public static final int INVALID = -1;

    public static final int DEFAULT_HISTORY_LOADING_COUNT = 50;

    // chat title style
    @StringRes
    public int chatTitleTextResId = INVALID;
    @ColorRes
    public int chatToolbarColorResId = INVALID;
    @ColorRes
    public int chatStatusBarColorResId = INVALID;
    @ColorRes
    public int menuItemTextColorResId = INVALID;
    @ColorRes
    public int chatToolbarTextColorResId = INVALID;
    @ColorRes
    public int chatToolbarHintTextColor = INVALID;
    public boolean showBackButton;

    // chat body style
    @ColorRes
    public int chatBackgroundColor = INVALID;
    @ColorRes
    public int chatHighlightingColor = INVALID;
    @ColorRes
    public int incomingMessageBubbleColor = INVALID;
    @ColorRes
    public int outgoingMessageBubbleColor = INVALID;
    @DrawableRes
    public int incomingMessageBubbleBackground = INVALID;
    @DrawableRes
    public int outgoingMessageBubbleBackground = INVALID;
    @ColorRes
    public int incomingMessageTextColor = INVALID;
    @ColorRes
    public int outgoingMessageTextColor = INVALID;
    @ColorRes
    public int chatBodyIconsTint = INVALID;
    @ColorRes
    public int connectionMessageTextColor = INVALID;
    @ColorRes
    public int filesAndMediaScreenBackgroundColor = INVALID;
    @ColorRes
    public int iconsAndSeparatorsColor = INVALID;
    @DrawableRes
    public int defaultIncomingMessageAvatar = INVALID;
    @DimenRes
    public int operatorAvatarSize = INVALID;
    @DimenRes
    public int operatorSystemAvatarSize = INVALID;
    @DrawableRes
    public int imagePlaceholder = INVALID;
    @StyleRes
    public int fileBrowserDialogStyleResId = INVALID;
    public boolean showConsultSearching;
    public boolean alwaysScrollToEnd;
    @DrawableRes
    public int scrollDownButtonResId = INVALID;
    @ColorRes
    public int unreadMsgStickerColorResId = INVALID;
    @ColorRes
    public int unreadMsgCountTextColorResId = INVALID;

    // chat input style
    @ColorRes
    public int chatMessageInputColor = INVALID;
    @ColorRes
    public int chatMessageInputHintTextColor = INVALID;
    @ColorRes
    public int inputTextColor = INVALID;
    public String inputTextFont;
    @DrawableRes
    public int attachmentsIconResId = INVALID;
    @DrawableRes
    public int sendMessageIconResId = INVALID;
    @StringRes
    public int inputHint = INVALID;
    @DimenRes
    public int inputHeight = INVALID;
    @DrawableRes
    public int inputBackground = INVALID;

    // push notification style
    @DrawableRes
    public int defPushIconResId;
    @StringRes
    public int defTitleResId;
    @ColorRes
    public int pushBackgroundColorResId;
    @ColorRes
    public int nougatPushAccentColorResId = INVALID;

    // resolve thread request style
    @StringRes
    public int requestToResolveThreadTextResId;
    @StringRes
    public int approveRequestToResolveThreadTextResId;
    @StringRes
    public int denyRequestToResolveThreadTextResId;

    // survey style
    @DrawableRes
    public int binarySurveyLikeUnselectedIconResId;
    @DrawableRes
    public int binarySurveyLikeSelectedIconResId;
    @DrawableRes
    public int binarySurveyDislikeUnselectedIconResId;
    @DrawableRes
    public int binarySurveyDislikeSelectedIconResId;
    @DrawableRes
    public int optionsSurveyUnselectedIconResId;
    @DrawableRes
    public int optionsSurveySelectedIconResId;
    @ColorRes
    public int surveySelectedColorFilterResId = INVALID;
    @ColorRes
    public int surveyUnselectedColorFilterResId = INVALID;
    @ColorRes
    public int surveyTextColorResId;

    // schedule message style
    @ColorRes
    public int scheduleMessageTextColorResId = INVALID;
    @DrawableRes
    public int scheduleMessageIconResId = INVALID;

    // is GoogleAnalytics enabled
    public boolean isGAEnabled;

    // welcome screen style
    @DrawableRes
    public int welcomeScreenLogoResId = INVALID;
    @StringRes
    public int welcomeScreenTitleTextResId = INVALID;
    @StringRes
    public int welcomeScreenSubtitleTextResId = INVALID;
    @ColorRes
    public int welcomeScreenTitleTextColorResId = INVALID;
    @ColorRes
    public int welcomeScreenSubtitleTextColorResId = INVALID;
    public int welcomeScreenTitleSizeInSp;
    public int welcomeScreenSubtitleSizeInSp;

    // set history loading count
    public int historyLoadingCount = 50;

    // set can show specialist onfo
    public boolean canShowSpecialistInfo = true;

    // specify fonts
    public String defaultFontBold;
    public String defaultFontLight;
    public String defaultFontRegular;
    public String toolbarTitleFont;
    public String toolbarSubtitleFont;
    public String placeholderTitleFont;
    public String placeholderSubtitleFont;
    public String inputQuotedMessageAuthorFont;
    public String inputQuotedMessageFont;
    public String bubbleMessageFont;
    public String bubbleTimeFont;
    public String quoteAuthorFont;
    public String quoteMessageFont;
    public String quoteTimeFont;
    public String messageHeaderFont;
    public String specialistConnectTitleFont;
    public String specialistConnectSubtitleFont;
    public String typingFont;
    public String scheduleAlerFont;

    private static volatile ChatStyle instance;

    public static ChatStyle getInstance() {
        ChatStyle localInstance = instance;
        if (localInstance == null) {
            synchronized (ChatStyle.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ChatStyle();
                }
            }
        }
        return localInstance;
    }
    
    private ChatStyle() {}

    public static class ChatStyleBuilder {
        private ChatStyle chatStyle;
        private Context ctx;

        private String clientId;
        private String userName;
        private String data;

        private ChatStyleBuilder() {
        }

        public static ChatStyleBuilder getBuilder(Context ctx, String clientId, String userName) {
            return getBuilder(ctx, clientId, userName, "");
        }

        public static ChatStyleBuilder getBuilder(Context ctx, String clientId, String userName, String data) {
            ChatStyleBuilder builder = new ChatStyleBuilder();
            builder.clientId = clientId;
            builder.userName = userName;
            builder.data = data;
            builder.chatStyle = getInstance();
            builder.ctx = ctx.getApplicationContext();
            return builder;
        }

        public ChatStyleBuilder setChatTitleStyle(
                @StringRes int chatTitleTextResId,
                @ColorRes int chatToolbarColorResId,
                @ColorRes int chatToolbarTextColorResId,
                @ColorRes int chatStatusBarColorResId,
                @ColorRes int menuItemTextColorResId,
                @ColorRes int chatToolbarHintTextColor,
                boolean showBackButton) {
            chatStyle.chatTitleTextResId = chatTitleTextResId;
            chatStyle.chatToolbarColorResId = chatToolbarColorResId != INVALID ? chatToolbarColorResId : R.color.threads_teal_009688;
            chatStyle.chatToolbarTextColorResId = chatToolbarTextColorResId != INVALID ? chatToolbarTextColorResId : R.color.threads_white;
            chatStyle.chatStatusBarColorResId = chatStatusBarColorResId != INVALID ? chatStatusBarColorResId : R.color.threads_teal_004D40;
            chatStyle.menuItemTextColorResId = menuItemTextColorResId;
            chatStyle.chatToolbarHintTextColor = chatToolbarHintTextColor;
            chatStyle.showBackButton = showBackButton;
            return this;
        }

        public ChatStyleBuilder setChatBodyStyle(
                @ColorRes int chatBackgroundColor,
                @ColorRes int chatHighlightingColor,
                @ColorRes int incomingMessageBubbleColor,
                @ColorRes int outgoingMessageBubbleColor,
                @DrawableRes int incomingMessageBubbleBackground,
                @DrawableRes int outgoingMessageBubbleBackground,
                @ColorRes int incomingMessageTextColor,
                @ColorRes int outgoingMessageTextColor,
                @ColorRes int chatBodyIconsTint,
                @ColorRes int connectionMessageTextColor,
                @ColorRes int filesAndMediaScreenBackgroundColor,
                @ColorRes int iconsAndSeparatorsColor,
                @DrawableRes int defaultIncomingMessageAvatar,
                @DimenRes int operatorAvatarSize,
                @DimenRes int operatorSystemAvatarSize,
                @DrawableRes int imagePlaceholder,
                @StyleRes int fileBrowserDialogStyleResId,
                boolean showConsultSearching,
                boolean alwaysScrollToEnd,
                @DrawableRes int scrollDownButtonResId,
                @ColorRes int unreadMsgStickerColorResId,
                @ColorRes int unreadMsgCountTextColorResId) {
            chatStyle.chatBackgroundColor = chatBackgroundColor;
            chatStyle.chatHighlightingColor = chatHighlightingColor != INVALID ? chatHighlightingColor : R.color.threads_blue_transparent_700F87FF;
            chatStyle.incomingMessageBubbleColor = incomingMessageBubbleColor != INVALID ? incomingMessageBubbleColor : R.color.threads_white;
            chatStyle.outgoingMessageBubbleColor = outgoingMessageBubbleColor != INVALID ? outgoingMessageBubbleColor : R.color.threads_blue_3598dc;
            chatStyle.incomingMessageBubbleBackground = incomingMessageBubbleBackground;
            chatStyle.outgoingMessageBubbleBackground = outgoingMessageBubbleBackground;
            chatStyle.incomingMessageTextColor = incomingMessageTextColor != INVALID ? incomingMessageTextColor : R.color.threads_black;
            chatStyle.outgoingMessageTextColor = outgoingMessageTextColor != INVALID ? outgoingMessageTextColor : R.color.threads_white;
            chatStyle.defaultIncomingMessageAvatar = defaultIncomingMessageAvatar;
            chatStyle.operatorAvatarSize = operatorAvatarSize;
            chatStyle.operatorSystemAvatarSize = operatorSystemAvatarSize;
            chatStyle.imagePlaceholder = imagePlaceholder;
            chatStyle.chatBodyIconsTint = chatBodyIconsTint;
            chatStyle.connectionMessageTextColor = connectionMessageTextColor;
            chatStyle.filesAndMediaScreenBackgroundColor = filesAndMediaScreenBackgroundColor;
            chatStyle.iconsAndSeparatorsColor = iconsAndSeparatorsColor != INVALID ? iconsAndSeparatorsColor : R.color.threads_disabled_text_color;
            chatStyle.fileBrowserDialogStyleResId = fileBrowserDialogStyleResId;
            chatStyle.showConsultSearching = showConsultSearching;
            chatStyle.alwaysScrollToEnd = alwaysScrollToEnd;
            chatStyle.scrollDownButtonResId = scrollDownButtonResId;
            chatStyle.unreadMsgStickerColorResId = unreadMsgStickerColorResId;
            chatStyle.unreadMsgCountTextColorResId = unreadMsgCountTextColorResId;
            return this;
        }

        public ChatStyleBuilder setChatInputStyle(
                @ColorRes int chatMessageInputHintTextColor,
                @ColorRes int chatMessageInputColor,
                @ColorRes int inputTextColor,
                String inputTextFont,
                @DrawableRes int attachmentsIconResId,
                @DrawableRes int sendMessageIconResId,
                @StringRes int inputHint,
                @DimenRes int inputHeight,
                @DrawableRes int inputBackground) {
            chatStyle.chatMessageInputColor = chatMessageInputColor;
            chatStyle.chatMessageInputHintTextColor = chatMessageInputHintTextColor;
            chatStyle.inputTextColor = inputTextColor;
            chatStyle.inputTextFont = inputTextFont;
            chatStyle.attachmentsIconResId = attachmentsIconResId;
            chatStyle.sendMessageIconResId = sendMessageIconResId;
            chatStyle.inputHint = inputHint;
            chatStyle.inputHeight = inputHeight;
            chatStyle.inputBackground = inputBackground;
            return this;
        }

        public ChatStyleBuilder setPushNotificationStyle(@DrawableRes int defIconResid,
                                                         @StringRes int defTitleResId,
                                                         @ColorRes int pushBackgroundColorResId,
                                                         @ColorRes int nougatPushAccentColorResId) {
            chatStyle.defPushIconResId = defIconResid != INVALID ? defIconResid : R.drawable.defult_push_icon;
            chatStyle.defTitleResId = defTitleResId != INVALID ? defTitleResId : R.string.lib_app_name;
            chatStyle.pushBackgroundColorResId = pushBackgroundColorResId != INVALID ? pushBackgroundColorResId : R.color.threads_green_dark;
            chatStyle.nougatPushAccentColorResId = nougatPushAccentColorResId;
            return this;
        }

        public ChatStyleBuilder setRequestResolveThreadStyle(@StringRes int requestToResolveThreadTextResId,
                                                             @StringRes int approveRequestToResolveThreadTextResId,
                                                             @StringRes int denyRequestToResolveThreadTextResId) {
            chatStyle.requestToResolveThreadTextResId = requestToResolveThreadTextResId;
            chatStyle.approveRequestToResolveThreadTextResId = approveRequestToResolveThreadTextResId;
            chatStyle.denyRequestToResolveThreadTextResId = denyRequestToResolveThreadTextResId;
            return this;
        }

        public ChatStyleBuilder setSurveyStyle(@DrawableRes int binarySurveyLikeUnselectedIconResId,
                                               @DrawableRes int binarySurveyLikeSelectedIconResId,
                                               @DrawableRes int binarySurveyDislikeUnselectedIconResId,
                                               @DrawableRes int binarySurveyDislikeSelectedIconResId,
                                               @DrawableRes int optionsSurveyUnselectedIconResId,
                                               @DrawableRes int optionsSurveySelectedIconResId,
                                               @ColorRes int surveySelectedColorFilterResId,
                                               @ColorRes int surveyUnselectedColorFilterResId,
                                               @ColorRes int surveyTextColorResId) {
            chatStyle.binarySurveyLikeUnselectedIconResId = binarySurveyLikeUnselectedIconResId != INVALID ? binarySurveyLikeUnselectedIconResId : R.drawable.ic_like_empty_36dp;
            chatStyle.binarySurveyLikeSelectedIconResId = binarySurveyLikeSelectedIconResId != INVALID ? binarySurveyLikeSelectedIconResId : R.drawable.ic_like_full_36dp;
            chatStyle.binarySurveyDislikeUnselectedIconResId = binarySurveyDislikeUnselectedIconResId != INVALID ? binarySurveyDislikeUnselectedIconResId : R.drawable.ic_dislike_empty_36dp;
            chatStyle.binarySurveyDislikeSelectedIconResId = binarySurveyDislikeSelectedIconResId != INVALID ? binarySurveyDislikeSelectedIconResId : R.drawable.ic_dislike_full_36dp;
            chatStyle.optionsSurveyUnselectedIconResId = optionsSurveyUnselectedIconResId != INVALID ? optionsSurveyUnselectedIconResId : R.drawable.ic_star_outline_grey600_24dp;
            chatStyle.optionsSurveySelectedIconResId = optionsSurveySelectedIconResId != INVALID ? optionsSurveySelectedIconResId : R.drawable.ic_star_grey600_24dp;
            chatStyle.surveySelectedColorFilterResId = surveySelectedColorFilterResId;
            chatStyle.surveyUnselectedColorFilterResId = surveyUnselectedColorFilterResId;
            chatStyle.surveyTextColorResId = surveyTextColorResId != INVALID ? surveyTextColorResId : R.color.threads_black;
            return this;
        }

        public ChatStyleBuilder setScheduleMessageStyle(
                @DrawableRes int scheduleMessageIconResId,
                @ColorRes int scheduleMessageTextColor) {
            chatStyle.scheduleMessageIconResId = scheduleMessageIconResId;
            chatStyle.scheduleMessageTextColorResId = scheduleMessageTextColor;
            return this;
        }

        public ChatStyleBuilder setGoogleAnalyticsEnabled(boolean isEnabled) {
            chatStyle.isGAEnabled = isEnabled;
            return this;
        }

        public ChatStyleBuilder setWelcomeScreenStyle(
                @DrawableRes int welcomeScreenLogoResId
                , @StringRes int welcomeScreenTitleTextResId
                , @StringRes int welcomeScreenSubtitleTextResId
                , @ColorRes int welcomeScreenTextColorResId
                , @ColorRes int welcomeScreenSubtitleTextColorResId
                , int welcomeScreenTitleSizeInSp
                , int welcomeScreenSubtitleSizeInSp) {
            chatStyle.welcomeScreenLogoResId = welcomeScreenLogoResId;
            chatStyle.welcomeScreenTitleTextColorResId = welcomeScreenTextColorResId;
            chatStyle.welcomeScreenSubtitleTextColorResId = welcomeScreenSubtitleTextColorResId;
            chatStyle.welcomeScreenTitleTextResId = welcomeScreenTitleTextResId;
            chatStyle.welcomeScreenSubtitleTextResId = welcomeScreenSubtitleTextResId;
            chatStyle.welcomeScreenTitleSizeInSp = welcomeScreenTitleSizeInSp;
            chatStyle.welcomeScreenSubtitleSizeInSp = welcomeScreenSubtitleSizeInSp;
            return this;
        }



        public ChatStyleBuilder setHistoryLoadingCount(Integer count) {
            if (count != null && count > 0) {
                chatStyle.historyLoadingCount = count;
            }
            return this;
        }

        public ChatStyleBuilder setCanShowSpecialistInfo(boolean show) {
            chatStyle.canShowSpecialistInfo = show;
            return this;
        }

        public ChatStyleBuilder setDefaultFontBold(String path) {
            chatStyle.defaultFontBold = path;
            return this;
        }

        public ChatStyleBuilder setDefaultFontLight(String path) {
            chatStyle.defaultFontLight = path;
            return this;
        }

        public ChatStyleBuilder setDefaultFontRegular(String path) {
            chatStyle.defaultFontRegular = path;
            return this;
        }

        public ChatStyleBuilder setToolbarTitleFont(String path) {
            chatStyle.toolbarTitleFont = path;
            return this;
        }

        public ChatStyleBuilder setToolbarSubtitleFont(String path) {
            chatStyle.toolbarSubtitleFont = path;
            return this;
        }

        public ChatStyleBuilder setPlaceholderTitleFont(String path) {
            chatStyle.placeholderTitleFont = path;
            return this;
        }

        public ChatStyleBuilder setPlaceholderSubtitleFont(String path) {
            chatStyle.placeholderSubtitleFont = path;
            return this;
        }

        public ChatStyleBuilder setInputQuotedMessageAuthorFont(String path) {
            chatStyle.inputQuotedMessageAuthorFont = path;
            return this;
        }

        public ChatStyleBuilder setInputQuotedMessageFont(String path) {
            chatStyle.inputQuotedMessageFont = path;
            return this;
        }

        public ChatStyleBuilder setBubbleMessageFont(String path) {
            chatStyle.bubbleMessageFont = path;
            return this;
        }

        public ChatStyleBuilder setBubbleTimeFont(String path) {
            chatStyle.bubbleTimeFont = path;
            return this;
        }

        public ChatStyleBuilder setQuoteAuthorFont(String path) {
            chatStyle.quoteAuthorFont = path;
            return this;
        }

        public ChatStyleBuilder setQuoteMessageFont(String path) {
            chatStyle.quoteMessageFont = path;
            return this;
        }

        public ChatStyleBuilder setQuoteTimeFont(String path) {
            chatStyle.quoteTimeFont = path;
            return this;
        }

        public ChatStyleBuilder setMessageHeaderFont(String path) {
            chatStyle.messageHeaderFont = path;
            return this;
        }

        public ChatStyleBuilder setSpecialistConnectTitleFont(String path) {
            chatStyle.specialistConnectTitleFont = path;
            return this;
        }

        public ChatStyleBuilder setSpecialistConnectSubtitleFont(String path) {
            chatStyle.specialistConnectSubtitleFont = path;
            return this;
        }

        public ChatStyleBuilder setTypingFont(String path) {
            chatStyle.typingFont = path;
            return this;
        }

        public ChatStyleBuilder setScheduleAlertFont(String path) {
            chatStyle.scheduleAlerFont = path;
            return this;
        }

        public ChatStyle build() {
            if (TextUtils.isEmpty(clientId) && PrefUtils.getClientID(ctx).equals("")) {
                throw new IllegalStateException(ctx.getString(R.string.lib_invalid_client_id));
            }

            PrefUtils.setIncomingStyle(ctx, chatStyle);

            PrefUtils.setNewClientId(ctx, clientId);
            PrefUtils.setUserName(ctx, userName);
            PrefUtils.setData(ctx, data);

            return chatStyle;
        }
    }

    // to prevent create a new instance of the class while deserialization
    protected Object readResolve() {
        return getInstance();
    }
}
