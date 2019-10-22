package im.threads;

import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;

import java.io.Serializable;

/**
 * Стиль чата.
 * Хранит в себе информацию о расцветках, иконках
 * и других кастомизациях чата.
 */
public final class ChatStyle implements Serializable {

    //common styles
    @ColorRes
    public int chatDisabledTextColor = R.color.threads_disabled_text_color;

    // chat title style
    @StringRes
    public int chatTitleTextResId = R.string.threads_contact_center;
    @StringRes
    public int chatSubtitleTextResId = R.string.threads_operator_subtitle;
    @ColorRes
    public int chatToolbarColorResId = R.color.threads_chat_toolbar;
    @ColorRes
    public int chatStatusBarColorResId = R.color.threads_chat_status_bar;
    @ColorRes
    public int menuItemTextColorResId = R.color.threads_chat_toolbar_menu_item;
    @ColorRes
    public int chatToolbarTextColorResId = R.color.threads_chat_toolbar_text;
    @ColorRes
    public int chatToolbarHintTextColor = R.color.threads_chat_toolbar_hint;
    public boolean showBackButton = false;
    public boolean chatSubtitleShowOrgUnit = false;

    // chat body style
    @ColorRes
    public int chatBackgroundColor = R.color.threads_chat_background;
    @ColorRes
    public int chatHighlightingColor = R.color.threads_chat_highlighting;
    @ColorRes
    public int incomingMessageBubbleColor = R.color.threads_chat_incoming_message_bubble;
    @ColorRes
    public int outgoingMessageBubbleColor = R.color.threads_chat_outgoing_message_bubble;
    @DrawableRes
    public int incomingMessageBubbleBackground = R.drawable.thread_incoming_bubble;
    @DrawableRes
    public int outgoingMessageBubbleBackground = R.drawable.thread_outgoing_bubble;
    @ColorRes
    public int incomingMessageTextColor = R.color.threads_incoming_message_text;
    @ColorRes
    public int outgoingMessageTextColor = R.color.threads_outgoing_message_text;

    @ColorRes
    public int incomingMessageTimeColor = R.color.threads_operator_message_timestamp;
    @ColorRes
    public int outgoingMessageTimeColor = R.color.threads_user_message_timestamp;

    @DrawableRes
    public int outgoingImageBubbleMask = R.drawable.thread_outgoing_image_mask;
    @ColorRes
    public int outgoingImageTimeColor = R.color.threads_outgoing_message_time;
    @ColorRes
    public int outgoingImageTimeBackgroundColor = R.color.threads_outgoing_time_underlay;
    @DrawableRes
    public int incomingImageBubbleMask = R.drawable.thread_incoming_image_mask;
    @ColorRes
    public int incomingImageTimeColor = R.color.threads_incoming_message_time;
    @ColorRes
    public int incomingImageTimeBackgroundColor = R.color.threads_incoming_time_underlay;

    @ColorRes
    public int incomingMessageLinkColor = R.color.threads_incoming_message_link;
    @ColorRes
    public int outgoingMessageLinkColor = R.color.threads_outgoing_message_link;

    @ColorRes
    public int chatBodyIconsTint = R.color.threads_chat_icons_tint;
    @ColorRes
    public int chatSystemMessageTextColor = R.color.threads_chat_connection_message;
    @ColorRes
    public int filesAndMediaScreenBackgroundColor = R.color.threads_files_medias_screen_background;
    @ColorRes
    public int filesAndMediaTextColor = R.color.threads_files_list;

    @ColorRes
    public int iconsAndSeparatorsColor = R.color.threads_icon_and_separators_color;
    @DrawableRes
    public int defaultOperatorAvatar = R.drawable.threads_operator_avatar_placeholder;
    @DimenRes
    public int operatorAvatarSize = R.dimen.threads_operator_photo_size;
    @DimenRes
    public int operatorSystemAvatarSize = R.dimen.threads_system_operator_photo_size;
    @DrawableRes
    public int imagePlaceholder = R.drawable.threads_image_placeholder;
    @StyleRes
    public int fileBrowserDialogStyleResId = R.style.FileDialogStyleTransparent;
    public boolean showConsultSearching = false;
    public boolean scrollChatToEndIfUserTyping = false;
    @DrawableRes
    public int scrollDownButtonResId = R.drawable.threads_scroll_down_btn_back;
    @ColorRes
    public int unreadMsgStickerColorResId = R.color.threads_chat_unread_msg_sticker_background;
    @ColorRes
    public int unreadMsgCountTextColorResId = R.color.threads_chat_unread_msg_count_text;

    //TODO #THREADS-3523
    //chat message statuses
//    R.color.threads_outgoing_message_sent_icon - цвет иконки статуса "Отправлено" (галочка) на сообщении клиента
//    R.color.threads_outgoing_message_received_icon - цвет иконки статуса "Прочитано" (двойная галочка) на сообщении клиента
//    R.color.threads_outgoing_message_not_send_icon - цвет иконки статуса "Не отправлено" на сообщении клиента
//    R.color.threads_outgoing_message_image_sent_icon - цвет иконки статуса "Отправлено" (галочка) на сообщении с изображением
//    R.color.threads_outgoing_message_image_received_icon - цвет иконки статуса "Прочитано" (двойная галочка) на сообщении с изображением
//    R.color.threads_outgoing_message_image_not_send_icon - цвет иконки статуса "Не отправлено" на сообщении с изображением
//    R.drawable.threads_message_sent - иконка статуса "Отправлено" (галочка) на сообщении клиента
//    R.drawable.threads_message_received - иконка статуса "Прочитано" (двойная галочка) на сообщении клиента
//    R.drawable.threads_message_waiting - иконка статуса "Не отправлено" на сообщении клиента
//    R.drawable.threads_message_image_sent - иконка статуса "Отправлено" (галочка) на сообщении с изображением
//    R.drawable.threads_image_message_received - иконка статуса "Прочитано" (двойная галочка) на сообщении с изображением
//    R.drawable.threads_message_image_waiting - иконка статуса "Не отправлено" на сообщении с изображением
//    R.drawable.timestamp_incoming_underlayer - бэкграунд подложки под временем на входящем сообщении с изображением
//    R.drawable.timestamp_outgoing_underlayer - бэкграунд подложки под временем на исходящем сообщении с изображением

    //images history & gallery screens
    @ColorRes
    public int imagesScreenToolbarColor = R.color.threads_attachments_toolbar;
    @ColorRes
    public int imagesScreenBackgroundColor = R.color.threads_attachments_background;
    @ColorRes
    public int imagesScreenAuthorTextColor = R.color.threads_attachments_author_text_color;
    @ColorRes
    public int imagesScreenDateTextColor = R.color.threads_attachments_date_text_color;
    @DimenRes
    public int imagesScreenAuthorTextSize = R.dimen.threads_attachments_author_text_size;
    @DimenRes
    public int imagesScreenDateTextSize = R.dimen.threads_attachments_date_text_size;

    // chat input style
    @ColorRes
    public int chatMessageInputColor = R.color.threads_input_background;
    @ColorRes
    public int chatMessageInputHintTextColor = R.color.threads_input_hint;
    @ColorRes
    public int inputTextColor = R.color.threads_input_text;
    @Nullable
    public String inputTextFont;
    @DrawableRes
    public int attachmentsIconResId = R.drawable.threads_ic_attachment_button;
    @DrawableRes
    public int sendMessageIconResId = R.drawable.threads_ic_send_button;
    @StringRes
    public int inputHint = R.string.threads_input_hint;
    @DimenRes
    public int inputHeight = R.dimen.threads_input_height;
    @DrawableRes
    public int inputBackground = R.drawable.threads_chat_input_background;

    // push notification style
    @DrawableRes
    public int defPushIconResId = R.drawable.default_push_icon;
    @StringRes
    public int defTitleResId = R.string.threads_push_title;
    @ColorRes
    public int pushBackgroundColorResId = R.color.threads_push_background;
    @ColorRes
    public int nougatPushAccentColorResId = R.color.threads_nougat_push_accent;
    @ColorRes
    public int quickReplyMessageBackgroundColor = R.color.threads_quick_reply_message_background;
    @ColorRes
    public int quickReplyMessageTextColor = R.color.threads_quick_reply_message_text_color;

    // resolve thread request style
    @StringRes
    public int requestToResolveThreadTextResId = R.string.threads_request_to_resolve_thread;
    @StringRes
    public int approveRequestToResolveThreadTextResId = R.string.threads_request_to_resolve_thread_close;
    @StringRes
    public int denyRequestToResolveThreadTextResId = R.string.threads_request_to_resolve_thread_open;

    // survey style
    @DrawableRes
    public int binarySurveyLikeUnselectedIconResId = R.drawable.threads_binary_survey_like_unselected;
    @DrawableRes
    public int binarySurveyLikeSelectedIconResId = R.drawable.threads_binary_survey_like_selected;
    @DrawableRes
    public int binarySurveyDislikeUnselectedIconResId = R.drawable.threads_binary_survey_dislike_unselected;
    @DrawableRes
    public int binarySurveyDislikeSelectedIconResId = R.drawable.threads_binary_survey_dislike_selected;
    @DrawableRes
    public int optionsSurveyUnselectedIconResId = R.drawable.threads_options_survey_unselected;
    @DrawableRes
    public int optionsSurveySelectedIconResId = R.drawable.threads_options_survey_selected;
    @ColorRes
    public int surveySelectedColorFilterResId = R.color.threads_survey_selected_icon_tint;
    @ColorRes
    public int surveyUnselectedColorFilterResId = R.color.threads_survey_unselected_icon_tint;
    @ColorRes
    public int surveyTextColorResId = R.color.threads_chat_system_message;
    @ColorRes
    public int surveyChoicesTextColorResId = R.color.threads_survey_choices_text;

    // schedule message style
    @ColorRes
    public int scheduleMessageTextColorResId = R.color.threads_schedule_text;
    @DrawableRes
    public int scheduleMessageIconResId = R.drawable.threads_schedule_icon;

    // welcome screen style
    @DrawableRes
    public int welcomeScreenLogoResId = R.drawable.threads_welcome_logo;
    @StringRes
    public int welcomeScreenTitleTextResId = R.string.threads_welcome_screen_title_text;
    @StringRes
    public int welcomeScreenSubtitleTextResId = R.string.threads_welcome_screen_subtitle_text;
    @ColorRes
    public int welcomeScreenTitleTextColorResId = R.color.threads_welcome_screen_title;
    @ColorRes
    public int welcomeScreenSubtitleTextColorResId = R.color.threads_welcome_screen_subtitle;
    public int welcomeScreenTitleSizeInSp = R.dimen.threads_welcome_screen_title;
    public int welcomeScreenSubtitleSizeInSp = R.dimen.threads_welcome_screen_subtitle;

    // set can show specialist onfo
    public boolean canShowSpecialistInfo = true;

    public boolean useExternalCameraApp = false;

    // specify fonts
    @Nullable
    public String defaultFontBold;
    @Nullable
    public String defaultFontLight;
    @Nullable
    public String defaultFontRegular;
    @Nullable
    public String toolbarTitleFont;
    @Nullable
    public String toolbarSubtitleFont;
    @Nullable
    public String placeholderTitleFont;
    @Nullable
    public String placeholderSubtitleFont;
    @Nullable
    public String inputQuotedMessageAuthorFont;
    @Nullable
    public String inputQuotedMessageFont;
    @Nullable
    public String bubbleMessageFont;
    @Nullable
    public String bubbleTimeFont;
    @Nullable
    public String quoteAuthorFont;
    @Nullable
    public String quoteMessageFont;
    @Nullable
    public String quoteTimeFont;
    @Nullable
    public String messageHeaderFont;
    @Nullable
    public String specialistConnectTitleFont;
    @Nullable
    public String specialistConnectSubtitleFont;
    @Nullable
    public String typingFont;
    @Nullable
    public String scheduleAlertFont;

    public ChatStyle() {
    }

    public ChatStyle showChatBackButton(final boolean showBackButton) {
        this.showBackButton = showBackButton;
        return this;
    }

    public ChatStyle setChatSubtitleShowConsultOrgUnit(boolean showConsultOrgUnit) {
        this.chatSubtitleShowOrgUnit = showConsultOrgUnit;
        return this;
    }

    public ChatStyle setShowConsultSearching(final boolean show) {
        this.showConsultSearching = show;
        return this;
    }

    public ChatStyle setScrollChatToEndIfUserTyping(final boolean scroll) {
        this.scrollChatToEndIfUserTyping = scroll;
        return this;
    }

    public ChatStyle setCanShowSpecialistInfo(final boolean show) {
        this.canShowSpecialistInfo = show;
        return this;
    }

    public ChatStyle setUseExternalCameraApp(final boolean useExternal) {
        this.useExternalCameraApp = useExternal;
        return this;
    }

    // set fonts
    public ChatStyle setDefaultFontBold(final String path) {
        this.defaultFontBold = path;
        return this;
    }

    public ChatStyle setDefaultFontLight(final String path) {
        this.defaultFontLight = path;
        return this;
    }

    public ChatStyle setDefaultFontRegular(final String path) {
        this.defaultFontRegular = path;
        return this;
    }

    public ChatStyle setToolbarTitleFont(final String path) {
        this.toolbarTitleFont = path;
        return this;
    }

    public ChatStyle setToolbarSubtitleFont(final String path) {
        this.toolbarSubtitleFont = path;
        return this;
    }

    public ChatStyle setPlaceholderTitleFont(final String path) {
        this.placeholderTitleFont = path;
        return this;
    }

    public ChatStyle setPlaceholderSubtitleFont(final String path) {
        this.placeholderSubtitleFont = path;
        return this;
    }

    public ChatStyle setInputQuotedMessageAuthorFont(final String path) {
        this.inputQuotedMessageAuthorFont = path;
        return this;
    }

    public ChatStyle setInputQuotedMessageFont(final String path) {
        this.inputQuotedMessageFont = path;
        return this;
    }

    public ChatStyle setBubbleMessageFont(final String path) {
        this.bubbleMessageFont = path;
        return this;
    }

    public ChatStyle setInputTextFont(final String path) {
        this.inputTextFont = path;
        return this;
    }

    public ChatStyle setBubbleTimeFont(final String path) {
        this.bubbleTimeFont = path;
        return this;
    }

    public ChatStyle setQuoteAuthorFont(final String path) {
        this.quoteAuthorFont = path;
        return this;
    }

    public ChatStyle setQuoteMessageFont(final String path) {
        this.quoteMessageFont = path;
        return this;
    }

    public ChatStyle setQuoteTimeFont(final String path) {
        this.quoteTimeFont = path;
        return this;
    }

    public ChatStyle setMessageHeaderFont(final String path) {
        this.messageHeaderFont = path;
        return this;
    }

    public ChatStyle setSpecialistConnectTitleFont(final String path) {
        this.specialistConnectTitleFont = path;
        return this;
    }

    public ChatStyle setSpecialistConnectSubtitleFont(final String path) {
        this.specialistConnectSubtitleFont = path;
        return this;
    }

    public ChatStyle setTypingFont(final String path) {
        this.typingFont = path;
        return this;
    }

    public ChatStyle setScheduleAlertFont(final String path) {
        this.scheduleAlertFont = path;
        return this;
    }

    // deprecated setters

    /**
     * Default values:
     *
     * @param chatTitleTextResId        - R.string.threads_contact_center
     * @param chatSubtitleTextResId     - R.string.threads_operator_subtitle
     * @param chatToolbarColorResId     - R.color.threads_chat_toolbar
     * @param chatToolbarTextColorResId - R.color.threads_chat_toolbar_text
     * @param chatStatusBarColorResId   - R.color.threads_chat_status_bar
     * @param menuItemTextColorResId    - R.color.threads_chat_toolbar_menu_item
     * @param chatToolbarHintTextColor  - R.color.threads_chat_toolbar_hint
     * @param showBackButton            - showChatBackButton(boolean showBackButton)
     */
    public ChatStyle setChatTitleStyle(
            @StringRes final int chatTitleTextResId,
            @StringRes final int chatSubtitleTextResId,
            @ColorRes final int chatToolbarColorResId,
            @ColorRes final int chatToolbarTextColorResId,
            @ColorRes final int chatStatusBarColorResId,
            @ColorRes final int menuItemTextColorResId,
            @ColorRes final int chatToolbarHintTextColor,
            final boolean showBackButton) {
        this.chatTitleTextResId = chatTitleTextResId;
        this.chatSubtitleTextResId = chatSubtitleTextResId;
        this.chatToolbarColorResId = chatToolbarColorResId;
        this.chatToolbarTextColorResId = chatToolbarTextColorResId;
        this.chatStatusBarColorResId = chatStatusBarColorResId;
        this.menuItemTextColorResId = menuItemTextColorResId;
        this.chatToolbarHintTextColor = chatToolbarHintTextColor;
        this.showBackButton = showBackButton;
        return this;
    }

    /**
     * Default values:
     *
     * @param chatBackgroundColor                - R.color.threads_chat_background
     * @param chatHighlightingColor              - R.color.threads_chat_highlighting
     * @param incomingMessageBubbleColor         - R.color.threads_chat_incoming_message_bubble
     * @param outgoingMessageBubbleColor         - R.color.threads_chat_outgoing_message_bubble
     * @param incomingMessageBubbleBackground    - R.drawable.thread_incoming_bubble
     * @param outgoingMessageBubbleBackground    - R.drawable.thread_outgoing_bubble
     * @param incomingMessageTextColor           - R.color.threads_incoming_message_text
     * @param outgoingMessageTextColor           - R.color.threads_outgoing_message_text
     * @param incomingMessageTimeColor           - R.color.threads_operator_message_timestamp;
     * @param outgoingMessageTimeColor           - R.color.threads_user_message_timestamp;
     * @param outgoingImageBubbleMask            - R.drawable.thread_outgoing_image_mask
     * @param outgoingImageTimeColor             = R.color.threads_outgoing_message_time;
     * @param outgoingImageTimeBackgroundColor   = R.color.threads_outgoing_time_underlay;
     * @param incomingImageBubbleMask            - R.drawable.thread_incoming_image_mask
     * @param incomingImageTimeColor             = R.color.threads_incoming_message_time;
     * @param incomingImageTimeBackgroundColor   = R.color.threads_incoming_time_underlay;
     * @param incomingMessageLinkColor           - R.color.threads_incoming_message_link;
     * @param outgoingMessageLinkColor           - R.color.threads_outgoing_message_link;
     * @param chatBodyIconsTint                  - R.color.threads_chat_icons_tint
     * @param chatSystemMessageTextColor         - R.color.threads_chat_connection_message
     * @param filesAndMediaScreenBackgroundColor - R.color.threads_files_medias_screen_background
     * @param filesAndMediaTextColor             = R.color.threads_files_list
     * @param iconsAndSeparatorsColor            - R.color.threads_icon_and_separators_color
     * @param defaultOperatorAvatar              - R.drawable.threads_operator_avatar_placeholder
     * @param operatorAvatarSize                 - R.dimen.threads_operator_photo_size
     * @param operatorSystemAvatarSize           - R.dimen.threads_system_operator_photo_size
     * @param imagePlaceholder                   - R.drawable.threads_image_placeholder
     * @param fileBrowserDialogStyleResId        - R.style.FileDialogStyleTransparent
     * @param showConsultSearching               - setShowConsultSearching(boolean show)
     * @param scrollChatToEndIfUserTyping        - setScrollChatToEndIfUserTyping(boolean scroll)
     * @param scrollDownButtonResId              - R.drawable.threads_scroll_down_btn_back
     * @param unreadMsgStickerColorResId         - R.color.threads_chat_unread_msg_sticker_background
     * @param unreadMsgCountTextColorResId       - R.color.threads_chat_unread_msg_count_text
     * @return Builder
     */
    public ChatStyle setChatBodyStyle(
            @ColorRes final int chatBackgroundColor,
            @ColorRes final int chatHighlightingColor,
            @ColorRes final int incomingMessageBubbleColor,
            @ColorRes final int outgoingMessageBubbleColor,
            @DrawableRes final int incomingMessageBubbleBackground,
            @DrawableRes final int outgoingMessageBubbleBackground,
            @ColorRes final int incomingMessageTextColor,
            @ColorRes final int outgoingMessageTextColor,
            @ColorRes int incomingMessageTimeColor,
            @ColorRes int outgoingMessageTimeColor,
            @DrawableRes int outgoingImageBubbleMask,
            @ColorRes int outgoingImageTimeColor,
            @ColorRes int outgoingImageTimeBackgroundColor,
            @DrawableRes int incomingImageBubbleMask,
            @ColorRes int incomingImageTimeColor,
            @ColorRes int incomingImageTimeBackgroundColor,
            @ColorRes int incomingMessageLinkColor,
            @ColorRes int outgoingMessageLinkColor,
            @ColorRes final int chatBodyIconsTint,
            @ColorRes final int chatSystemMessageTextColor,
            @ColorRes final int filesAndMediaScreenBackgroundColor,
            @ColorRes int filesAndMediaTextColor,
            @ColorRes final int iconsAndSeparatorsColor,
            @DrawableRes final int defaultOperatorAvatar,
            @DimenRes final int operatorAvatarSize,
            @DimenRes final int operatorSystemAvatarSize,
            @DrawableRes final int imagePlaceholder,
            @StyleRes final int fileBrowserDialogStyleResId,
            final boolean showConsultSearching,
            final boolean scrollChatToEndIfUserTyping,
            @DrawableRes final int scrollDownButtonResId,
            @ColorRes final int unreadMsgStickerColorResId,
            @ColorRes final int unreadMsgCountTextColorResId) {

        this.chatBackgroundColor = chatBackgroundColor;
        this.chatHighlightingColor = chatHighlightingColor;

        this.incomingMessageBubbleColor = incomingMessageBubbleColor;
        this.outgoingMessageBubbleColor = outgoingMessageBubbleColor;
        this.incomingMessageBubbleBackground = incomingMessageBubbleBackground;
        this.outgoingMessageBubbleBackground = outgoingMessageBubbleBackground;

        this.incomingMessageTextColor = incomingMessageTextColor;
        this.outgoingMessageTextColor = outgoingMessageTextColor;
        this.incomingMessageTimeColor = incomingMessageTimeColor;
        this.outgoingMessageTimeColor = outgoingMessageTimeColor;

        this.outgoingImageBubbleMask = outgoingImageBubbleMask;
        this.outgoingImageTimeColor = outgoingImageTimeColor;
        this.outgoingImageTimeBackgroundColor = outgoingImageTimeBackgroundColor;

        this.incomingImageBubbleMask = incomingImageBubbleMask;
        this.incomingImageTimeColor = incomingImageTimeColor;
        this.incomingImageTimeBackgroundColor = incomingImageTimeBackgroundColor;

        this.incomingMessageLinkColor = incomingMessageLinkColor;
        this.outgoingMessageLinkColor = outgoingMessageLinkColor;

        this.defaultOperatorAvatar = defaultOperatorAvatar;
        this.operatorAvatarSize = operatorAvatarSize;
        this.operatorSystemAvatarSize = operatorSystemAvatarSize;

        this.imagePlaceholder = imagePlaceholder;
        this.chatBodyIconsTint = chatBodyIconsTint;
        this.chatSystemMessageTextColor = chatSystemMessageTextColor;

        this.filesAndMediaScreenBackgroundColor = filesAndMediaScreenBackgroundColor;
        this.filesAndMediaTextColor = filesAndMediaTextColor;

        this.iconsAndSeparatorsColor = iconsAndSeparatorsColor;
        this.fileBrowserDialogStyleResId = fileBrowserDialogStyleResId;

        this.showConsultSearching = showConsultSearching;

        this.scrollChatToEndIfUserTyping = scrollChatToEndIfUserTyping;
        this.scrollDownButtonResId = scrollDownButtonResId;

        this.unreadMsgStickerColorResId = unreadMsgStickerColorResId;
        this.unreadMsgCountTextColorResId = unreadMsgCountTextColorResId;

        return this;
    }


    /**
     * Default values:
     *
     * @param chatMessageInputHintTextColor - R.color.threads_input_hint
     * @param chatMessageInputColor         - R.color.threads_input_background
     * @param inputTextColor                - R.color.threads_input_text
     * @param inputTextFont                 - setInputTextFont(String path)
     * @param attachmentsIconResId          - R.drawable.threads_ic_attachment_button
     * @param sendMessageIconResId          - R.drawable.threads_ic_send_button
     * @param inputHint                     - R.string.threads_input_hint
     * @param inputHeight                   - R.dimen.threads_input_height
     * @param inputBackground               - R.drawable.threads_chat_input_background
     * @return Builder
     */
    public ChatStyle setChatInputStyle(
            @ColorRes final int chatMessageInputHintTextColor,
            @ColorRes final int chatMessageInputColor,
            @ColorRes final int inputTextColor,
            final String inputTextFont,
            @DrawableRes final int attachmentsIconResId,
            @DrawableRes final int sendMessageIconResId,
            @StringRes final int inputHint,
            @DimenRes final int inputHeight,
            @DrawableRes final int inputBackground) {

        this.chatMessageInputColor = chatMessageInputColor;
        this.chatMessageInputHintTextColor = chatMessageInputHintTextColor;
        this.inputTextColor = inputTextColor;
        this.inputTextFont = inputTextFont;
        this.attachmentsIconResId = attachmentsIconResId;
        this.sendMessageIconResId = sendMessageIconResId;
        this.inputHint = inputHint;
        this.inputHeight = inputHeight;
        this.inputBackground = inputBackground;
        return this;
    }

    /**
     * Default values:
     *
     * @param defPushIconResId                 - R.drawable.default_push_icon
     * @param defTitleResId                    - R.string.threads_push_title
     * @param pushBackgroundColorResId         - R.color.threads_push_background
     * @param nougatPushAccentColorResId       - R.color.threads_nougat_push_accent
     * @param quickReplyMessageBackgroundColor = R.color.threads_quick_reply_message_background;
     * @param quickReplyMessageTextColor       = R.color.threads_quick_reply_message_text_color;
     * @return Builder
     */
    public ChatStyle setPushNotificationStyle(@DrawableRes final int defPushIconResId,
                                              @StringRes final int defTitleResId,
                                              @ColorRes final int pushBackgroundColorResId,
                                              @ColorRes final int nougatPushAccentColorResId,
                                              @ColorRes int quickReplyMessageBackgroundColor,
                                              @ColorRes int quickReplyMessageTextColor) {

        this.defPushIconResId = defPushIconResId;
        this.defTitleResId = defTitleResId;
        this.pushBackgroundColorResId = pushBackgroundColorResId;
        this.nougatPushAccentColorResId = nougatPushAccentColorResId;
        this.quickReplyMessageBackgroundColor = quickReplyMessageBackgroundColor;
        this.quickReplyMessageTextColor = quickReplyMessageTextColor;
        return this;
    }

    //images history screen


    /**
     * Default values:
     *
     * @param imagesScreenToolbarColor    = R.color.threads_attachments_toolbar;
     * @param imagesScreenBackgroundColor = R.color.threads_attachments_background;
     * @param imagesScreenAuthorTextColor = R.color.threads_attachments_author_text_color;
     * @param imagesScreenDateTextColor   = R.color.threads_attachments_date_text_color;
     * @param imagesScreenAuthorTextSize  = R.dimen.threads_attachments_author_text_size;
     * @param imagesScreenDateTextSize    = R.dimen.threads_attachments_date_text_size;
     */
    public ChatStyle setImagesGalleryStyle(@ColorRes int imagesScreenToolbarColor,
                                           @ColorRes int imagesScreenBackgroundColor,
                                           @ColorRes int imagesScreenAuthorTextColor,
                                           @ColorRes int imagesScreenDateTextColor,
                                           @DimenRes int imagesScreenAuthorTextSize,
                                           @DimenRes int imagesScreenDateTextSize) {

        this.imagesScreenToolbarColor = imagesScreenToolbarColor;
        this.imagesScreenBackgroundColor = imagesScreenBackgroundColor;
        this.imagesScreenAuthorTextColor = imagesScreenAuthorTextColor;
        this.imagesScreenDateTextColor = imagesScreenDateTextColor;
        this.imagesScreenAuthorTextSize = imagesScreenAuthorTextSize;
        this.imagesScreenDateTextSize = imagesScreenDateTextSize;
        return this;
    }

    /**
     * Default values:
     *
     * @param requestToResolveThreadTextResId        - R.string.threads_request_to_resolve_thread
     * @param approveRequestToResolveThreadTextResId - R.string.threads_request_to_resolve_thread_close
     * @param denyRequestToResolveThreadTextResId    - R.string.threads_request_to_resolve_thread_open
     */
    public ChatStyle setRequestResolveThreadStyle(@StringRes final int requestToResolveThreadTextResId,
                                                  @StringRes final int approveRequestToResolveThreadTextResId,
                                                  @StringRes final int denyRequestToResolveThreadTextResId) {
        this.requestToResolveThreadTextResId = requestToResolveThreadTextResId;
        this.approveRequestToResolveThreadTextResId = approveRequestToResolveThreadTextResId;
        this.denyRequestToResolveThreadTextResId = denyRequestToResolveThreadTextResId;
        return this;
    }

    /**
     * Default values:
     *
     * @param binarySurveyLikeUnselectedIconResId    - R.drawable.threads_binary_survey_like_unselected
     * @param binarySurveyLikeSelectedIconResId      - R.drawable.threads_binary_survey_like_selected
     * @param binarySurveyDislikeUnselectedIconResId - R.drawable.threads_binary_survey_dislike_unselected
     * @param binarySurveyDislikeSelectedIconResId   - R.drawable.threads_binary_survey_dislike_selected
     * @param optionsSurveyUnselectedIconResId       - R.drawable.threads_options_survey_unselected
     * @param optionsSurveySelectedIconResId         - R.drawable.threads_options_survey_selected
     * @param surveySelectedColorFilterResId         - R.color.threads_survey_selected_icon_tint
     * @param surveyUnselectedColorFilterResId       - R.color.threads_survey_unselected_icon_tint
     * @param surveyTextColorResId                   - R.color.threads_chat_system_message
     * @param surveyChoicesTextColorResId            - R.color.threads_survey_choices_text
     */
    public ChatStyle setSurveyStyle(@DrawableRes final int binarySurveyLikeUnselectedIconResId,
                                    @DrawableRes final int binarySurveyLikeSelectedIconResId,
                                    @DrawableRes final int binarySurveyDislikeUnselectedIconResId,
                                    @DrawableRes final int binarySurveyDislikeSelectedIconResId,
                                    @DrawableRes final int optionsSurveyUnselectedIconResId,
                                    @DrawableRes final int optionsSurveySelectedIconResId,
                                    @ColorRes final int surveySelectedColorFilterResId,
                                    @ColorRes final int surveyUnselectedColorFilterResId,
                                    @ColorRes final int surveyTextColorResId,
                                    @ColorRes int surveyChoicesTextColorResId) {
        this.binarySurveyLikeUnselectedIconResId = binarySurveyLikeUnselectedIconResId;
        this.binarySurveyLikeSelectedIconResId = binarySurveyLikeSelectedIconResId;
        this.binarySurveyDislikeUnselectedIconResId = binarySurveyDislikeUnselectedIconResId;
        this.binarySurveyDislikeSelectedIconResId = binarySurveyDislikeSelectedIconResId;
        this.optionsSurveyUnselectedIconResId = optionsSurveyUnselectedIconResId;
        this.optionsSurveySelectedIconResId = optionsSurveySelectedIconResId;
        this.surveySelectedColorFilterResId = surveySelectedColorFilterResId;
        this.surveyUnselectedColorFilterResId = surveyUnselectedColorFilterResId;
        this.surveyTextColorResId = surveyTextColorResId;
        this.surveyChoicesTextColorResId = surveyChoicesTextColorResId;
        return this;
    }


    /**
     * Default values:
     *
     * @param scheduleMessageIconResId      - R.drawable.threads_schedule_icon
     * @param scheduleMessageTextColorResId - R.color.threads_schedule_text
     * @return Builder
     */
    public ChatStyle setScheduleMessageStyle(
            @DrawableRes final int scheduleMessageIconResId,
            @ColorRes final int scheduleMessageTextColorResId) {
        this.scheduleMessageIconResId = scheduleMessageIconResId;
        this.scheduleMessageTextColorResId = scheduleMessageTextColorResId;
        return this;
    }

    /**
     * Default values:
     *
     * @param welcomeScreenLogoResId              - R.drawable.threads_welcome_logo
     * @param welcomeScreenTitleTextResId         - R.string.threads_welcome_screen_title_text
     * @param welcomeScreenSubtitleTextResId      - R.string.threads_welcome_screen_subtitle_text
     * @param welcomeScreenTitleTextColorResId    - R.color.threads_welcome_screen_title
     * @param welcomeScreenSubtitleTextColorResId - R.color.threads_welcome_screen_subtitle
     * @param welcomeScreenTitleSizeInSp          - R.dimen.threads_welcome_screen_title
     * @param welcomeScreenSubtitleSizeInSp       - R.dimen.threads_welcome_screen_subtitle
     * @return Builder
     */
    public ChatStyle setWelcomeScreenStyle(
            @DrawableRes final int welcomeScreenLogoResId
            , @StringRes final int welcomeScreenTitleTextResId
            , @StringRes final int welcomeScreenSubtitleTextResId
            , @ColorRes final int welcomeScreenTitleTextColorResId
            , @ColorRes final int welcomeScreenSubtitleTextColorResId
            , final int welcomeScreenTitleSizeInSp
            , final int welcomeScreenSubtitleSizeInSp) {
        this.welcomeScreenLogoResId = welcomeScreenLogoResId;
        this.welcomeScreenTitleTextColorResId = welcomeScreenTitleTextColorResId;
        this.welcomeScreenSubtitleTextColorResId = welcomeScreenSubtitleTextColorResId;
        this.welcomeScreenTitleTextResId = welcomeScreenTitleTextResId;
        this.welcomeScreenSubtitleTextResId = welcomeScreenSubtitleTextResId;
        this.welcomeScreenTitleSizeInSp = welcomeScreenTitleSizeInSp;
        this.welcomeScreenSubtitleSizeInSp = welcomeScreenSubtitleSizeInSp;
        return this;
    }

}
