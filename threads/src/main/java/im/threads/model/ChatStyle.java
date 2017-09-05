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
    public static final boolean DEFAULT_CAN_SHOW_SPECIALIST_INFO = true;

    @StringRes
    public int chatTitleTextResId;
    @ColorRes
    public int chatToolbarColorResId;
    @ColorRes
    public int chatStatusBarColorResId;
    @ColorRes
    public int menuItemTextColorResId;
    @ColorRes
    public int chatToolbarTextColorResId;
    @ColorRes
    public int chatToolbarHintTextColor;
    @ColorRes
    public int chatBackgroundColor;
    @ColorRes
    public int chatHighlightingColor;
    @ColorRes
    public int chatMessageInputColor;
    @ColorRes
    public int chatMessageInputHintTextColor;
    @ColorRes
    public int incomingMessageBubbleColor;
    @ColorRes
    public int outgoingMessageBubbleColor;
    @ColorRes
    public int incomingMessageTextColor;
    @ColorRes
    public int outgoingMessageTextColor;
    @DrawableRes
    public int defaultIncomingMessageAvatar;
    @DimenRes
    public int operatorAvatarSize;
    @DimenRes
    public int operatorSystemAvatarSize;
    @DrawableRes
    public int imagePlaceholder;
    @DrawableRes
    public int defPushIconResId;
    @ColorRes
    public int nougatPushAccentColorResId;
    @StringRes
    public int defTitleResId;
    public boolean isGAEnabled;
    @DrawableRes
    public int welcomeScreenLogoResId;
    @StringRes
    public int welcomeScreenTitleTextResId;
    @StringRes
    public int welcomeScreenSubtitleTextResId;
    @ColorRes
    public int welcomeScreenTextColorResId;
    public int welcomeScreenTitleSizeInSp;
    public int welcomeScreenSubtitleSizeInSp;
    @ColorRes
    public int chatBodyIconsTint;
    @ColorRes
    public int connectionMessageTextColor;
    @ColorRes
    public int filesAndMediaScreenBackgroundColor;
    @StyleRes
    public int fileBrowserDialogStyleResId;
    @ColorRes
    public int pushBackgroundColorResId;
    @ColorRes
    public int scheduleMessageTextColorResId;
    @DrawableRes
    public int scheduleMessageIconResId;
    public boolean showConsultSearching;
    public boolean showBackButton;
    public boolean alwaysScrollToEnd;
    public String inputTextFont;
    @ColorRes
    public int inputTextColor;
    public int historyLoadingCount;
    public boolean canShowSpecialistInfo;

    //resolve thread request styles
    @StringRes
    public int requestToResolveThreadTextResId;
    @StringRes
    public int approveRequestToResolveThreadTextResId;
    @StringRes
    public int denyRequestToResolveThreadTextResId;

    // Survey
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
    public int surveySelectedColorFilterResId;
    @ColorRes
    public int surveyUnselectedColorFilterResId;
    @ColorRes
    public int surveyTextColorResId;

    // Scroll down button
    @DrawableRes
    public int scrollDownButtonResId;
    @ColorRes
    public int unreadMsgStickerColorResId;
    @ColorRes
    public int unreadMsgCountTextColorResId;

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
                     @DimenRes int operatorAvatarSize,
                     @DimenRes int operatorSystemAvatarSize,
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
                     boolean alwaysScrollToEnd,
                     boolean isGAEnabled,
                     int defPushIconResId,
                     int defTitleResId,
                     int pushBackgroundColorResId,
                     int nougatPushAccentColorResId,
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
                     boolean canShowSpecialistInfo,
                     int requestToResolveThreadTextResId,
                     int approveRequestToResolveThreadTextResId,
                     int denyRequestToResolveThreadTextResId,
                     @DrawableRes int binarySurveyLikeUnselectedIconResId,
                     @DrawableRes int binarySurveyLikeSelectedIconResId,
                     @DrawableRes int binarySurveyDislikeUnselectedIconResId,
                     @DrawableRes int binarySurveyDislikeSelectedIconResId,
                     @DrawableRes int optionsSurveyUnselectedIconResId,
                     @DrawableRes int optionsSurveySelectedIconResId,
                     @ColorRes int surveySelectedColorFilterResId,
                     @ColorRes int surveyUnselectedColorFilterResId,
                     @ColorRes int surveyTextColorResId,
                     @DrawableRes int scrollDownButtonResId,
                     @ColorRes int unreadMsgStickerColorResId,
                     @ColorRes int unreadMsgCountTextColorResId,
                     String defaultFontBold,
                     String defaultFontLight,
                     String defaultFontRegular,
                     String toolbarTitleFont,
                     String toolbarSubtitleFont,
                     String placeholderTitleFont,
                     String placeholderSubtitleFont,
                     String inputQuotedMessageFont,
                     String inputQuotedMessageAuthorFont,
                     String bubbleMessageFont,
                     String bubbleTimeFont,
                     String quoteAuthorFont,
                     String quoteMessageFont,
                     String quoteTimeFont,
                     String messageHeaderFont,
                     String specialistConnectTitleFont,
                     String specialistConnectSubtitleFont,
                     String typingFont,
                     String scheduleAlerFont
    ) {
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
        this.operatorAvatarSize = operatorAvatarSize;
        this.operatorSystemAvatarSize = operatorSystemAvatarSize;
        this.imagePlaceholder = imagePlaceholder;
        this.defPushIconResId = defPushIconResId;
        this.defTitleResId = defTitleResId;
        this.isGAEnabled = isGAEnabled;
        this.alwaysScrollToEnd = alwaysScrollToEnd;
        this.welcomeScreenLogoResId = welcomeScreenLogoResId;
        this.welcomeScreenTitleTextResId = welcomeScreenTitleTextResId;
        this.welcomeScreenSubtitleTextResId = welcomeScreenSubtitleTextResId;
        this.welcomeScreenTextColorResId = welcomeScreenTextColorResId == INVALID ? R.color.black : welcomeScreenTextColorResId;
        this.welcomeScreenTitleSizeInSp = welcomeScreenTitleSizeInSp;
        this.welcomeScreenSubtitleSizeInSp = welcomeScreenSubtitleSizeInSp;
        this.chatBodyIconsTint = chatBodyIconsTint == INVALID ? R.color.blue_0F87FF : chatBodyIconsTint;
        this.connectionMessageTextColor = connectionMessageTextColor == INVALID ? R.color.blue_grey_607d8b : connectionMessageTextColor;
        this.filesAndMediaScreenBackgroundColor = filesAndMediaScreenBackgroundColor;
        this.pushBackgroundColorResId = pushBackgroundColorResId;
        this.nougatPushAccentColorResId = nougatPushAccentColorResId;
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

        this.requestToResolveThreadTextResId = requestToResolveThreadTextResId == INVALID ? R.string.request_to_resolve_thread : requestToResolveThreadTextResId;
        this.approveRequestToResolveThreadTextResId = approveRequestToResolveThreadTextResId == INVALID ? R.string.request_to_resolve_thread_close : approveRequestToResolveThreadTextResId;
        this.denyRequestToResolveThreadTextResId = denyRequestToResolveThreadTextResId == INVALID ? R.string.request_to_resolve_thread_open : denyRequestToResolveThreadTextResId;

        this.binarySurveyLikeUnselectedIconResId = binarySurveyLikeUnselectedIconResId == INVALID ? R.drawable.ic_like_empty_36dp : binarySurveyLikeUnselectedIconResId;
        this.binarySurveyLikeSelectedIconResId = binarySurveyLikeSelectedIconResId == INVALID ? R.drawable.ic_like_full_36dp : binarySurveyLikeSelectedIconResId;
        this.binarySurveyDislikeUnselectedIconResId = binarySurveyDislikeUnselectedIconResId == INVALID ? R.drawable.ic_dislike_empty_36dp : binarySurveyDislikeUnselectedIconResId;
        this.binarySurveyDislikeSelectedIconResId = binarySurveyDislikeSelectedIconResId == INVALID ? R.drawable.ic_dislike_full_36dp : binarySurveyDislikeSelectedIconResId;
        this.optionsSurveyUnselectedIconResId = optionsSurveyUnselectedIconResId == INVALID ? R.drawable.ic_star_outline_grey600_24dp : optionsSurveyUnselectedIconResId;
        this.optionsSurveySelectedIconResId = optionsSurveySelectedIconResId == INVALID ? R.drawable.ic_star_grey600_24dp : optionsSurveySelectedIconResId;
        this.surveySelectedColorFilterResId = surveySelectedColorFilterResId;
        this.surveyUnselectedColorFilterResId = surveyUnselectedColorFilterResId;
        this.surveyTextColorResId = surveyTextColorResId == INVALID ? R.color.black : surveyTextColorResId;

        this.scrollDownButtonResId = scrollDownButtonResId;
        this.unreadMsgStickerColorResId = unreadMsgStickerColorResId;
        this.unreadMsgCountTextColorResId = unreadMsgCountTextColorResId;

        this.defaultFontBold = defaultFontBold;
        this.defaultFontLight = defaultFontLight;
        this.defaultFontRegular = defaultFontRegular;
        this.toolbarTitleFont = toolbarTitleFont;
        this.toolbarSubtitleFont = toolbarSubtitleFont;
        this.placeholderTitleFont = placeholderTitleFont;
        this.placeholderSubtitleFont = placeholderSubtitleFont;
        this.inputQuotedMessageFont = inputQuotedMessageFont;
        this.inputQuotedMessageAuthorFont = inputQuotedMessageAuthorFont;
        this.bubbleMessageFont = bubbleMessageFont;
        this.bubbleTimeFont = bubbleTimeFont;
        this.quoteAuthorFont = quoteAuthorFont;
        this.quoteMessageFont = quoteMessageFont;
        this.quoteTimeFont = quoteTimeFont;
        this.messageHeaderFont = messageHeaderFont;
        this.specialistConnectTitleFont = specialistConnectTitleFont;
        this.specialistConnectSubtitleFont = specialistConnectSubtitleFont;
        this.typingFont = typingFont;
        this.scheduleAlerFont = scheduleAlerFont;
    }

    public static class ChatStyleBuilder {
        private static final String TAG = "BundleBuilder ";
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
            chatStyle.chatToolbarColorResId = chatToolbarColorResId;
            chatStyle.chatToolbarTextColorResId = chatToolbarTextColorResId;
            chatStyle.chatStatusBarColorResId = chatStatusBarColorResId;
            chatStyle.menuItemTextColorResId = menuItemTextColorResId;
            chatStyle.chatToolbarHintTextColor = chatToolbarHintTextColor;
            chatStyle.showBackButton = showBackButton;
            return this;
        }

        public ChatStyleBuilder setChatBodyStyle(
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
                @DimenRes int operatorAvatarSize,
                @DimenRes int operatorSystemAvatarSize,
                @DrawableRes int imagePlaceholder,
                @StyleRes int fileBrowserDialogStyleResId,
                boolean showConsultSearching,
                boolean alwaysScrollToEnd,
                @ColorRes int inputTextColor,
                String inputTextFont,
                @DrawableRes int scrollDownButtonResId,
                @ColorRes int unreadMsgStickerColorResId,
                @ColorRes int unreadMsgCountTextColorResId) {

            chatStyle.chatBackgroundColor = chatBackgroundColor;
            chatStyle.chatHighlightingColor = chatHighlightingColor;
            chatStyle.chatMessageInputColor = chatMessageInputColor;
            chatStyle.chatMessageInputHintTextColor = chatMessageInputHintTextColor;
            chatStyle.incomingMessageBubbleColor = incomingMessageBubbleColor;
            chatStyle.outgoingMessageBubbleColor = outgoingMessageBubbleColor;
            chatStyle.incomingMessageTextColor = incomingMessageTextColor;
            chatStyle.outgoingMessageTextColor = outgoingMessageTextColor;
            chatStyle.defaultIncomingMessageAvatar = defaultIncomingMessageAvatar;
            chatStyle.operatorAvatarSize = operatorAvatarSize;
            chatStyle.operatorSystemAvatarSize = operatorSystemAvatarSize;
            chatStyle.imagePlaceholder = imagePlaceholder;
            chatStyle.chatBodyIconsTint = chatBodyIconsTint;
            chatStyle.connectionMessageTextColor = connectionMessageTextColor;
            chatStyle.filesAndMediaScreenBackgroundColor = filesAndMediaScreenBackgroundColor;
            chatStyle.fileBrowserDialogStyleResId = fileBrowserDialogStyleResId;
            chatStyle.showConsultSearching = showConsultSearching;
            chatStyle.alwaysScrollToEnd = alwaysScrollToEnd;
            chatStyle.inputTextColor = inputTextColor;
            chatStyle.inputTextFont = inputTextFont;
            chatStyle.scrollDownButtonResId = scrollDownButtonResId;
            chatStyle.unreadMsgStickerColorResId = unreadMsgStickerColorResId;
            chatStyle.unreadMsgCountTextColorResId = unreadMsgCountTextColorResId;
            return this;
        }


        public ChatStyleBuilder setPushNotificationStyle(@DrawableRes int defIconResid,
                                                         @StringRes int defTitleResId,
                                                         @ColorRes int pushBackgroundColorResId,
                                                         @ColorRes int nougatPushAccentColorResId) {
            chatStyle.defPushIconResId = defIconResid;
            chatStyle.defTitleResId = defTitleResId;
            chatStyle.pushBackgroundColorResId = pushBackgroundColorResId;
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
            chatStyle.binarySurveyLikeUnselectedIconResId = binarySurveyLikeUnselectedIconResId;
            chatStyle.binarySurveyLikeSelectedIconResId = binarySurveyLikeSelectedIconResId;
            chatStyle.binarySurveyDislikeUnselectedIconResId = binarySurveyDislikeUnselectedIconResId;
            chatStyle.binarySurveyDislikeSelectedIconResId = binarySurveyDislikeSelectedIconResId;
            chatStyle.optionsSurveyUnselectedIconResId = optionsSurveyUnselectedIconResId;
            chatStyle.optionsSurveySelectedIconResId = optionsSurveySelectedIconResId;
            chatStyle.surveySelectedColorFilterResId = surveySelectedColorFilterResId;
            chatStyle.surveyUnselectedColorFilterResId = surveyUnselectedColorFilterResId;
            chatStyle.surveyTextColorResId = surveyTextColorResId;
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
                , int welcomeScreenTitleSizeInSp
                , int welcomeScreenSubtitleSizeInSp) {
            chatStyle.welcomeScreenLogoResId = welcomeScreenLogoResId;
            chatStyle.welcomeScreenTextColorResId = welcomeScreenTextColorResId;
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
                throw new IllegalStateException(ctx.getString(R.string.invalid_client_id));
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
