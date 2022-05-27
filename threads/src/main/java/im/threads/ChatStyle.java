package im.threads;

import android.content.Context;
import android.view.Gravity;

import androidx.annotation.ArrayRes;
import androidx.annotation.BoolRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.yydcdut.markdown.MarkdownConfiguration;

import java.io.Serializable;

/**
 * Стиль чата.
 * Хранит в себе информацию о расцветках, иконках
 * и других кастомизациях чата.
 */
public final class ChatStyle implements Serializable {

    public boolean arePermissionDescriptionDialogsEnabled = false;

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
    public int chatToolbarContextMenuColorResId = R.color.threads_chat_toolbar_context_menu;
    @ColorRes
    public int chatStatusBarColorResId = R.color.threads_chat_status_bar;
    @BoolRes
    public int windowLightStatusBarResId = R.bool.threads_chat_is_light_status_bar;
    @ColorRes
    public int menuItemTextColorResId = R.color.threads_chat_toolbar_menu_item;
    @ColorRes
    public int chatToolbarTextColorResId = R.color.threads_chat_toolbar_text;
    @ColorRes
    public int chatToolbarHintTextColor = R.color.threads_chat_toolbar_hint;
    @BoolRes
    public int fixedChatTitle = R.bool.threads_chat_fixed_chat_title;
    @BoolRes
    public int fixedChatSubtitle = R.bool.threads_chat_fixed_chat_subtitle;

    public boolean showBackButton = false;
    public boolean chatSubtitleShowOrgUnit = false;

    @ColorRes
    public int chatToolbarInverseIconTintResId = R.color.threads_green_83B144;
    @DrawableRes
    public int chatToolbarBackIconResId = R.drawable.threads_ic_arrow_back_24dp;
    @DrawableRes
    public int chatToolbarPopUpMenuIconResId = R.drawable.threads_ic_more_vert_24dp;
    @DrawableRes
    public int chatToolbarContentCopyIconResId = R.drawable.threads_ic_content_copy_24dp;
    @DrawableRes
    public int chatToolbarReplyIconResId = R.drawable.threads_ic_reply_24dp;

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

    @DimenRes
    public int bubbleOutgoingPaddingLeft = R.dimen.bubbleOutgoingPaddingLeft;
    @DimenRes
    public int bubbleOutgoingPaddingTop = R.dimen.bubbleOutgoingPaddingTop;
    @DimenRes
    public int bubbleOutgoingPaddingRight = R.dimen.bubbleOutgoingPaddingRight;
    @DimenRes
    public int bubbleOutgoingPaddingBottom = R.dimen.bubbleOutgoingPaddingBottom;

    @DimenRes
    public int bubbleIncomingPaddingLeft = R.dimen.bubbleIncomingPaddingLeft;
    @DimenRes
    public int bubbleIncomingPaddingTop = R.dimen.bubbleIncomingPaddingTop;
    @DimenRes
    public int bubbleIncomingPaddingRight = R.dimen.bubbleIncomingPaddingRight;
    @DimenRes
    public int bubbleIncomingPaddingBottom = R.dimen.bubbleIncomingPaddingBottom;

    @ColorRes
    public int incomingMessageLinkColor = R.color.threads_incoming_message_link;
    @ColorRes
    public int outgoingMessageLinkColor = R.color.threads_outgoing_message_link;

    @ColorRes
    public int incomingPlayPauseButtonColor = R.color.threads_incoming_play_pause_button;
    @ColorRes
    public int outgoingPlayPauseButtonColor = R.color.threads_outgoing_play_pause_button;
    @ColorRes
    public int previewPlayPauseButtonColor = R.color.threads_preview_play_pause_button;
    @DrawableRes
    public int voiceMessagePlayButton = R.drawable.threads_voice_message_play;
    @DrawableRes
    public int voiceMessagePauseButton = R.drawable.threads_voice_message_pause;

    @ColorRes
    public int chatBodyIconsTint = R.color.threads_green_83B144;
    public int [] chatBodyIconsColorState = null;
    @ColorRes
    public int chatSystemMessageTextColor = R.color.threads_chat_connection_message;

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
    public boolean inputEnabledDuringQuickReplies = false;
    @DrawableRes
    public int scrollDownIconResId = R.drawable.threads_scroll_down_icon;
    @DrawableRes
    public int scrollDownBackgroundResId = R.drawable.threads_scroll_down_background;
    @DimenRes
    public int scrollDownButtonWidth = R.dimen.threads_scroll_down_button_width;
    @DimenRes
    public int scrollDownButtonHeight = R.dimen.threads_scroll_down_button_height;
    @DimenRes
    public int scrollDownButtonMargin = R.dimen.threads_scroll_down_button_margin;
    @DimenRes
    public int scrollDownButtonElevation = R.dimen.threads_scroll_down_button_elevation;
    @ColorRes
    public int unreadMsgStickerColorResId = R.color.threads_chat_unread_msg_sticker_background;
    @ColorRes
    public int unreadMsgCountTextColorResId = R.color.threads_chat_unread_msg_count_text;
    @ArrayRes
    public int threadsSwipeRefreshColors = R.array.threads_swipe_refresh_colors;

    @DrawableRes
    public int threadsRecordButtonBackground = R.drawable.threads_record_button_background;
    @ColorRes
    public int threadsRecordButtonBackgroundColor = R.color.threads_record_button_background;
    @DrawableRes
    public int threadsRecordButtonIcon = R.drawable.threads_record_button_icon;
    @ColorRes
    public int threadsRecordButtonIconColor = R.color.threads_record_button_icon;
    @ColorRes
    public int threadsRecordButtonSmallMicColor = R.color.threads_record_button_small_mic;

    //download button
    @ColorRes
    public int downloadButtonTintResId = R.color.threads_green_83B144;
    @ColorRes
    public int loaderTintResId = R.color.threads_green_83B144;
    @DrawableRes
    public int startDownloadIconResId = R.drawable.threads_ic_vertical_align_bottom_18dp;
    @DrawableRes
    public int inProgressIconResId = R.drawable.threads_ic_clear_36dp;
    @DrawableRes
    public int completedIconResId = R.drawable.threads_ic_file_outline_24dp;

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
    public int imagesScreenBackgroundColor = R.color.threads_attachments_background;
    @ColorRes
    public int imagesScreenAuthorTextColor = R.color.threads_attachments_author_text_color;
    @ColorRes
    public int imagesScreenDateTextColor = R.color.threads_attachments_date_text_color;
    @DimenRes
    public int imagesScreenAuthorTextSize = R.dimen.threads_attachments_author_text_size;
    @DimenRes
    public int imagesScreenDateTextSize = R.dimen.threads_attachments_date_text_size;

    // quote style
    @ColorRes
    public int quoteClearIconTintResId = R.color.threads_green_83B144;
    @DrawableRes
    public int quoteClearIconResId = R.drawable.threads_ic_clear_36dp;

    // chat input style
    @ColorRes
    public int chatMessageInputColor = R.color.threads_input_background;
    @ColorRes
    public int chatMessageInputHintTextColor = R.color.threads_input_hint;
    @ColorRes
    public int inputTextColor = R.color.threads_input_text;
    @Nullable
    public String inputTextFont;
    @ColorRes
    public int inputIconTintResId = R.color.threads_green_83B144;
    @DrawableRes
    public int attachmentIconResId = R.drawable.threads_ic_attachment_button;
    @DrawableRes
    public int sendMessageIconResId = R.drawable.threads_ic_send_button;
    @StringRes
    public int inputHint = R.string.threads_input_hint;
    @DimenRes
    public int inputHeight = R.dimen.threads_input_height;
    @DrawableRes
    public int inputBackground = R.drawable.threads_chat_input_background;

    //attachment bottom sheet style
    @ColorRes
    public int attachmentBottomSheetButtonTintResId = R.color.threads_green_83B144;
    @DrawableRes
    public int attachmentDoneIconResId = R.drawable.threads_ic_circle_done_36dp;
    @DrawableRes
    public int attachmentCameraIconResId = R.drawable.threads_ic_camera_42dp;
    @DrawableRes
    public int attachmentGalleryIconResId = R.drawable.threads_ic_photo_42dp;
    @DrawableRes
    public int attachmentFileIconResId = R.drawable.threads_ic_file_fill_42dp;
    @DrawableRes
    public int attachmentSelfieCameraIconResId = R.drawable.threads_ic_camera_front_42dp;
    @DrawableRes
    public int attachmentSendIconResId = R.drawable.threads_ic_send_42dp;

    // Media and files screen
    @BoolRes
    public int mediaAndFilesWindowLightStatusBarResId = R.bool.threads_chat_is_light_status_bar;
    @ColorRes
    public int mediaAndFilesStatusBarColorResId = R.color.threads_chat_status_bar;
    @ColorRes
    public int mediaAndFilesToolbarColorResId = R.color.threads_chat_toolbar;
    @ColorRes
    public int mediaAndFilesToolbarTextColorResId = R.color.threads_chat_toolbar_text;
    @ColorRes
    public int mediaAndFilesToolbarHintTextColor = R.color.threads_chat_toolbar_hint;
    @ColorRes
    public int mediaAndFilesScreenBackgroundColor = R.color.threads_files_medias_screen_background;
    @ColorRes
    public int mediaAndFilesTextColor = R.color.threads_files_list;
    @ColorRes
    public int mediaAndFilesFileIconTintResId = R.color.threads_green_83B144;
    @DrawableRes
    public int mediaAndFilesFileIconResId = R.drawable.threads_ic_file_fill_36dp;

    // Empty Media and files screen header
    @StringRes
    public int emptyMediaAndFilesHeaderText = R.string.threads_no_media_and_files_header;
    public String emptyMediaAndFilesHeaderFontPath;
    @DimenRes
    public int emptyMediaAndFilesHeaderTextSize = R.dimen.text_medium;
    @ColorRes
    public int emptyMediaAndFilesHeaderTextColor = R.color.threads_files_list;

    // Empty Media and files screen description
    @StringRes
    public int emptyMediaAndFilesDescriptionText = R.string.threads_no_media_and_files_description;
    public String emptyMediaAndFilesDescriptionFontPath;
    @DimenRes
    public int emptyMediaAndFilesDescriptionTextSize = R.dimen.text_regular;
    @ColorRes
    public int emptyMediaAndFilesDescriptionTextColor = R.color.threads_files_list;

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
    public int notificationQuickReplyMessageBackgroundColor = R.color.threads_notification_quick_reply_message_background;
    @ColorRes
    public int notificationQuickReplyMessageTextColor = R.color.threads_notification_quick_reply_message_text_color;

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
    @DimenRes
    public int welcomeScreenTitleSizeInSp = R.dimen.threads_welcome_screen_title;
    @DimenRes
    public int welcomeScreenSubtitleSizeInSp = R.dimen.threads_welcome_screen_subtitle;
    @DimenRes
    public int welcomeScreenLogoWidth = R.dimen.threads_welcome_logo_width;
    @DimenRes
    public int welcomeScreenLogoHeight = R.dimen.threads_welcome_logo_height;

    // empty state
    @ColorRes
    public int emptyStateBackgroundColorResId = R.color.threads_empty_state_background;
    @ColorRes
    public int emptyStateProgressBarColorResId = R.color.threads_empty_state_progress;
    @ColorRes
    public int emptyStateHintColorResId = R.color.threads_empty_state_hint;
    @StringRes
    public int loaderTextResId = R.string.loading;

    // system messages
    @DimenRes
    public int systemMessageTextSize = R.dimen.threads_system_message_text_size;
    @ColorRes
    public int systemMessageTextColorResId = R.color.threads_chat_new_system_message;
    @DimenRes
    public int systemMessageLeftRightPadding = R.dimen.threads_system_message_left_right_padding;
    public int systemMessageTextGravity = Gravity.CENTER;
    @ColorRes
    public int systemMessageLinkColor = R.color.threads_system_message_link;
    @DrawableRes
    public int quickReplyButtonBackground = R.drawable.threads_quick_reply_button_background;
    @ColorRes
    public int quickReplyTextColor = R.color.threads_quick_reply_text_color;
    @IntegerRes
    public int maxGalleryImagesCount = R.integer.max_count_attached_images;
    @IntegerRes
    public int maxGalleryImagesCountFixedBySystem = R.integer.max_count_attached_images_final;
    @ColorRes
    public int consultSearchingProgressColor = R.color.threads_consult_searching_progress_color;

    // set can show specialist info
    public boolean canShowSpecialistInfo = true;

    public boolean useExternalCameraApp = true;

    public boolean selfieEnabled = false;

    public boolean voiceMessageEnabled = false;

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
    @Nullable
    public String systemMessageFont;

    // Конфигурации markdown в сообщениях
    public MarkdownConfiguration incomingMarkdownConfiguration, outgoingMarkdownConfiguration;

    public ChatStyle() {
    }

    public ChatStyle setArePermissionDescriptionDialogsEnabled(final boolean areEnabled) {
        this.arePermissionDescriptionDialogsEnabled = areEnabled;
        return this;
    }

    // <editor-fold defaultState="collapsed" desc="chat title style">
    public ChatStyle showChatBackButton(final boolean showBackButton) {
        this.showBackButton = showBackButton;
        return this;
    }

    public ChatStyle setFixedChatTitle(@BoolRes final int fixedChatTitle) {
        this.fixedChatTitle = fixedChatTitle;
        return this;
    }

    public ChatStyle setFixedChatSubtitle(@BoolRes final int fixedChatSubtitle) {
        this.fixedChatSubtitle = fixedChatSubtitle;
        return this;
    }

    public ChatStyle setChatSubtitleShowConsultOrgUnit(boolean showConsultOrgUnit) {
        this.chatSubtitleShowOrgUnit = showConsultOrgUnit;
        return this;
    }

    public ChatStyle setChatToolbarInverseIconTintResId(
            @ColorRes int chatToolbarInverseIconTintResId) {
        this.chatToolbarInverseIconTintResId = chatToolbarInverseIconTintResId;
        return this;
    }

    public ChatStyle setChatToolbarBackIconResId(
            @DrawableRes int chatToolbarBackIconResId) {
        this.chatToolbarBackIconResId = chatToolbarBackIconResId;
        return this;
    }

    public ChatStyle setChatToolbarPopUpMenuIconResId(
            @DrawableRes int chatToolbarPopUpMenuIconResId) {
        this.chatToolbarPopUpMenuIconResId = chatToolbarPopUpMenuIconResId;
        return this;
    }

    public ChatStyle setChatToolbarContentCopyIconResId(
            @DrawableRes int chatToolbarContentCopyIconResId) {
        this.chatToolbarContentCopyIconResId = chatToolbarContentCopyIconResId;
        return this;
    }

    public ChatStyle setChatToolbarReplyIconResId(
            @DrawableRes int chatToolbarReplyIconResId) {
        this.chatToolbarReplyIconResId = chatToolbarReplyIconResId;
        return this;
    }
    // </editor-fold>

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

    public ChatStyle setSelfieEnabled(final boolean selfieEnabled) {
        this.selfieEnabled = selfieEnabled;
        return this;
    }

    public ChatStyle setVoiceMessageEnabled(final boolean voiceMessageEnabled) {
        this.voiceMessageEnabled = voiceMessageEnabled;
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

    public ChatStyle setSystemMessageFont(final String path) {
        this.systemMessageFont = path;
        return this;
    }

    public ChatStyle setLoaderTextResId(final int loaderTextResId) {
        this.loaderTextResId = loaderTextResId;
        return this;
    }

    public ChatStyle setInputEnabledDuringQuickReplies(final boolean inputEnabledDuringQuickReplies) {
        this.inputEnabledDuringQuickReplies = inputEnabledDuringQuickReplies;
        return this;
    }

    // <editor-fold defaultState="collapsed" desc="quote style">
    public ChatStyle setQuoteClearIconTintResId(@ColorRes final int quoteClearIconTintResId) {
        this.quoteClearIconTintResId = quoteClearIconTintResId;
        return this;
    }

    public ChatStyle setQuoteClearIconResId(@DrawableRes final int quoteClearIconResId) {
        this.quoteClearIconResId = quoteClearIconResId;
        return this;
    }
    // </editor-fold>

    // <editor-fold defaultState="collapsed" desc="attachment bottom sheet style">
    public ChatStyle setAttachmentBottomSheetButtonTintResId(@ColorRes final int attachmentBottomSheetButtonTintResId) {
        this.attachmentBottomSheetButtonTintResId = attachmentBottomSheetButtonTintResId;
        return this;
    }

    public ChatStyle setAttachmentDoneIconResId(@DrawableRes final int attachmentDoneIconResId) {
        this.attachmentDoneIconResId = attachmentDoneIconResId;
        return this;
    }

    public ChatStyle setAttachmentCameraIconResId(@DrawableRes final int attachmentCameraIconResId) {
        this.attachmentCameraIconResId = attachmentCameraIconResId;
        return this;
    }

    public ChatStyle setAttachmentGalleryIconResId(@DrawableRes final int attachmentGalleryIconResId) {
        this.attachmentGalleryIconResId = attachmentGalleryIconResId;
        return this;
    }

    public ChatStyle setAttachmentFileIconResId(@DrawableRes final int attachmentFileIconResId) {
        this.attachmentFileIconResId = attachmentFileIconResId;
        return this;
    }

    public ChatStyle setAttachmentSelfieCameraIconResId(@DrawableRes final int attachmentSelfieCameraIconResId) {
        this.attachmentSelfieCameraIconResId = attachmentSelfieCameraIconResId;
        return this;
    }

    public ChatStyle setAttachmentSendIconResId(@DrawableRes final int attachmentSendIconResId) {
        this.attachmentSendIconResId = attachmentSendIconResId;
        return this;
    }
    // </editor-fold>

    // <editor-fold defaultState="collapsed" desc="MediaAndFiles Screen">
    public ChatStyle setMediaAndFilesWindowLightStatusBarResId(@BoolRes final int mediaAndFilesWindowLightStatusBarResId) {
        this.mediaAndFilesWindowLightStatusBarResId = mediaAndFilesWindowLightStatusBarResId;
        return this;
    }

    public ChatStyle setMediaAndFilesStatusBarColorResId(@ColorRes final int mediaAndFilesStatusBarColorResId) {
        this.mediaAndFilesStatusBarColorResId = mediaAndFilesStatusBarColorResId;
        return this;
    }

    public ChatStyle setMediaAndFilesToolbarColorResId(@ColorRes final int mediaAndFilesToolbarColorResId) {
        this.mediaAndFilesToolbarColorResId = mediaAndFilesToolbarColorResId;
        return this;
    }

    public ChatStyle setMediaAndFilesToolbarTextColorResId(@ColorRes final int mediaAndFilesToolbarTextColorResId) {
        this.mediaAndFilesToolbarTextColorResId = mediaAndFilesToolbarTextColorResId;
        return this;
    }

    public ChatStyle setMediaAndFilesToolbarHintTextColor(@ColorRes final int mediaAndFilesToolbarHintTextColor) {
        this.mediaAndFilesToolbarHintTextColor = mediaAndFilesToolbarHintTextColor;
        return this;
    }

    public ChatStyle setMediaAndFilesScreenBackgroundColor(@ColorRes final int mediaAndFilesScreenBackgroundColor) {
        this.mediaAndFilesScreenBackgroundColor = mediaAndFilesScreenBackgroundColor;
        return this;
    }

    public ChatStyle setMediaAndFilesTextColor(@ColorRes final int mediaAndFilesTextColor) {
        this.mediaAndFilesTextColor = mediaAndFilesTextColor;
        return this;
    }

    public ChatStyle setMediaAndFilesFileIconTintResId(@ColorRes final int mediaAndFilesFileIconTintResId) {
        this.mediaAndFilesFileIconTintResId = mediaAndFilesFileIconTintResId;
        return this;
    }

    public ChatStyle setMediaAndFilesFileIconResId(@DrawableRes final int mediaAndFilesFileIconResId) {
        this.mediaAndFilesFileIconResId = mediaAndFilesFileIconResId;
        return this;
    }

    public ChatStyle setEmptyMediaAndFilesHeaderTextResId(@StringRes final int headerTextResId) {
        this.emptyMediaAndFilesHeaderText = headerTextResId;
        return this;
    }

    /**
     * @param headerFontPath path like "fonts/lato-regular.ttf"
     * @return ChatStyle
     */
    public ChatStyle setEmptyMediaAndFilesHeaderFontPath(@NonNull final String headerFontPath) {
        this.emptyMediaAndFilesHeaderFontPath = headerFontPath;
        return this;
    }

    public ChatStyle setEmptyMediaAndFilesHeaderTextSize(@DimenRes final int headerTextSizeResId) {
        this.emptyMediaAndFilesHeaderTextSize = headerTextSizeResId;
        return this;
    }

    public ChatStyle setEmptyMediaAndFilesHeaderTextColor(@ColorRes final int headerTextColorResId) {
        this.emptyMediaAndFilesHeaderTextColor = headerTextColorResId;
        return this;
    }

    public ChatStyle setEmptyMediaAndFilesDescriptionTextResId(@StringRes final int descriptionTextResId) {
        this.emptyMediaAndFilesDescriptionText = descriptionTextResId;
        return this;
    }

    /**
     * @param descriptionFontPath path like "fonts/lato-regular.ttf"
     * @return ChatStyle
     */
    public ChatStyle setEmptyMediaAndFilesDescriptionFontPath(@NonNull final String descriptionFontPath) {
        this.emptyMediaAndFilesDescriptionFontPath = descriptionFontPath;
        return this;
    }

    public ChatStyle setEmptyMediaAndFilesDescriptionTextSize(@DimenRes final int descriptionTextSizeResId) {
        this.emptyMediaAndFilesDescriptionTextSize = descriptionTextSizeResId;
        return this;
    }

    public ChatStyle setEmptyMediaAndFilesDescriptionTextColor(@ColorRes final int descriptionTextColorResId) {
        this.emptyMediaAndFilesDescriptionTextColor = descriptionTextColorResId;
        return this;
    }
    // </editor-fold>

    public ChatStyle setChatBodyIconsTint(@ColorRes final int chatBodyIconsTint) {
        this.chatBodyIconsTint = chatBodyIconsTint;
        return this;
    }

    public ChatStyle setChatBodyIconsColorStateTint(@ColorRes final int iconStateDisabledTint,
                                                    @ColorRes final int iconStateEnabledTint,
                                                    @ColorRes final int iconStatePressedTint) {
        this.chatBodyIconsColorState = new int[] {
                iconStateDisabledTint,
                iconStateEnabledTint,
                iconStatePressedTint};
        return this;
    }

    public ChatStyle setInputIconTintResId(@ColorRes final int inputIconTintResId) {
        this.inputIconTintResId = inputIconTintResId;
        return this;
    }

    public ChatStyle setAttachmentIconResId(@DrawableRes final int attachmentIconResId) {
        this.attachmentIconResId = attachmentIconResId;
        return this;
    }

    public ChatStyle setSendMessageIconResId(@DrawableRes final int sendMessageIconResId) {
        this.sendMessageIconResId = sendMessageIconResId;
        return this;
    }

    public ChatStyle setDownloadButtonTintResId(@ColorRes final int downloadButtonTintResId) {
        this.downloadButtonTintResId = downloadButtonTintResId;
        return this;
    }

    public ChatStyle setLoaderTintResId(@ColorRes final int loaderTintResId) {
        this.loaderTintResId = loaderTintResId;
        return this;
    }

    public ChatStyle setStartDownloadIconResId(@DrawableRes final int startDownloadIconResId) {
        this.startDownloadIconResId = startDownloadIconResId;
        return this;
    }

    public ChatStyle setInProgressIconResId(@DrawableRes final int inProgressIconResId) {
        this.inProgressIconResId = inProgressIconResId;
        return this;
    }

    public ChatStyle setCompletedIconResId(@DrawableRes final int completedIconResId) {
        this.completedIconResId = completedIconResId;
        return this;
    }

    /**
     * @see android.view.Gravity
     */
    public ChatStyle setSystemMessageTextGravity(final int systemMessageTextGravity) {
        this.systemMessageTextGravity = systemMessageTextGravity;
        return this;
    }

    // deprecated setters

    /**
     * Default values:
     *
     * @param chatTitleTextResId               - R.string.threads_contact_center
     * @param chatSubtitleTextResId            - R.string.threads_operator_subtitle
     * @param chatToolbarColorResId            - R.color.threads_chat_toolbar
     * @param chatToolbarContextMenuColorResId - R.color.threads_chat_toolbar_context_menu
     * @param chatToolbarTextColorResId        - R.color.threads_chat_toolbar_text
     * @param chatStatusBarColorResId          - R.color.threads_chat_status_bar
     * @param menuItemTextColorResId           - R.color.threads_chat_toolbar_menu_item
     * @param chatToolbarHintTextColor         - R.color.threads_chat_toolbar_hint
     * @param showBackButton                   - showChatBackButton(boolean showBackButton)
     */
    @Deprecated
    public ChatStyle setChatTitleStyle(
            @StringRes final int chatTitleTextResId,
            @StringRes final int chatSubtitleTextResId,
            @ColorRes final int chatToolbarColorResId,
            @ColorRes final int chatToolbarContextMenuColorResId,
            @ColorRes final int chatToolbarTextColorResId,
            @ColorRes final int chatStatusBarColorResId,
            @ColorRes final int menuItemTextColorResId,
            @ColorRes final int chatToolbarHintTextColor,
            final boolean showBackButton) {
        this.chatTitleTextResId = chatTitleTextResId;
        this.chatSubtitleTextResId = chatSubtitleTextResId;
        this.chatToolbarColorResId = chatToolbarColorResId;
        this.chatToolbarContextMenuColorResId = chatToolbarContextMenuColorResId;
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
     * @param chatTitleTextResId               - R.string.threads_contact_center
     * @param chatSubtitleTextResId            - R.string.threads_operator_subtitle
     * @param chatToolbarColorResId            - R.color.threads_chat_toolbar
     * @param chatToolbarContextMenuColorResId - R.color.threads_chat_toolbar_context_menu
     * @param chatToolbarTextColorResId        - R.color.threads_chat_toolbar_text
     * @param chatStatusBarColorResId          - R.color.threads_chat_status_bar
     * @param windowLightStatusBarResId        - R.bool.threads_chat_is_light_status_bar
     * @param menuItemTextColorResId           - R.color.threads_chat_toolbar_menu_item
     * @param chatToolbarHintTextColor         - R.color.threads_chat_toolbar_hint
     * @param showBackButton                   - showChatBackButton(boolean showBackButton)
     */
    public ChatStyle setChatTitleStyle(
            @StringRes final int chatTitleTextResId,
            @StringRes final int chatSubtitleTextResId,
            @ColorRes final int chatToolbarColorResId,
            @ColorRes final int chatToolbarContextMenuColorResId,
            @ColorRes final int chatToolbarTextColorResId,
            @ColorRes final int chatStatusBarColorResId,
            @BoolRes final int windowLightStatusBarResId,
            @ColorRes final int menuItemTextColorResId,
            @ColorRes final int chatToolbarHintTextColor,
            final boolean showBackButton) {
        this.chatTitleTextResId = chatTitleTextResId;
        this.chatSubtitleTextResId = chatSubtitleTextResId;
        this.chatToolbarColorResId = chatToolbarColorResId;
        this.chatToolbarContextMenuColorResId = chatToolbarContextMenuColorResId;
        this.chatToolbarTextColorResId = chatToolbarTextColorResId;
        this.chatStatusBarColorResId = chatStatusBarColorResId;
        this.windowLightStatusBarResId = windowLightStatusBarResId;
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
     * @param incomingMessageTimeColor           - R.color.threads_operator_message_timestampIcon
     * @param outgoingMessageTimeColor           - R.color.threads_user_message_timestampIcon
     * @param outgoingImageBubbleMask            - R.drawable.thread_outgoing_image_mask
     * @param outgoingImageTimeColor             - R.color.threads_outgoing_message_timeIcon
     * @param outgoingImageTimeBackgroundColor   - R.color.threads_outgoing_time_underlayIcon
     * @param incomingImageBubbleMask            - R.drawable.thread_incoming_image_mask
     * @param incomingImageTimeColor             - R.color.threads_incoming_message_timeIcon
     * @param incomingImageTimeBackgroundColor   - R.color.threads_incoming_time_underlayIcon
     * @param incomingMessageLinkColor           - R.color.threads_incoming_message_link
     * @param outgoingMessageLinkColor           - R.color.threads_outgoing_message_linkIcon
     * @param chatBodyIconsTint                  - R.color.threads_chat_icons_tint
     * @param chatSystemMessageTextColor         - R.color.threads_chat_connection_message
     * @param mediaAndFilesScreenBackgroundColor - R.color.threads_files_medias_screen_background
     * @param mediaAndFilesTextColor             - R.color.threads_files_list
     * @param iconsAndSeparatorsColor            - R.color.threads_icon_and_separators_color
     * @param defaultOperatorAvatar              - R.drawable.threads_operator_avatar_placeholder
     * @param operatorAvatarSize                 - R.dimen.threads_operator_photo_size
     * @param operatorSystemAvatarSize           - R.dimen.threads_system_operator_photo_size
     * @param imagePlaceholder                   - R.drawable.threads_image_placeholder
     * @param fileBrowserDialogStyleResId        - R.style.FileDialogStyleTransparent
     * @param showConsultSearching               - setShowConsultSearching(boolean show)
     * @param scrollChatToEndIfUserTyping        - setScrollChatToEndIfUserTyping(boolean scroll)
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
            @ColorRes final int mediaAndFilesScreenBackgroundColor,
            @ColorRes int mediaAndFilesTextColor,
            @ColorRes final int iconsAndSeparatorsColor,
            @DrawableRes final int defaultOperatorAvatar,
            @DimenRes final int operatorAvatarSize,
            @DimenRes final int operatorSystemAvatarSize,
            @DrawableRes final int imagePlaceholder,
            @StyleRes final int fileBrowserDialogStyleResId,
            final boolean showConsultSearching,
            final boolean scrollChatToEndIfUserTyping
    ) {

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

        this.mediaAndFilesScreenBackgroundColor = mediaAndFilesScreenBackgroundColor;
        this.mediaAndFilesTextColor = mediaAndFilesTextColor;

        this.iconsAndSeparatorsColor = iconsAndSeparatorsColor;
        this.fileBrowserDialogStyleResId = fileBrowserDialogStyleResId;

        this.showConsultSearching = showConsultSearching;

        this.scrollChatToEndIfUserTyping = scrollChatToEndIfUserTyping;

        return this;
    }


    /**
     * Default values:
     *
     * @param chatMessageInputHintTextColor - R.color.threads_input_hint
     * @param chatMessageInputColor         - R.color.threads_input_background
     * @param inputTextColor                - R.color.threads_input_text
     * @param inputTextFont                 - setInputTextFont(String path)
     * @param attachmentIconResId          - R.drawable.threads_ic_attachment_button
     * @param sendMessageIconResId          - R.drawable.threads_ic_send_button
     * @param inputHint                     - R.string.threads_input_hint
     * @param inputHeight                   - R.dimen.threads_input_height
     * @param inputBackground               - R.drawable.threads_chat_input_background
     */
    public ChatStyle setChatInputStyle(
            @ColorRes final int chatMessageInputHintTextColor,
            @ColorRes final int chatMessageInputColor,
            @ColorRes final int inputTextColor,
            final String inputTextFont,
            @DrawableRes final int attachmentIconResId,
            @DrawableRes final int sendMessageIconResId,
            @StringRes final int inputHint,
            @DimenRes final int inputHeight,
            @DrawableRes final int inputBackground) {
        this.chatMessageInputColor = chatMessageInputColor;
        this.chatMessageInputHintTextColor = chatMessageInputHintTextColor;
        this.inputTextColor = inputTextColor;
        this.inputTextFont = inputTextFont;
        this.attachmentIconResId = attachmentIconResId;
        this.sendMessageIconResId = sendMessageIconResId;
        this.inputHint = inputHint;
        this.inputHeight = inputHeight;
        this.inputBackground = inputBackground;
        return this;
    }

    /**
     * Default values:
     *
     * @param defPushIconResId                             - R.drawable.default_push_icon
     * @param defTitleResId                                - R.string.threads_push_title
     * @param pushBackgroundColorResId                     - R.color.threads_push_background
     * @param nougatPushAccentColorResId                   - R.color.threads_nougat_push_accent
     * @param notificationQuickReplyMessageBackgroundColor - R.color.threads_quick_reply_message_background
     * @param notificationQuickReplyMessageTextColor       - R.color.threads_quick_reply_message_text_color
     * @return Builder
     */
    public ChatStyle setPushNotificationStyle(@DrawableRes final int defPushIconResId,
                                              @StringRes final int defTitleResId,
                                              @ColorRes final int pushBackgroundColorResId,
                                              @ColorRes final int nougatPushAccentColorResId,
                                              @ColorRes int notificationQuickReplyMessageBackgroundColor,
                                              @ColorRes int notificationQuickReplyMessageTextColor) {

        this.defPushIconResId = defPushIconResId;
        this.defTitleResId = defTitleResId;
        this.pushBackgroundColorResId = pushBackgroundColorResId;
        this.nougatPushAccentColorResId = nougatPushAccentColorResId;
        this.notificationQuickReplyMessageBackgroundColor = notificationQuickReplyMessageBackgroundColor;
        this.notificationQuickReplyMessageTextColor = notificationQuickReplyMessageTextColor;
        return this;
    }

    //images history screen


    /**
     * Default values:
     *
     * @param imagesScreenBackgroundColor - R.color.threads_attachments_background
     * @param imagesScreenAuthorTextColor - R.color.threads_attachments_author_text_color
     * @param imagesScreenDateTextColor   - R.color.threads_attachments_date_text_color
     * @param imagesScreenAuthorTextSize  - R.dimen.threads_attachments_author_text_size
     * @param imagesScreenDateTextSize    - R.dimen.threads_attachments_date_text_size
     */
    public ChatStyle setImagesGalleryStyle(@ColorRes int imagesScreenBackgroundColor,
                                           @ColorRes int imagesScreenAuthorTextColor,
                                           @ColorRes int imagesScreenDateTextColor,
                                           @DimenRes int imagesScreenAuthorTextSize,
                                           @DimenRes int imagesScreenDateTextSize) {
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
     * @param welcomeScreenLogoWidth              - R.dimen.threads_welcome_logo_width
     * @param welcomeScreenLogoHeight             - R.dimen.threads_welcome_logo_height
     * @return Builder
     */
    public ChatStyle setWelcomeScreenStyle(
            @DrawableRes final int welcomeScreenLogoResId,
            @StringRes final int welcomeScreenTitleTextResId,
            @StringRes final int welcomeScreenSubtitleTextResId,
            @ColorRes final int welcomeScreenTitleTextColorResId,
            @ColorRes final int welcomeScreenSubtitleTextColorResId,
            @DimenRes final int welcomeScreenTitleSizeInSp,
            @DimenRes final int welcomeScreenSubtitleSizeInSp,
            @DimenRes final int welcomeScreenLogoWidth,
            @DimenRes final int welcomeScreenLogoHeight) {
        this.welcomeScreenLogoResId = welcomeScreenLogoResId;
        this.welcomeScreenTitleTextColorResId = welcomeScreenTitleTextColorResId;
        this.welcomeScreenSubtitleTextColorResId = welcomeScreenSubtitleTextColorResId;
        this.welcomeScreenTitleTextResId = welcomeScreenTitleTextResId;
        this.welcomeScreenSubtitleTextResId = welcomeScreenSubtitleTextResId;
        this.welcomeScreenTitleSizeInSp = welcomeScreenTitleSizeInSp;
        this.welcomeScreenSubtitleSizeInSp = welcomeScreenSubtitleSizeInSp;
        this.welcomeScreenLogoWidth = welcomeScreenLogoWidth;
        this.welcomeScreenLogoHeight = welcomeScreenLogoHeight;
        return this;
    }

    // https://github.com/yydcdut/RxMarkdown
    public ChatStyle setIncomingMarkdownConfiguration(MarkdownConfiguration incoming) {
        this.incomingMarkdownConfiguration = incoming;
        return this;
    }

    /**
     * Default values:
     *
     * @param systemMessageFont             - setSystemMessageFont(String path)
     * @param systemMessageTextSize         - R.dimen.threads_system_message_text_size
     * @param systemMessageTextColorResId   - R.color.threads_chat_new_system_message
     * @param systemMessageLeftRightPadding - R.dimen.threads_system_message_left_right_padding
     * @param systemMessageTextGravity      - Gravity.CENTER
     * @param systemMessageLinkColor        - R.color.threads_system_message_link
     */
    public ChatStyle setSystemMessageStyle(
            final String systemMessageFont,
            @DimenRes final int systemMessageTextSize,
            @ColorRes final int systemMessageTextColorResId,
            @DimenRes final int systemMessageLeftRightPadding,
            final int systemMessageTextGravity,
            @ColorRes final int systemMessageLinkColor
    ) {
        this.systemMessageFont = systemMessageFont;
        this.systemMessageTextSize = systemMessageTextSize;
        this.systemMessageTextColorResId = systemMessageTextColorResId;
        this.systemMessageLeftRightPadding = systemMessageLeftRightPadding;
        this.systemMessageTextGravity = systemMessageTextGravity;
        this.systemMessageLinkColor = systemMessageLinkColor;
        return this;
    }

    /**
     * Default values:
     *
     * @param scrollDownIconResId          - R.drawable.threads_scroll_down_icon
     * @param scrollDownBackgroundResId    - R.drawable.threads_scroll_down_background
     * @param scrollDownButtonWidth        - R.dimen.threads_scroll_down_button_width
     * @param scrollDownButtonHeight       - R.dimen.threads_scroll_down_button_height
     * @param scrollDownButtonMargin       - R.dimen.threads_scroll_down_button_margin
     * @param scrollDownButtonElevation    - R.dimen.threads_scroll_down_button_elevation
     * @param unreadMsgStickerColorResId   - R.color.threads_chat_unread_msg_sticker_background
     * @param unreadMsgCountTextColorResId - R.color.threads_chat_unread_msg_count_text
     * @return Builder
     */
    public ChatStyle setScrollDownButtonStyle(
            @DrawableRes final int scrollDownIconResId,
            @DrawableRes final int scrollDownBackgroundResId,
            @DimenRes final int scrollDownButtonWidth,
            @DimenRes final int scrollDownButtonHeight,
            @DimenRes final int scrollDownButtonMargin,
            @DimenRes final int scrollDownButtonElevation,
            @ColorRes final int unreadMsgStickerColorResId,
            @ColorRes final int unreadMsgCountTextColorResId
    ) {
        this.scrollDownIconResId = scrollDownIconResId;
        this.scrollDownBackgroundResId = scrollDownBackgroundResId;
        this.scrollDownButtonWidth = scrollDownButtonWidth;
        this.scrollDownButtonHeight = scrollDownButtonHeight;
        this.scrollDownButtonMargin = scrollDownButtonMargin;
        this.scrollDownButtonElevation = scrollDownButtonElevation;
        this.unreadMsgStickerColorResId = unreadMsgStickerColorResId;
        this.unreadMsgCountTextColorResId = unreadMsgCountTextColorResId;
        return this;
    }

    /**
     * Default values:
     *
     * @param threadsSwipeRefreshColors - R.array.threads_swipe_refresh_colors
     * @return Builder
     */
    public ChatStyle setSwipeRefreshColors(@ArrayRes final int threadsSwipeRefreshColors) {
        this.threadsSwipeRefreshColors = threadsSwipeRefreshColors;
        return this;
    }

    /**
     * Default values:
     *
     * @param threadsRecordButtonBackground      - R.drawable.threads_record_button_background
     * @param threadsRecordButtonBackgroundColor - R.color.threads_record_button_background
     * @param threadsRecordButtonIcon            - R.drawable.threads_record_button_icon
     * @param threadsRecordButtonIconColor       - R.color.threads_record_button_icon
     * @param threadsRecordButtonSmallMicColor   - R.color.threads_record_button_small_mic
     * @return Builder
     */
    public ChatStyle setRecordButtonStyle(
            @DrawableRes final int threadsRecordButtonBackground,
            @ColorRes final int threadsRecordButtonBackgroundColor,
            @DrawableRes final int threadsRecordButtonIcon,
            @ColorRes final int threadsRecordButtonIconColor,
            @ColorRes final int threadsRecordButtonSmallMicColor
    ) {
        this.threadsRecordButtonBackground = threadsRecordButtonBackground;
        this.threadsRecordButtonBackgroundColor = threadsRecordButtonBackgroundColor;
        this.threadsRecordButtonIcon = threadsRecordButtonIcon;
        this.threadsRecordButtonIconColor = threadsRecordButtonIconColor;
        this.threadsRecordButtonSmallMicColor = threadsRecordButtonSmallMicColor;
        return this;
    }

    /**
     * Default values:
     *
     * @param incomingPlayPauseButtonColor - R.color.threads_incoming_play_pause_button
     * @param outgoingPlayPauseButtonColor - R.color.threads_outgoing_play_pause_button
     * @param previewPlayPauseButtonColor  - R.color.threads_preview_play_pause_button
     * @param voiceMessagePlayButton       - R.drawable.threads_voice_message_play
     * @param voiceMessagePauseButton      - R.drawable.threads_voice_message_pause
     * @return Builder
     */
    public ChatStyle setPlayPauseButtonStyle(
            @ColorRes final int incomingPlayPauseButtonColor,
            @ColorRes final int outgoingPlayPauseButtonColor,
            @ColorRes final int previewPlayPauseButtonColor,
            @DrawableRes final int voiceMessagePlayButton,
            @DrawableRes final int voiceMessagePauseButton
    ) {
        this.incomingPlayPauseButtonColor = incomingPlayPauseButtonColor;
        this.outgoingPlayPauseButtonColor = outgoingPlayPauseButtonColor;
        this.previewPlayPauseButtonColor = previewPlayPauseButtonColor;
        this.voiceMessagePlayButton = voiceMessagePlayButton;
        this.voiceMessagePauseButton = voiceMessagePauseButton;
        return this;
    }

    /**
     * Default values:
     *
     * @param emptyStateBackgroundColorResId  - R.color.threads_empty_state_background
     * @param emptyStateProgressBarColorResId - R.color.threads_empty_state_progress
     * @param emptyStateHintColorResId        - R.color.threads_empty_state_hint
     */
    public ChatStyle setEmptyStateStyle(
            @ColorRes final int emptyStateBackgroundColorResId,
            @ColorRes final int emptyStateProgressBarColorResId,
            @ColorRes final int emptyStateHintColorResId
    ) {
        this.emptyStateBackgroundColorResId = emptyStateBackgroundColorResId;
        this.emptyStateProgressBarColorResId = emptyStateProgressBarColorResId;
        this.emptyStateHintColorResId = emptyStateHintColorResId;
        return this;
    }

    /**
     * Default values:
     *
     * @param quickReplyButtonBackground - R.drawable.threads_quick_reply_button_background
     * @param quickReplyTextColor        - R.color.threads_quick_reply_text_color
     */
    public ChatStyle setQuickReplyChipChoiceStyle(@DrawableRes final int quickReplyButtonBackground, @ColorRes final int quickReplyTextColor) {
        this.quickReplyButtonBackground = quickReplyButtonBackground;
        this.quickReplyTextColor = quickReplyTextColor;
        return this;
    }

    /**
     * Default values:
     *
     * @param consultSearchingProgressColor - R.color.threads_consult_searching_progress_color
     * @return Builder
     */
    public ChatStyle setConsultSearchingProgressColor(@ColorRes final int consultSearchingProgressColor) {
        this.consultSearchingProgressColor = consultSearchingProgressColor;
        return this;
    }

    /**
     * Default values:
     *
     * @param maxGalleryImagesCount - R.integer.max_count_attached_images
     * @return Builder
     */
    public ChatStyle setMaxGalleryImagesCount(@IntegerRes final int maxGalleryImagesCount) {
        this.maxGalleryImagesCount = maxGalleryImagesCount;
        return this;
    }

    public ChatStyle setOutgoingPadding(
            @DimenRes int left,
            @DimenRes int top,
            @DimenRes int right,
            @DimenRes int bottom
    ) {
        this.bubbleOutgoingPaddingLeft = left;
        this.bubbleOutgoingPaddingTop = top;
        this.bubbleOutgoingPaddingRight = right;
        this.bubbleOutgoingPaddingBottom = bottom;
        return this;
    }

    public ChatStyle setIngoingPadding(
            @DimenRes int left,
            @DimenRes int top,
            @DimenRes int right,
            @DimenRes int bottom
    ) {
        this.bubbleIncomingPaddingLeft = left;
        this.bubbleIncomingPaddingTop = top;
        this.bubbleIncomingPaddingRight = right;
        this.bubbleIncomingPaddingBottom = bottom;
        return this;
    }

    /**
     * Определяет максимальное количество приложенных к сообщению файлов
     *
     * @param context
     * @return Максимальное количество приложенных к сообщению файлов
     */
    public int getMaxGalleryImagesCount(@NonNull Context context) {
        int count = context.getResources().getInteger(maxGalleryImagesCount);
        int maxCount = context.getResources().getInteger(maxGalleryImagesCountFixedBySystem);
        if (count <= maxCount && count > 0)
            return count;
        return maxCount;
    }
}