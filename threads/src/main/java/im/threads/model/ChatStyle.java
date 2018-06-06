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
    public int incomingMessageLinkColor = R.color.threads_incoming_message_link;
    @ColorRes
    public int outgoingMessageLinkColor = R.color.threads_outgoing_message_link;

    @ColorRes
    public int chatBodyIconsTint = INVALID;
    @ColorRes
    public int chatSystemMessageTextColor = INVALID;
    @ColorRes
    public int filesAndMediaScreenBackgroundColor = INVALID;
    @ColorRes
    public int iconsAndSeparatorsColor = INVALID;
    @DrawableRes
    public int defaultOperatorAvatar = INVALID;
    @DimenRes
    public int operatorAvatarSize = INVALID;
    @DimenRes
    public int operatorSystemAvatarSize = INVALID;
    @DrawableRes
    public int imagePlaceholder = INVALID;
    @StyleRes
    public int fileBrowserDialogStyleResId = INVALID;
    public boolean showConsultSearching;
    public boolean scrollChatToEndIfUserTyping;
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
    public int defPushIconResId = INVALID;
    @StringRes
    public int defTitleResId = INVALID;
    @ColorRes
    public int pushBackgroundColorResId = INVALID;
    @ColorRes
    public int nougatPushAccentColorResId = INVALID;

    // resolve thread request style
    @StringRes
    public int requestToResolveThreadTextResId = INVALID;
    @StringRes
    public int approveRequestToResolveThreadTextResId = INVALID;
    @StringRes
    public int denyRequestToResolveThreadTextResId = INVALID;

    // survey style
    @DrawableRes
    public int binarySurveyLikeUnselectedIconResId = INVALID;
    @DrawableRes
    public int binarySurveyLikeSelectedIconResId = INVALID;
    @DrawableRes
    public int binarySurveyDislikeUnselectedIconResId = INVALID;
    @DrawableRes
    public int binarySurveyDislikeSelectedIconResId = INVALID;
    @DrawableRes
    public int optionsSurveyUnselectedIconResId = INVALID;
    @DrawableRes
    public int optionsSurveySelectedIconResId = INVALID;
    @ColorRes
    public int surveySelectedColorFilterResId = INVALID;
    @ColorRes
    public int surveyUnselectedColorFilterResId = INVALID;
    @ColorRes
    public int surveyTextColorResId = INVALID;

    // schedule message style
    @ColorRes
    public int scheduleMessageTextColorResId = INVALID;
    @DrawableRes
    public int scheduleMessageIconResId = INVALID;

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
    public int welcomeScreenTitleSizeInSp = INVALID;
    public int welcomeScreenSubtitleSizeInSp = INVALID;

    // set history loading count
    public int historyLoadingCount = 50;

    // set can show specialist onfo
    public boolean canShowSpecialistInfo = true;

    // set if client id encrypted
    public boolean isClientIdEncrypted = false;

    public boolean isDebugLoggingEnabled = false;


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

        public static ChatStyleBuilder getBuilder(final Context ctx, final String clientId, final String userName) {
            return getBuilder(ctx, clientId, userName, "");
        }

        public static ChatStyleBuilder getBuilder(final Context ctx, final String clientId, final String userName, final String data) {
            final ChatStyleBuilder builder = new ChatStyleBuilder();
            builder.clientId = clientId;
            builder.userName = userName;
            builder.data = data;
            builder.chatStyle = getInstance();
            builder.ctx = ctx.getApplicationContext();
            return builder;
        }

        public ChatStyleBuilder showChatBackButton(final boolean showBackButton) {
            chatStyle.showBackButton = showBackButton;
            return this;
        }

        public ChatStyleBuilder setShowConsultSearching(final boolean show) {
            chatStyle.showConsultSearching = show;
            return this;
        }

        public ChatStyleBuilder setIsClientIdEncrypted(final boolean encrypted) {
            chatStyle.isClientIdEncrypted = encrypted;
            if (encrypted) {
                PrefUtils.setClientIdEncrypted(ctx);
            }
            return this;
        }

        public ChatStyleBuilder setDebugLoggingEnabled(boolean enabled) {
            chatStyle.isDebugLoggingEnabled = enabled;
            return this;
        }

        public ChatStyleBuilder setScrollChatToEndIfUserTyping(final boolean scroll) {
            chatStyle.scrollChatToEndIfUserTyping = scroll;
            return this;
        }


        public ChatStyleBuilder setHistoryLoadingCount(final int count) {
            if (count > 0) {
                chatStyle.historyLoadingCount = count;
            }
            return this;
        }

        public ChatStyleBuilder setCanShowSpecialistInfo(final boolean show) {
            chatStyle.canShowSpecialistInfo = show;
            return this;
        }

        // set fonts

        public ChatStyleBuilder setDefaultFontBold(final String path) {
            chatStyle.defaultFontBold = path;
            return this;
        }

        public ChatStyleBuilder setDefaultFontLight(final String path) {
            chatStyle.defaultFontLight = path;
            return this;
        }

        public ChatStyleBuilder setDefaultFontRegular(final String path) {
            chatStyle.defaultFontRegular = path;
            return this;
        }

        public ChatStyleBuilder setToolbarTitleFont(final String path) {
            chatStyle.toolbarTitleFont = path;
            return this;
        }

        public ChatStyleBuilder setToolbarSubtitleFont(final String path) {
            chatStyle.toolbarSubtitleFont = path;
            return this;
        }

        public ChatStyleBuilder setPlaceholderTitleFont(final String path) {
            chatStyle.placeholderTitleFont = path;
            return this;
        }

        public ChatStyleBuilder setPlaceholderSubtitleFont(final String path) {
            chatStyle.placeholderSubtitleFont = path;
            return this;
        }

        public ChatStyleBuilder setInputQuotedMessageAuthorFont(final String path) {
            chatStyle.inputQuotedMessageAuthorFont = path;
            return this;
        }

        public ChatStyleBuilder setInputQuotedMessageFont(final String path) {
            chatStyle.inputQuotedMessageFont = path;
            return this;
        }

        public ChatStyleBuilder setBubbleMessageFont(final String path) {
            chatStyle.bubbleMessageFont = path;
            return this;
        }

        public ChatStyleBuilder setInputTextFont(final String path) {
            chatStyle.inputTextFont = path;
            return this;
        }

        public ChatStyleBuilder setBubbleTimeFont(final String path) {
            chatStyle.bubbleTimeFont = path;
            return this;
        }

        public ChatStyleBuilder setQuoteAuthorFont(final String path) {
            chatStyle.quoteAuthorFont = path;
            return this;
        }

        public ChatStyleBuilder setQuoteMessageFont(final String path) {
            chatStyle.quoteMessageFont = path;
            return this;
        }

        public ChatStyleBuilder setQuoteTimeFont(final String path) {
            chatStyle.quoteTimeFont = path;
            return this;
        }

        public ChatStyleBuilder setMessageHeaderFont(final String path) {
            chatStyle.messageHeaderFont = path;
            return this;
        }

        public ChatStyleBuilder setSpecialistConnectTitleFont(final String path) {
            chatStyle.specialistConnectTitleFont = path;
            return this;
        }

        public ChatStyleBuilder setSpecialistConnectSubtitleFont(final String path) {
            chatStyle.specialistConnectSubtitleFont = path;
            return this;
        }

        public ChatStyleBuilder setTypingFont(final String path) {
            chatStyle.typingFont = path;
            return this;
        }

        public ChatStyleBuilder setScheduleAlertFont(final String path) {
            chatStyle.scheduleAlerFont = path;
            return this;
        }

        public ChatStyle build() {
            if (TextUtils.isEmpty(clientId) && PrefUtils.getClientID(ctx).equals("")) {
                throw new IllegalStateException(ctx.getString(R.string.threads_invalid_client_id));
            }

            PrefUtils.setIncomingStyle(ctx, chatStyle);

            PrefUtils.setNewClientId(ctx, clientId);
            PrefUtils.setUserName(ctx, userName);
            PrefUtils.setData(ctx, data);

            return chatStyle;
        }

        // deprecated setters

        /**
         * @deprecated
         * Отношение старый параметр - новый параметр
         * @param chatTitleTextResId - R.string.threads_contact_center
         * @param chatToolbarColorResId - R.color.threads_chat_toolbar
         * @param chatToolbarTextColorResId - R.color.threads_chat_toolbar_text
         * @param chatStatusBarColorResId - R.color.threads_chat_status_bar
         * @param menuItemTextColorResId - R.color.threads_chat_toolbar_menu_item
         * @param chatToolbarHintTextColor - R.color.threads_chat_toolbar_hint
         * @param showBackButton - showChatBackButton(boolean showBackButton)
         */
        @Deprecated
        public ChatStyleBuilder setChatTitleStyle(
                @StringRes final int chatTitleTextResId,
                @ColorRes final int chatToolbarColorResId,
                @ColorRes final int chatToolbarTextColorResId,
                @ColorRes final int chatStatusBarColorResId,
                @ColorRes final int menuItemTextColorResId,
                @ColorRes final int chatToolbarHintTextColor,
                final boolean showBackButton) {
            chatStyle.chatTitleTextResId = chatTitleTextResId;
            chatStyle.chatToolbarColorResId = chatToolbarColorResId;
            chatStyle.chatToolbarTextColorResId = chatToolbarTextColorResId;
            chatStyle.chatStatusBarColorResId = chatStatusBarColorResId;
            chatStyle.menuItemTextColorResId = menuItemTextColorResId;
            chatStyle.chatToolbarHintTextColor = chatToolbarHintTextColor;
            chatStyle.showBackButton = showBackButton;
            return this;
        }

        /**
         * @deprecated
         * Отношение старый параметр - новый параметр
         * @param chatBackgroundColor - R.color.threads_chat_background
         * @param chatHighlightingColor - R.color.threads_chat_highlighting
         * @param incomingMessageBubbleColor - R.color.threads_chat_incoming_message_bubble
         * @param outgoingMessageBubbleColor- R.color.threads_chat_outgoing_message_bubble
         * @param incomingMessageBubbleBackground - R.drawable.thread_incoming_bubble
         * @param outgoingMessageBubbleBackground - R.drawable.thread_outgoing_bubble
         * @param incomingMessageTextColor - R.color.threads_incoming_message_text
         * @param outgoingMessageTextColor - R.color.threads_outgoing_message_text
         * @param chatBodyIconsTint - R.color.threads_chat_icons_tint
         * @param connectionMessageTextColor - R.color.threads_chat_connection_message
         * @param filesAndMediaScreenBackgroundColor - R.color.threads_files_medias_screen_background
         * @param iconsAndSeparatorsColor - R.color.threads_icon_and_separators_color
         * @param defaultIncomingMessageAvatar - R.drawable.threads_operator_avatar_placeholder
         * @param operatorAvatarSize - R.dimen.threads_operator_photo_size
         * @param operatorSystemAvatarSize - R.dimen.threads_system_operator_photo_size
         * @param imagePlaceholder - R.drawable.threads_image_placeholder
         * @param fileBrowserDialogStyleResId - deleted
         * @param showConsultSearching - setShowConsultSearching(boolean show)
         * @param alwaysScrollToEnd - setScrollChatToEndIfUserTyping(boolean scroll)
         * @param scrollDownButtonResId - R.drawable.threads_scroll_down_btn_back
         * @param unreadMsgStickerColorResId - R.color.threads_chat_unread_msg_sticker_background
         * @param unreadMsgCountTextColorResId - R.color.threads_chat_unread_msg_count_text
         * @return
         */
        @Deprecated
        public ChatStyleBuilder setChatBodyStyle(
                @ColorRes final int chatBackgroundColor,
                @ColorRes final int chatHighlightingColor,
                @ColorRes final int incomingMessageBubbleColor,
                @ColorRes final int outgoingMessageBubbleColor,
                @DrawableRes final int incomingMessageBubbleBackground,
                @DrawableRes final int outgoingMessageBubbleBackground,
                @ColorRes final int incomingMessageTextColor,
                @ColorRes final int outgoingMessageTextColor,
                @ColorRes final int chatBodyIconsTint,
                @ColorRes final int connectionMessageTextColor,
                @ColorRes final int filesAndMediaScreenBackgroundColor,
                @ColorRes final int iconsAndSeparatorsColor,
                @DrawableRes final int defaultIncomingMessageAvatar,
                @DimenRes final int operatorAvatarSize,
                @DimenRes final int operatorSystemAvatarSize,
                @DrawableRes final int imagePlaceholder,
                @StyleRes final int fileBrowserDialogStyleResId,
                final boolean showConsultSearching,
                final boolean alwaysScrollToEnd,
                @DrawableRes final int scrollDownButtonResId,
                @ColorRes final int unreadMsgStickerColorResId,
                @ColorRes final int unreadMsgCountTextColorResId) {
            chatStyle.chatBackgroundColor = chatBackgroundColor;
            chatStyle.chatHighlightingColor = chatHighlightingColor;
            chatStyle.incomingMessageBubbleColor = incomingMessageBubbleColor;
            chatStyle.outgoingMessageBubbleColor = outgoingMessageBubbleColor;
            chatStyle.incomingMessageBubbleBackground = incomingMessageBubbleBackground;
            chatStyle.outgoingMessageBubbleBackground = outgoingMessageBubbleBackground;
            chatStyle.incomingMessageTextColor = incomingMessageTextColor;
            chatStyle.outgoingMessageTextColor = outgoingMessageTextColor;
            chatStyle.defaultOperatorAvatar = defaultIncomingMessageAvatar;
            chatStyle.operatorAvatarSize = operatorAvatarSize;
            chatStyle.operatorSystemAvatarSize = operatorSystemAvatarSize;
            chatStyle.imagePlaceholder = imagePlaceholder;
            chatStyle.chatBodyIconsTint = chatBodyIconsTint;
            chatStyle.chatSystemMessageTextColor = connectionMessageTextColor;
            chatStyle.filesAndMediaScreenBackgroundColor = filesAndMediaScreenBackgroundColor;
            chatStyle.iconsAndSeparatorsColor = iconsAndSeparatorsColor;
            chatStyle.fileBrowserDialogStyleResId = fileBrowserDialogStyleResId;
            chatStyle.showConsultSearching = showConsultSearching;
            chatStyle.scrollChatToEndIfUserTyping = alwaysScrollToEnd;
            chatStyle.scrollDownButtonResId = scrollDownButtonResId;
            chatStyle.unreadMsgStickerColorResId = unreadMsgStickerColorResId;
            chatStyle.unreadMsgCountTextColorResId = unreadMsgCountTextColorResId;
            return this;
        }


        /**
         * @deprecated
         * Отношение старый параметр - новый параметр
         * @param chatMessageInputHintTextColor - R.color.threads_input_hint
         * @param chatMessageInputColor - R.color.threads_input_background
         * @param inputTextColor - R.color.threads_input_text
         * @param inputTextFont - setInputTextFont(String path)
         * @param attachmentsIconResId - R.drawable.threads_ic_attachment_button
         * @param sendMessageIconResId - R.drawable.threads_ic_send_button
         * @param inputHint - R.string.threads_input_hint
         * @param inputHeight - R.dimen.threads_input_height
         * @param inputBackground - R.drawable.threads_chat_input_background
         * @return
         */
        @Deprecated
        public ChatStyleBuilder setChatInputStyle(
                @ColorRes final int chatMessageInputHintTextColor,
                @ColorRes final int chatMessageInputColor,
                @ColorRes final int inputTextColor,
                final String inputTextFont,
                @DrawableRes final int attachmentsIconResId,
                @DrawableRes final int sendMessageIconResId,
                @StringRes final int inputHint,
                @DimenRes final int inputHeight,
                @DrawableRes final int inputBackground) {

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

        /**
         * @deprecated
         * Отношение старый параметр - новый параметр
         * @param defIconResId - R.drawable.default_push_icon
         * @param defTitleResId - R.string.threads_push_title
         * @param pushBackgroundColorResId - R.color.threads_push_background
         * @param nougatPushAccentColorResId - R.color.nougat_push_accent
         * @return
         */
        @Deprecated
        public ChatStyleBuilder setPushNotificationStyle(@DrawableRes final int defIconResId,
                                                         @StringRes final int defTitleResId,
                                                         @ColorRes final int pushBackgroundColorResId,
                                                         @ColorRes final int nougatPushAccentColorResId) {

            chatStyle.defPushIconResId = defIconResId;
            chatStyle.defTitleResId = defTitleResId;
            chatStyle.pushBackgroundColorResId = pushBackgroundColorResId;
            chatStyle.nougatPushAccentColorResId = nougatPushAccentColorResId;
            return this;
        }

        /**
         * @deprecated
         * Отношение старый параметр - новый параметр
         * @param requestToResolveThreadTextResId - R.string.threads_request_to_resolve_thread
         * @param approveRequestToResolveThreadTextResId - R.string.threads_request_to_resolve_thread_close
         * @param denyRequestToResolveThreadTextResId - R.string.threads_request_to_resolve_thread_open
         */
        @Deprecated
        public ChatStyleBuilder setRequestResolveThreadStyle(@StringRes final int requestToResolveThreadTextResId,
                                                             @StringRes final int approveRequestToResolveThreadTextResId,
                                                             @StringRes final int denyRequestToResolveThreadTextResId) {
            chatStyle.requestToResolveThreadTextResId = requestToResolveThreadTextResId;
            chatStyle.approveRequestToResolveThreadTextResId = approveRequestToResolveThreadTextResId;
            chatStyle.denyRequestToResolveThreadTextResId = denyRequestToResolveThreadTextResId;
            return this;
        }

        /**
         * @deprecated
         * Отношение старый параметр - новый параметр
         * @param binarySurveyLikeUnselectedIconResId - R.drawable.threads_binary_survey_like_unselected
         * @param binarySurveyLikeSelectedIconResId - R.drawable.threads_binary_survey_like_selected
         * @param binarySurveyDislikeUnselectedIconResId - R.drawable.threads_binary_survey_dislike_unselected
         * @param binarySurveyDislikeSelectedIconResId - R.drawable.threads_binary_survey_dislike_selected
         * @param optionsSurveyUnselectedIconResId - R.drawable.threads_options_survey_unselected
         * @param optionsSurveySelectedIconResId - R.drawable.threads_options_survey_selected
         * @param surveySelectedColorFilterResId - R.color.threads_survey_selected_icon_tint
         * @param surveyUnselectedColorFilterResId - R.color.threads_survey_unselected_icon_tint
         * @param surveyTextColorResId - R.color.threads_chat_system_message
         */
        @Deprecated
        public ChatStyleBuilder setSurveyStyle(@DrawableRes final int binarySurveyLikeUnselectedIconResId,
                                               @DrawableRes final int binarySurveyLikeSelectedIconResId,
                                               @DrawableRes final int binarySurveyDislikeUnselectedIconResId,
                                               @DrawableRes final int binarySurveyDislikeSelectedIconResId,
                                               @DrawableRes final int optionsSurveyUnselectedIconResId,
                                               @DrawableRes final int optionsSurveySelectedIconResId,
                                               @ColorRes final int surveySelectedColorFilterResId,
                                               @ColorRes final int surveyUnselectedColorFilterResId,
                                               @ColorRes final int surveyTextColorResId) {
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


        /**
         * @deprecated
         * Отношение старый параметр - новый параметр
         * @param scheduleMessageIconResId - R.drawable.threads_schedule_icon
         * @param scheduleMessageTextColor - R.color.threads_schedule_text
         * @return
         */
        @Deprecated
        public ChatStyleBuilder setScheduleMessageStyle(
                @DrawableRes final int scheduleMessageIconResId,
                @ColorRes final int scheduleMessageTextColor) {
            chatStyle.scheduleMessageIconResId = scheduleMessageIconResId;
            chatStyle.scheduleMessageTextColorResId = scheduleMessageTextColor;

            return this;
        }

        /**
         * @deprecated
         * Отношение старый параметр - новый параметр
         * @param welcomeScreenLogoResId  - R.drawable.threads_welcome_logo
         * @param welcomeScreenTitleTextResId - R.string.threads_welcome_screen_title_text
         * @param welcomeScreenSubtitleTextResId - R.string.threads_welcome_screen_subtitle_text
         * @param welcomeScreenTextColorResId - R.color.threads_welcome_screen_title
         * @param welcomeScreenSubtitleTextColorResId - R.color.threads_welcome_screen_subtitle
         * @param welcomeScreenTitleSizeInSp - R.dimen.welcome_screen_title
         * @param welcomeScreenSubtitleSizeInSp - R.dimen.welcome_screen_subtitle
         * @return
         */
        @Deprecated
        public ChatStyleBuilder setWelcomeScreenStyle(
                @DrawableRes final int welcomeScreenLogoResId
                , @StringRes final int welcomeScreenTitleTextResId
                , @StringRes final int welcomeScreenSubtitleTextResId
                , @ColorRes final int welcomeScreenTextColorResId
                , @ColorRes final int welcomeScreenSubtitleTextColorResId
                , final int welcomeScreenTitleSizeInSp
                , final int welcomeScreenSubtitleSizeInSp) {

            chatStyle.welcomeScreenLogoResId = welcomeScreenLogoResId;
            chatStyle.welcomeScreenTitleTextColorResId = welcomeScreenTextColorResId;
            chatStyle.welcomeScreenSubtitleTextColorResId = welcomeScreenSubtitleTextColorResId;
            chatStyle.welcomeScreenTitleTextResId = welcomeScreenTitleTextResId;
            chatStyle.welcomeScreenSubtitleTextResId = welcomeScreenSubtitleTextResId;
            chatStyle.welcomeScreenTitleSizeInSp = welcomeScreenTitleSizeInSp;
            chatStyle.welcomeScreenSubtitleSizeInSp = welcomeScreenSubtitleSizeInSp;
            return this;
        }
    }
}
