package im.threads.ui;

import android.content.Context;
import android.content.res.ColorStateList;
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

import java.io.Serializable;

import im.threads.R;
import im.threads.business.config.BaseConfig;
import im.threads.business.markdown.MarkdownConfig;

/**
 * Стиль чата.
 * Хранит в себе информацию о расцветках, иконках и других настройках чата.
 */
public final class ChatStyle implements Serializable {
    /**
     * Определяет доступность предпросмотра для ссылок (OpenGraph). По умолчанию отключено.
     */
    public boolean linkPreviewEnabled = false;

    public boolean arePermissionDescriptionDialogsEnabled = false;

    public boolean isClearSearchBtnVisible = true;

    public boolean isSearchLoaderVisible = true;

    @DrawableRes
    public Integer searchLoaderDrawable = null;

    @ColorRes
    public int searchLoaderColorTint = R.color.ecc_white;

    @DrawableRes
    public int searchIconResId = R.drawable.ecc_ic_search_white_24dp;

    @ColorRes
    public int searchResultsDividerColor = R.color.ecc_search_divider_color;

    @DrawableRes
    public int searchResultsItemRightArrowDrawable = R.drawable.right_arrow;

    @ColorRes
    public int searchResultsItemRightArrowTintColor = R.color.ecc_search_results_item_secondary;

    @ColorRes
    public int searchResultsItemDateTextColor = R.color.ecc_search_results_item_secondary;

    @ColorRes
    public int searchResultsItemMessageTextColor = R.color.ecc_search_results_message_color;

    @ColorRes
    public int searchResultsItemNameTextColor = R.color.ecc_black;

    @DrawableRes
    public int searchResultNoItemsImageDrawable = R.drawable.ecc_search_not_found;

    @ColorRes
    public int searchResultNoItemsTextColor = R.color.ecc_black;

    @StringRes
    public int searchResultNoItemsText = R.string.ecc_no_results_found;

    @ColorRes
    public int searchBarTextColor = R.color.ecc_white;

    @StringRes
    public int searchMessageHintText = R.string.ecc_search_messages;

    @StringRes
    public int loadedSettingsErrorText = R.string.ecc_settings_not_loaded;

    @StringRes
    public int loadedAttachmentSettingsErrorText = R.string.ecc_attachment_settings_not_loaded;

    @ColorRes
    public int chatToolbarHintTextColor = R.color.ecc_chat_toolbar_hint;

    //common styles
    @ColorRes
    public int chatDisabledTextColor = R.color.ecc_disabled_text_color;

    // chat title style
    @StringRes
    public int chatTitleTextResId = R.string.ecc_contact_center;
    @StringRes
    public int chatSubtitleTextResId = R.string.ecc_operator_subtitle;
    @ColorRes
    public int chatToolbarColorResId = R.color.ecc_chat_toolbar;
    @ColorRes
    public int chatToolbarContextMenuColorResId = R.color.ecc_chat_toolbar_context_menu;
    @ColorRes
    public int chatStatusBarColorResId = R.color.ecc_chat_status_bar;
    @BoolRes
    public int windowLightStatusBarResId = R.bool.ecc_chat_is_light_status_bar;
    @ColorRes
    public int menuItemTextColorResId = R.color.ecc_chat_toolbar_menu_item;
    @ColorRes
    public int chatToolbarTextColorResId = R.color.ecc_chat_toolbar_text;
    @BoolRes
    public int fixedChatTitle = R.bool.ecc_chat_fixed_chat_title;
    @BoolRes
    public int fixedChatSubtitle = R.bool.ecc_chat_fixed_chat_subtitle;
    @BoolRes
    public int isChatSubtitleVisible = R.bool.ecc_chat_subtitle_is_visible;
    @BoolRes
    public int isChatTitleShadowVisible = R.bool.ecc_chat_title_shadow_is_visible;
    @BoolRes
    public int searchEnabled = R.bool.ecc_chat_search_enabled;

    public boolean showBackButton = false;
    public boolean isToolbarTextCentered = false;
    public boolean chatSubtitleShowOrgUnit = false;

    @ColorRes
    public int chatToolbarInverseIconTintResId = R.color.ecc_green_83b144;
    @DrawableRes
    public int chatToolbarBackIconResId = R.drawable.ecc_ic_arrow_back_24dp;
    @DrawableRes
    public int chatToolbarPopUpMenuIconResId = R.drawable.ecc_ic_more_vert_24dp;
    @DrawableRes
    public int chatToolbarContentCopyIconResId = R.drawable.ecc_ic_content_copy_24dp;
    @DrawableRes
    public int chatToolbarReplyIconResId = R.drawable.ecc_ic_reply_24dp;

    // chat body style
    @ColorRes
    public int chatBackgroundColor = R.color.ecc_chat_background;
    @ColorRes
    public int chatHighlightingColor = R.color.ecc_chat_highlighting;
    @ColorRes
    public int incomingMessageBubbleColor = R.color.ecc_chat_incoming_message_bubble;
    @ColorRes
    public int outgoingMessageBubbleColor = R.color.ecc_chat_outgoing_message_bubble;
    @ColorRes
    public int messageNotSentBubbleBackgroundColor = R.color.ecc_error_red_df0000;

    @ColorRes
    public int messageNotSentErrorImageColor = R.color.ecc_white;
    @DrawableRes
    public int incomingMessageBubbleBackground = R.drawable.ecc_incoming_bubble;
    @DrawableRes
    public int outgoingMessageBubbleBackground = R.drawable.ecc_outgoing_bubble;
    @ColorRes
    public int incomingMessageTextColor = R.color.ecc_incoming_message_text;
    @ColorRes
    public int outgoingMessageTextColor = R.color.ecc_outgoing_message_text;

    @DrawableRes
    public int quoteBackgroundResId = R.drawable.ecc_quote_background;
    @ColorRes
    public int quoteIconColorRes = R.color.ecc_white;
    @ColorRes
    public int quoteOutgoingBackgroundColorRes = R.color.ecc_quote_bg_color;
    @ColorRes
    public int quoteOutgoingDelimiterColorRes = R.color.ecc_quote_delimiter_color;
    @ColorRes
    public int quoteIncomingBackgroundColorRes = R.color.ecc_quote_bg_color;
    @ColorRes
    public int quoteIncomingDelimiterColorRes = R.color.ecc_quote_delimiter_color;
    @ColorRes
    public int quoteIncomingAuthorTextColorRes = R.color.ecc_quote_author_color_text;
    @ColorRes
    public int quoteIncomingTextColorRes = R.color.ecc_quote_color_text;
    @ColorRes
    public int quoteOutgoingAuthorTextColorRes = R.color.ecc_quote_author_color_text;
    @ColorRes
    public int quoteOutgoingTextColorRes = R.color.ecc_quote_color_text;


    @ColorRes
    public int errorMessageTextColor = R.color.ecc_error_red_df0000;

    @ColorRes
    public int quoteHeaderTextColor = R.color.ecc_incoming_message_text;
    @ColorRes
    public int quoteTextTextColor = R.color.ecc_incoming_message_text;

    @ColorRes
    public int incomingMessageTimeColor = R.color.ecc_operator_message_timestamp;
    @ColorRes
    public int outgoingMessageTimeColor = R.color.ecc_user_message_timestamp;

    @ColorRes
    public int incomingMessageLoaderColor = R.color.ecc_green_83b144;
    @ColorRes
    public int outgoingMessageLoaderColor = R.color.ecc_teal_004d40;
    
    @DimenRes
    public int incomingMessageTimeTextSize = 0;
    @DimenRes
    public int outgoingMessageTimeTextSize = 0;

    @ColorRes
    public int incomingDelimitersColor = R.color.ecc_chat_toolbar;
    @ColorRes
    public int outgoingDelimitersColor = R.color.ecc_outgoing_message_text;

    @DrawableRes
    public int outgoingImageBubbleMask = R.drawable.ecc_outgoing_image_mask;
    @ColorRes
    public int outgoingImageTimeColor = R.color.ecc_outgoing_message_time;
    @ColorRes
    public int outgoingImageTimeBackgroundColor = R.color.ecc_outgoing_time_underlay;
    @DrawableRes
    public int incomingImageBubbleMask = R.drawable.ecc_incoming_image_mask;
    @ColorRes
    public int incomingImageTimeColor = R.color.ecc_incoming_message_time;
    @ColorRes
    public int incomingImageTimeBackgroundColor = R.color.ecc_incoming_time_underlay;

    @DimenRes
    public int bubbleOutgoingPaddingLeft = R.dimen.ecc_bubbleOutgoingPaddingLeft;
    @DimenRes
    public int bubbleOutgoingPaddingTop = R.dimen.ecc_bubbleOutgoingPaddingTop;
    @DimenRes
    public int bubbleOutgoingPaddingRight = R.dimen.ecc_bubbleOutgoingPaddingRight;
    @DimenRes
    public int bubbleOutgoingPaddingBottom = R.dimen.ecc_bubbleOutgoingPaddingBottom;

    @DimenRes
    public int bubbleIncomingPaddingLeft = R.dimen.ecc_bubbleIncomingPaddingLeft;
    @DimenRes
    public int bubbleIncomingPaddingTop = R.dimen.ecc_bubbleIncomingPaddingTop;
    @DimenRes
    public int bubbleIncomingPaddingRight = R.dimen.ecc_bubbleIncomingPaddingRight;
    @DimenRes
    public int bubbleIncomingPaddingBottom = R.dimen.ecc_bubbleIncomingPaddingBottom;

    @DimenRes
    public int bubbleOutgoingMarginLeft = R.dimen.ecc_user_margin_left;
    @DimenRes
    public int bubbleOutgoingMarginTop = R.dimen.ecc_margin_quarter;
    @DimenRes
    public int bubbleOutgoingMarginRight = R.dimen.ecc_user_margin_right;
    @DimenRes
    public int bubbleOutgoingMarginBottom = R.dimen.ecc_margin_quarter;

    @DimenRes
    public int bubbleIncomingMarginLeft = R.dimen.ecc_margin_quarter;
    @DimenRes
    public int bubbleIncomingMarginTop = R.dimen.ecc_margin_quarter;
    @DimenRes
    public int bubbleIncomingMarginRight = R.dimen.ecc_consultant_margin_right;
    @DimenRes
    public int bubbleIncomingMarginBottom = R.dimen.ecc_margin_quarter;

    @DimenRes
    public int incomingImageLeftBorderSize = R.dimen.ecc_incomingImageLeftBorderSize;
    @DimenRes
    public int incomingImageTopBorderSize = R.dimen.ecc_incomingImageTopBorderSize;
    @DimenRes
    public int incomingImageRightBorderSize = R.dimen.ecc_incomingImageRightBorderSize;
    @DimenRes
    public int incomingImageBottomBorderSize = R.dimen.ecc_incomingImageBottomBorderSize;
    @DimenRes
    public int outgoingImageLeftBorderSize = R.dimen.ecc_outgoingImageLeftBorderSize;
    @DimenRes
    public int outgoingImageTopBorderSize = R.dimen.ecc_outgoingImageTopBorderSize;
    @DimenRes
    public int outgoingImageRightBorderSize = R.dimen.ecc_outgoingImageRightBorderSize;
    @DimenRes
    public int outgoingImageBottomBorderSize = R.dimen.ecc_outgoingImageBottomBorderSize;

    public float imageBubbleSize = 0.66f;

    @DimenRes
    public int inputFieldPaddingLeft = R.dimen.ecc_margin_three_fourth;
    @DimenRes
    public int inputFieldPaddingTop = R.dimen.ecc_margin_quarter;
    @DimenRes
    public int inputFieldPaddingRight = R.dimen.ecc_margin_three_fourth;
    @DimenRes
    public int inputFieldPaddingBottom = R.dimen.ecc_margin_quarter;

    @DimenRes
    public int inputFieldMarginLeft = R.dimen.ecc_margin_zero;
    @DimenRes
    public int inputFieldMarginTop = R.dimen.ecc_margin_zero;
    @DimenRes
    public int inputFieldMarginRight = R.dimen.ecc_margin_zero;
    @DimenRes
    public int inputFieldMarginBottom = R.dimen.ecc_margin_zero;

    @ColorRes
    public int incomingMessageLinkColor = R.color.ecc_incoming_message_link;
    @ColorRes
    public int outgoingMessageLinkColor = R.color.ecc_outgoing_message_link;

    @ColorRes
    public int incomingPlayPauseButtonColor = R.color.ecc_incoming_play_pause_button;
    @ColorRes
    public int outgoingPlayPauseButtonColor = R.color.ecc_outgoing_play_pause_button;
    @ColorRes
    public int previewPlayPauseButtonColor = R.color.ecc_preview_play_pause_button;
    @DrawableRes
    public int voiceMessagePlayButton = R.drawable.ecc_voice_message_play;
    @DrawableRes
    public int voiceMessagePauseButton = R.drawable.ecc_voice_message_pause;

    @ColorRes
    public int chatBodyIconsTint = R.color.ecc_chat_icons_tint;

    @ColorRes
    public int searchClearIconTintColor = R.color.ecc_white;

    @DrawableRes
    public int searchClearIconDrawable = R.drawable.ecc_ic_clear_36dp;

    public int[] chatBodyIconsColorState = null;

    @ColorRes
    public int chatSystemMessageTextColor = R.color.ecc_chat_connection_message;

    @ColorRes
    public int iconsAndSeparatorsColor = R.color.ecc_icon_and_separators_color;
    @DrawableRes
    public int defaultOperatorAvatar = R.drawable.ecc_operator_avatar_placeholder;
    @DimenRes
    public int operatorAvatarSize = R.dimen.ecc_operator_photo_size;
    @DimenRes
    public int operatorSystemAvatarSize = R.dimen.ecc_system_operator_photo_size;
    @DrawableRes
    public int imagePlaceholder = R.drawable.ecc_image_placeholder;
    @StyleRes
    public int fileBrowserDialogStyleResId = R.style.FileDialogStyleTransparent;
    public boolean showConsultSearching = false;
    public boolean scrollChatToEndIfUserTyping = false;
    public boolean inputEnabledDuringQuickReplies = false;
    @DrawableRes
    public int scrollDownIconResId = R.drawable.ecc_scroll_down_icon;
    @DrawableRes
    public int scrollDownBackgroundResId = R.drawable.ecc_scroll_down_background;
    @DimenRes
    public int scrollDownButtonWidth = R.dimen.ecc_scroll_down_button_width;
    @DimenRes
    public int scrollDownButtonHeight = R.dimen.ecc_scroll_down_button_height;
    @DimenRes
    public int scrollDownButtonMargin = R.dimen.ecc_scroll_down_button_margin;
    @DimenRes
    public int scrollDownButtonElevation = R.dimen.ecc_scroll_down_button_elevation;
    @ColorRes
    public int unreadMsgStickerColorResId = R.color.ecc_chat_unread_msg_sticker_background;
    @ColorRes
    public int unreadMsgCountTextColorResId = R.color.ecc_chat_unread_msg_count_text;
    @ArrayRes
    public int threadsSwipeRefreshColors = R.array.ecc_swipe_refresh_colors;

    @DrawableRes
    public int threadsRecordButtonBackground = R.drawable.ecc_record_button_background;
    @ColorRes
    public int threadsRecordButtonBackgroundColor = R.color.ecc_record_button_background;
    @DrawableRes
    public int threadsRecordButtonIcon = R.drawable.ecc_record_button_icon;
    @ColorRes
    public int threadsRecordButtonIconColor = R.color.ecc_record_button_icon;
    @ColorRes
    public int threadsRecordButtonSmallMicColor = R.color.ecc_record_button_small_mic;

    //download button
    @ColorRes
    public int downloadButtonTintResId = R.color.ecc_green_83b144;
    @ColorRes
    public int downloadButtonBackgroundTintResId = R.color.ecc_white;

    @ColorRes
    public int loaderTintResId = R.color.ecc_green_83b144;
    @DrawableRes
    public int startDownloadIconResId = R.drawable.ecc_ic_vertical_align_bottom_18dp;
    @DrawableRes
    public int inProgressIconResId = R.drawable.ecc_ic_clear_36dp;
    @DrawableRes
    public int completedIconResId = R.drawable.ecc_ic_file_outline_24dp;

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
    public int imagesScreenBackgroundColor = R.color.ecc_attachments_background;
    @ColorRes
    public int imagesScreenAuthorTextColor = R.color.ecc_attachments_author_text_color;
    @ColorRes
    public int imagesScreenDateTextColor = R.color.ecc_attachments_date_text_color;
    @DimenRes
    public int imagesScreenAuthorTextSize = R.dimen.ecc_attachments_author_text_size;
    @DimenRes
    public int imagesScreenDateTextSize = R.dimen.ecc_attachments_date_text_size;

    // quote style
    @ColorRes
    public int quoteClearIconTintResId = R.color.ecc_green_83b144;
    @DrawableRes
    public int quoteClearIconResId = R.drawable.ecc_ic_clear_36dp;

    // chat input style
    @ColorRes
    public int chatMessageInputColor = R.color.ecc_input_background;
    @ColorRes
    public int chatMessageInputHintTextColor = R.color.ecc_input_hint;
    @ColorRes
    public int inputTextColor = R.color.ecc_input_text;
    @Nullable
    public String inputTextFont;
    @ColorRes
    public int inputIconTintResId = R.color.ecc_green_83b144;
    @DrawableRes
    public int attachmentIconResId = R.drawable.ecc_ic_attachment_button;
    @DrawableRes
    public int sendMessageIconResId = R.drawable.ecc_ic_send_button;
    @StringRes
    public int inputHint = R.string.ecc_input_hint;
    @DimenRes
    public int inputHeight = R.dimen.ecc_input_height;
    @DrawableRes
    public int inputBackground = R.drawable.ecc_chat_input_background;
    @DrawableRes
    public int quoteAttachmentIconResId = R.drawable.ecc_ic_reply_gray_24dp;

    //attachment bottom sheet style
    @ColorRes
    public int attachmentBottomSheetButtonTintResId = R.color.ecc_green_83b144;
    @DrawableRes
    public int attachmentDoneIconResId = R.drawable.ecc_ic_circle_done_36dp;
    @DrawableRes
    public int attachmentCameraIconResId = R.drawable.ecc_ic_camera_42dp;
    @DrawableRes
    public int attachmentGalleryIconResId = R.drawable.ecc_ic_photo_42dp;
    @DrawableRes
    public int attachmentFileIconResId = R.drawable.ecc_ic_file_fill_42dp;
    @DrawableRes
    public int attachmentSendIconResId = R.drawable.ecc_ic_send_42dp;

    // push notification style
    @DrawableRes
    public int defPushIconResId = R.drawable.ecc_default_push_icon;
    @StringRes
    public int defTitleResId = R.string.ecc_push_title;
    @ColorRes
    public int pushBackgroundColorResId = R.color.ecc_push_background;
    @ColorRes
    public int nougatPushAccentColorResId = R.color.ecc_nougat_push_accent;
    @ColorRes
    public int notificationQuickReplyMessageBackgroundColor = R.color.ecc_notification_quick_reply_message_background;
    @ColorRes
    public int notificationQuickReplyMessageTextColor = R.color.ecc_notification_quick_reply_message_text_color;

    // resolve thread request style
    @StringRes
    public int requestToResolveThreadTextResId = R.string.ecc_request_to_resolve_thread;
    @StringRes
    public int approveRequestToResolveThreadTextResId = R.string.ecc_request_to_resolve_thread_close;
    @StringRes
    public int denyRequestToResolveThreadTextResId = R.string.ecc_request_to_resolve_thread_open;

    // survey style
    @DrawableRes
    public int binarySurveyLikeUnselectedIconResId = R.drawable.ecc_binary_survey_like_unselected;
    @DrawableRes
    public int binarySurveyLikeSelectedIconResId = R.drawable.ecc_binary_survey_like_selected;
    @DrawableRes
    public int binarySurveyDislikeUnselectedIconResId = R.drawable.ecc_binary_survey_dislike_unselected;
    @DrawableRes
    public int binarySurveyDislikeSelectedIconResId = R.drawable.ecc_binary_survey_dislike_selected;
    @DrawableRes
    public int optionsSurveyUnselectedIconResId = R.drawable.ecc_options_survey_unselected;
    @DrawableRes
    public int optionsSurveySelectedIconResId = R.drawable.ecc_options_survey_selected;
    @ColorRes
    public int surveySelectedColorFilterResId = R.color.ecc_survey_selected_icon_tint;
    @ColorRes
    public int surveyFinalColorFilterResId = R.color.ecc_outgoing_message_text;
    @ColorRes
    public int surveyUnselectedColorFilterResId = R.color.ecc_survey_unselected_icon_tint;
    @ColorRes
    public int surveyTextColorResId = R.color.ecc_chat_system_message;
    @ColorRes
    public int surveyChoicesTextColorResId = R.color.ecc_survey_choices_text;

    // schedule message style
    @ColorRes
    public int scheduleMessageTextColorResId = R.color.ecc_schedule_text;
    @DrawableRes
    public int scheduleMessageIconResId = R.drawable.ecc_schedule_icon;

    // welcome screen style
    @DrawableRes
    public int welcomeScreenLogoResId = R.drawable.ecc_welcome_logo;
    @StringRes
    public int welcomeScreenTitleTextResId = R.string.ecc_welcome_screen_title_text;
    @StringRes
    public int welcomeScreenSubtitleTextResId = R.string.ecc_welcome_screen_subtitle_text;
    @ColorRes
    public int welcomeScreenTitleTextColorResId = R.color.ecc_welcome_screen_title;
    @ColorRes
    public int welcomeScreenSubtitleTextColorResId = R.color.ecc_welcome_screen_subtitle;
    @DimenRes
    public int welcomeScreenTitleSizeInSp = R.dimen.ecc_welcome_screen_title;
    @DimenRes
    public int welcomeScreenSubtitleSizeInSp = R.dimen.ecc_welcome_screen_subtitle;
    @DimenRes
    public int welcomeScreenLogoWidth = R.dimen.ecc_welcome_logo_width;
    @DimenRes
    public int welcomeScreenLogoHeight = R.dimen.ecc_welcome_logo_height;

    // empty state
    @ColorRes
    public int emptyStateBackgroundColorResId = R.color.ecc_empty_state_background;
    @ColorRes
    public int emptyStateProgressBarColorResId = R.color.ecc_empty_state_progress;
    @ColorRes
    public int emptyStateHintColorResId = R.color.ecc_empty_state_hint;
    @StringRes
    public int loaderTextResId = R.string.ecc_loading;

    // system messages
    @DimenRes
    public int systemMessageTextSize = R.dimen.ecc_system_message_text_size;
    @ColorRes
    public int systemMessageTextColorResId = R.color.ecc_chat_new_system_message;
    @DimenRes
    public int systemMessageLeftRightPadding = R.dimen.ecc_system_message_left_right_padding;
    public int systemMessageTextGravity = Gravity.CENTER;
    @ColorRes
    public int systemMessageLinkColor = R.color.ecc_system_message_link;
    @DrawableRes
    public int quickReplyButtonBackground = R.drawable.ecc_quick_reply_button_background;
    @ColorRes
    public int quickReplyTextColor = R.color.ecc_quick_reply_text_color;
    @IntegerRes
    public int maxGalleryImagesCount = R.integer.ecc_max_count_attached_images;
    @IntegerRes
    public int maxGalleryImagesCountFixedBySystem = R.integer.ecc_max_count_attached_images_final;
    @ColorRes
    public int consultSearchingProgressColor = R.color.ecc_consult_searching_progress_color;
    // set can show specialist info
    public boolean canShowSpecialistInfo = true;

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
    @DimenRes
    public int toastTextSize = 0;
    @ColorRes
    public int toastTextColor = 0;
    @ColorRes
    public int toastBackgroundColor = 0;
    // Конфигурации markdown в сообщениях
    @DrawableRes
    public int messageEditedIconResId = R.drawable.ecc_message_image_edited;
    @DrawableRes
    public int messageSendingIconResId = R.drawable.ecc_message_image_sending;
    @DrawableRes
    public int messageSentIconResId = R.drawable.ecc_message_image_sending;
    @DrawableRes
    public int messageDeliveredIconResId = R.drawable.ecc_message_image_delivered;
    @DrawableRes
    public int messageReadIconResId = R.drawable.ecc_image_message_read;
    @DrawableRes
    public int messageFailedIconResId = R.drawable.ecc_message_image_failed;
    @ColorRes
    public int messageEditedIconColorResId = R.color.ecc_user_message_timestamp;
    @ColorRes
    public int messageSendingIconColorResId = R.color.ecc_white;
    @ColorRes
    public int messageSentIconColorResId = R.color.ecc_white;
    @ColorRes
    public int messageDeliveredIconColorResId = R.color.ecc_white;
    @ColorRes
    public int messageReadIconColorResId = R.color.ecc_white;
    @ColorRes
    public int messageFailedIconColorResId = R.color.ecc_white;

    // Chat error screen
    @DrawableRes
    public int chatErrorScreenImageResId = R.drawable.ecc_serious_worker;

    @DimenRes
    public int chatErrorScreenMessageTextSizeResId = R.dimen.ecc_text_medium;

    @ColorRes
    public int chatErrorScreenMessageTextColorResId = R.color.ecc_chat_new_system_message;

    @DimenRes
    public int chatErrorScreenButtonTextSizeResId = R.dimen.ecc_text_medium;

    @StringRes
    public int chatErrorScreenButtonTextResId = R.string.ecc_repeat;

    @ColorRes
    public int chatErrorScreenButtonTextColorResId = R.color.ecc_white;

    public ColorStateList chatErrorScreenButtonTintColorList = null;

    private MarkdownConfig incomingMarkdownConfiguration, outgoingMarkdownConfiguration;

    public ChatStyle() {
    }

    public ChatStyle setChatErrorScreenStyle(
            @DrawableRes Integer imageResId,
            @DimenRes Integer messageTextSizeResId,
            @ColorRes Integer messageTextColorResId,
            @DimenRes Integer buttonTextSizeResId,
            @ColorRes Integer buttonTextColorResId,
            ColorStateList buttonTintColorStateList,
            @StringRes Integer buttonTextResId
    ) {
        if (imageResId != null) chatErrorScreenImageResId = imageResId;
        if (messageTextSizeResId != null) chatErrorScreenMessageTextSizeResId = messageTextSizeResId;
        if (messageTextColorResId != null) chatErrorScreenMessageTextColorResId = messageTextColorResId;
        if (buttonTextSizeResId != null) chatErrorScreenButtonTextSizeResId = buttonTextSizeResId;
        if (buttonTextResId != null) chatErrorScreenButtonTextResId = buttonTextResId;
        if (buttonTintColorStateList != null) chatErrorScreenButtonTintColorList = buttonTintColorStateList;
        if (buttonTextColorResId != null) chatErrorScreenButtonTextColorResId = buttonTextColorResId;

        return this;
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

    /**
     * Центрирует текст в тулбаре
     */
    public ChatStyle centerToolbarText() {
        this.isToolbarTextCentered = true;
        return this;
    }

    public ChatStyle setFixedChatTitle(@BoolRes final int fixedChatTitle) {
        this.fixedChatTitle = fixedChatTitle;
        return this;
    }

    public ChatStyle setVisibleChatSubtitle(@BoolRes final int isChatSubtitleVisible) {
        this.isChatSubtitleVisible = isChatSubtitleVisible;
        return this;
    }

    public ChatStyle setSearchEnabled(@BoolRes final int searchEnabled) {
        this.searchEnabled = searchEnabled;
        return this;
    }

    public ChatStyle setVisibleChatTitleShadow(@BoolRes final int isChatTitleShadowVisible) {
        this.isChatTitleShadowVisible = isChatTitleShadowVisible;
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
    // </editor-fold>

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
    // </editor-fold>

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
    // </editor-fold>

    public ChatStyle setAttachmentGalleryIconResId(@DrawableRes final int attachmentGalleryIconResId) {
        this.attachmentGalleryIconResId = attachmentGalleryIconResId;
        return this;
    }

    public ChatStyle setAttachmentFileIconResId(@DrawableRes final int attachmentFileIconResId) {
        this.attachmentFileIconResId = attachmentFileIconResId;
        return this;
    }

    public ChatStyle setAttachmentSendIconResId(@DrawableRes final int attachmentSendIconResId) {
        this.attachmentSendIconResId = attachmentSendIconResId;
        return this;
    }

    public ChatStyle setChatBodyIconsTint(@ColorRes final int chatBodyIconsTint) {
        this.chatBodyIconsTint = chatBodyIconsTint;
        return this;
    }

    public ChatStyle setChatBodyIconsColorStateTint(@ColorRes final int iconStateDisabledTint,
                                                    @ColorRes final int iconStateEnabledTint,
                                                    @ColorRes final int iconStatePressedTint) {
        this.chatBodyIconsColorState = new int[]{
                iconStateDisabledTint,
                iconStateEnabledTint,
                iconStatePressedTint
        };
        return this;
    }

    public ChatStyle setInputIconTintResId(@ColorRes final int inputIconTintResId) {
        this.inputIconTintResId = inputIconTintResId;
        return this;
    }

    public ChatStyle setQuoteAttachmentIconResId(@DrawableRes final int quoteAttachmentIconResId) {
        this.quoteAttachmentIconResId = quoteAttachmentIconResId;
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

    public ChatStyle setDownloadButtonBackgroundTintResId(@ColorRes final int downloadButtonBackgroundTintResId) {
        this.downloadButtonBackgroundTintResId = downloadButtonBackgroundTintResId;
        return this;
    }

    public ChatStyle setLoaderTintResId(@ColorRes final int loaderTintResId) {
        this.loaderTintResId = loaderTintResId;
        return this;
    }

    public ChatStyle setIncomingMessageLoaderColorResId(@ColorRes final int incomingMessageLoaderColor) {
        this.incomingMessageLoaderColor = incomingMessageLoaderColor;
        return this;
    }

    public ChatStyle setOutgoingMessageLoaderColorResId(@ColorRes final int outgoingMessageLoaderColor) {
        this.outgoingMessageLoaderColor = outgoingMessageLoaderColor;
        return this;
    }

    public ChatStyle setStartDownloadIconResId(@DrawableRes final int startDownloadIconResId) {
        this.startDownloadIconResId = startDownloadIconResId;
        return this;
    }

    // deprecated setters

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

    /**
     * @param color цвет входящего баббла
     */
    public ChatStyle setIncomingMessageBubbleColor(@ColorRes final int color) {
        this.incomingMessageBubbleColor = color;
        return this;
    }

    /**
     * @param color цвет исходящего баббла
     */
    public ChatStyle setOutgoingMessageBubbleColor(@ColorRes final int color) {
        this.outgoingMessageBubbleColor = color;
        return this;
    }

    /**
     * Default values:
     *
     * @param chatTitleTextResId               - R.string.threads_contact_center
     * @param chatSubtitleTextResId            - R.string.threads_operator_subtitle
     * @param chatToolbarColorResId            - R.color.threads_chat_toolbar
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
            final boolean showBackButton
    ) {
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
     * Устанавливает значение цвета для текста входящего сообщения
     * @param incomingMessageTextColor цвет для текста входящего сообщения
     */
    public ChatStyle setIncomingMessageTextColor(@ColorRes final int incomingMessageTextColor) {
        this.incomingMessageTextColor = incomingMessageTextColor;
        return this;
    }

    /**
     * Устанавливает значение цвета для текста исходящего сообщения
     * @param outgoingMessageTextColor цвет для текста исходящего сообщения
     */
    public ChatStyle setOutgoingMessageTextColor(@ColorRes final int outgoingMessageTextColor) {
        this.outgoingMessageTextColor = outgoingMessageTextColor;
        return this;
    }

    /**
     * Устанавливает значение цвета для текста времени входящего сообщения
     * @param incomingMessageTimeColor цвет для текста времени входящего сообщения
     */
    public ChatStyle setIncomingMessageTimeColor(@ColorRes final int incomingMessageTimeColor) {
        this.incomingMessageTimeColor = incomingMessageTimeColor;
        return this;
    }

    /**
     * Устанавливает значение цвета для текста времени исходящего сообщения
     * @param outgoingMessageTimeColor цвет для текста времени исходящего сообщения
     */
    public ChatStyle setOutgoingMessageTimeColor(@ColorRes final int outgoingMessageTimeColor) {
        this.outgoingMessageTimeColor = outgoingMessageTimeColor;
        return this;
    }

    /**
     * Устанавливает цвет подложки для времени исходящего сообщения
     * @param outgoingImageTimeBackgroundColor цвет подложки
     */
    public ChatStyle setOutgoingImageTimeBackgroundColor(@ColorRes final int outgoingImageTimeBackgroundColor) {
        this.outgoingImageTimeBackgroundColor = outgoingImageTimeBackgroundColor;
        return this;
    }

    /**
     * Устанавливает цвет подложки для времени входящего сообщения
     * @param incomingImageTimeBackgroundColor цвет подложки
     */
    public ChatStyle setIncomingImageTimeBackgroundColor(@ColorRes final int incomingImageTimeBackgroundColor) {
        this.incomingImageTimeBackgroundColor = incomingImageTimeBackgroundColor;
        return this;
    }

    /**
     * Устанавливает цвет подсветки фона при выделении сообщения (долгий тап)
     * @param chatHighlightingColor цвет подсветки фона
     * @return
     */
    public ChatStyle setChatHighlightingColor(@ColorRes final int chatHighlightingColor) {
        this.chatHighlightingColor = chatHighlightingColor;
        return this;
    }

    /**
     * Устанавливает цвет фона для чата
     * @param chatBackgroundColor цвет фона для чата
     */
    public ChatStyle setChatBackgroundColor(@ColorRes final int chatBackgroundColor) {
        this.chatBackgroundColor = chatBackgroundColor;
        return this;
    }

    /**
     * Устанавливает цвет текста ссылок во входящих сообщениях
     * @param incomingMessageLinkColor цвет текста ссылок
     */
    public ChatStyle setIncomingMessageLinkColor(@ColorRes final int incomingMessageLinkColor) {
        this.incomingMessageLinkColor = incomingMessageLinkColor;
        return this;
    }

    /**
     * Устанавливает цвет текста ссылок в исходящих сообщениях
     * @param outgoingMessageLinkColor цвет текста ссылок
     */
    public ChatStyle setOutgoingMessageLinkColor(@ColorRes final int outgoingMessageLinkColor) {
        this.outgoingMessageLinkColor = outgoingMessageLinkColor;
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
     * @param attachmentIconResId           - R.drawable.threads_ic_attachment_button
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
     * Устанавливает стили для отзывов. Можно передать null для значений, которые хотите оставить по умолчанию
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
    public ChatStyle setSurveyStyle(@DrawableRes final Integer binarySurveyLikeUnselectedIconResId,
                                    @DrawableRes final Integer binarySurveyLikeSelectedIconResId,
                                    @DrawableRes final Integer binarySurveyDislikeUnselectedIconResId,
                                    @DrawableRes final Integer binarySurveyDislikeSelectedIconResId,
                                    @DrawableRes final Integer optionsSurveyUnselectedIconResId,
                                    @DrawableRes final Integer optionsSurveySelectedIconResId,
                                    @ColorRes final Integer surveySelectedColorFilterResId,
                                    @ColorRes final Integer surveyUnselectedColorFilterResId,
                                    @ColorRes final Integer surveyTextColorResId,
                                    @ColorRes Integer surveyChoicesTextColorResId) {
        if (binarySurveyLikeUnselectedIconResId != null) this.binarySurveyLikeUnselectedIconResId = binarySurveyLikeUnselectedIconResId;
        if (binarySurveyLikeSelectedIconResId != null) this.binarySurveyLikeSelectedIconResId = binarySurveyLikeSelectedIconResId;
        if (binarySurveyDislikeUnselectedIconResId != null) this.binarySurveyDislikeUnselectedIconResId = binarySurveyDislikeUnselectedIconResId;
        if (binarySurveyDislikeSelectedIconResId != null) this.binarySurveyDislikeSelectedIconResId = binarySurveyDislikeSelectedIconResId;
        if (optionsSurveyUnselectedIconResId != null) this.optionsSurveyUnselectedIconResId = optionsSurveyUnselectedIconResId;
        if (optionsSurveySelectedIconResId != null) this.optionsSurveySelectedIconResId = optionsSurveySelectedIconResId;
        if (surveySelectedColorFilterResId != null) this.surveySelectedColorFilterResId = surveySelectedColorFilterResId;
        if (surveyUnselectedColorFilterResId != null) this.surveyUnselectedColorFilterResId = surveyUnselectedColorFilterResId;
        if (surveyTextColorResId != null) this.surveyTextColorResId = surveyTextColorResId;
        if (surveyChoicesTextColorResId != null) this.surveyChoicesTextColorResId = surveyChoicesTextColorResId;
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
     * @param surveyFinalColorFilterResId            - R.color.ecc_outgoing_message_text
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
                                    @ColorRes final int surveyFinalColorFilterResId,
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
        this.surveyFinalColorFilterResId = surveyFinalColorFilterResId;
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

    public MarkdownConfig getIncomingMarkdownConfiguration() {
        if (incomingMarkdownConfiguration == null) {
            incomingMarkdownConfiguration = new MarkdownConfig();
        }
        return incomingMarkdownConfiguration;
    }

    public ChatStyle setIncomingMarkdownConfiguration(MarkdownConfig incoming) {
        this.incomingMarkdownConfiguration = incoming;
        return this;
    }

    public MarkdownConfig getOutgoingMarkdownConfiguration() {
        if (outgoingMarkdownConfiguration == null) {
            outgoingMarkdownConfiguration = new MarkdownConfig();
        }
        return outgoingMarkdownConfiguration;
    }

    public ChatStyle setOutgoingMarkdownConfiguration(MarkdownConfig outgoing) {
        this.outgoingMarkdownConfiguration = outgoing;
        return this;
    }

    /**
     * Устанавливает стиль для системных сообщений. Можно передать null для значений, которые хотите оставить по умолчанию
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
            @DimenRes final Integer systemMessageTextSize,
            @ColorRes final Integer systemMessageTextColorResId,
            @DimenRes final Integer systemMessageLeftRightPadding,
            final Integer systemMessageTextGravity,
            @ColorRes final Integer systemMessageLinkColor
    ) {
        if (systemMessageFont != null) this.systemMessageFont = systemMessageFont;
        if (systemMessageTextSize != null) this.systemMessageTextSize = systemMessageTextSize;
        if (systemMessageTextColorResId != null) this.systemMessageTextColorResId = systemMessageTextColorResId;
        if (systemMessageLeftRightPadding != null) this.systemMessageLeftRightPadding = systemMessageLeftRightPadding;
        if (systemMessageTextGravity != null) this.systemMessageTextGravity = systemMessageTextGravity;
        if (systemMessageLinkColor != null) this.systemMessageLinkColor = systemMessageLinkColor;
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
     *
     * @param scrollDownIconResId иконка "стрелка вниз" для перехода к концу чата
     */
    public ChatStyle setScrollDownButtonIcon(@DrawableRes final int scrollDownIconResId) {
        this.scrollDownIconResId = scrollDownIconResId;
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
     * @param threadsRecordButtonBackgroundColor Устанавливает цвет бэкраунда кнопки записи голосовых сообщений
     */
    public ChatStyle setRecordButtonBackgroundColor(@ColorRes final int threadsRecordButtonBackgroundColor) {
        this.threadsRecordButtonBackgroundColor = threadsRecordButtonBackgroundColor;
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
     * Устанавливает цвет баббла и цвет картинки-ошибки слева (восклицательный знако по дефолту) если сообщение не было отправлено.
     *
     * @param messageNotSentBackgroundColor - Фоновый цвет для баббла. По умолчанию - R.color.ecc_error_red_df0000
     * @param messageNotSentErrorImageColor - Цвет картинки-ошибки слева для баббла (восклицательный знако по дефолту).
*      По умолчанию - R.color.ecc_white
     * @return Builder
     */
    public ChatStyle setMessageNotSentBubbleColors(
            @ColorRes final int messageNotSentBackgroundColor,
            @ColorRes final int messageNotSentErrorImageColor
    ) {
        this.messageNotSentBubbleBackgroundColor = messageNotSentBackgroundColor;
        this.messageNotSentErrorImageColor = messageNotSentErrorImageColor;
        return this;
    }

    /**
     * Default values:
     *
     * @param maxGalleryImagesCount - R.integer.ecc_max_count_attached_images
     * @return Builder
     */
    public ChatStyle setMaxGalleryImagesCount(@IntegerRes final int maxGalleryImagesCount) {
        this.maxGalleryImagesCount = maxGalleryImagesCount;
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
     * Устанавливает размеры бордера для входящих изображений в баббле
     * (если сообщение сообщение состоит только из изображения)
     * @param incomingImageLeftBorderSize ссылка на dimen ресурс для размера бордера входящего сообщения слева
     * @param incomingImageTopBorderSize ссылка на dimen ресурс для размера бордера входящего сообщения сверху
     * @param incomingImageRightBorderSize ссылка на dimen ресурс для размера бордера входящего сообщения справа
     * @param incomingImageBottomBorderSize ссылка на dimen ресурс для размера бордера входящего сообщения снизу
     */
    public ChatStyle setIncomingImageBordersSize(
            @DimenRes int incomingImageLeftBorderSize,
            @DimenRes int incomingImageTopBorderSize,
            @DimenRes int incomingImageRightBorderSize,
            @DimenRes int incomingImageBottomBorderSize
    ) {
        this.incomingImageLeftBorderSize = incomingImageLeftBorderSize;
        this.incomingImageTopBorderSize = incomingImageTopBorderSize;
        this.incomingImageRightBorderSize = incomingImageRightBorderSize;
        this.incomingImageBottomBorderSize = incomingImageBottomBorderSize;

        return this;
    }

    /**
     * Устанавливает размеры бордера для исходящих изображений в баббле
     * (если сообщение сообщение состоит только из изображения)
     * @param outgoingImageLeftBorderSize ссылка на dimen ресурс для размера бордера исходящего сообщения слева
     * @param outgoingImageTopBorderSize ссылка на dimen ресурс для размера бордера исходящего сообщения сверху
     * @param outgoingImageRightBorderSize ссылка на dimen ресурс для размера бордера исходящего сообщения справа
     * @param outgoingImageBottomBorderSize ссылка на dimen ресурс для размера бордера исходящего сообщения снизу
     */
    public ChatStyle setOutgoingImageBordersSize(
            @DimenRes int outgoingImageLeftBorderSize,
            @DimenRes int outgoingImageTopBorderSize,
            @DimenRes int outgoingImageRightBorderSize,
            @DimenRes int outgoingImageBottomBorderSize
    ) {
        this.outgoingImageLeftBorderSize = outgoingImageLeftBorderSize;
        this.outgoingImageTopBorderSize = outgoingImageTopBorderSize;
        this.outgoingImageRightBorderSize = outgoingImageRightBorderSize;
        this.outgoingImageBottomBorderSize = outgoingImageBottomBorderSize;

        return this;
    }

    /**
     * Устанавливает маску для входящего изобрадения
     * @param mask ссылка на ресурс изображения маски
     */
    public ChatStyle setIncomingImageMask(@DrawableRes int mask) {
        this.incomingImageBubbleMask = mask;
        return this;
    }

    /**
     * Устанавливает маску для входящего баббла сообщения
     * @param mask ссылка на ресурс изображения маски
     */
    public ChatStyle setIncomingBubbleMask(@DrawableRes int mask) {
        this.incomingMessageBubbleBackground = mask;
        return this;
    }

    /**
     * Устанавливает маску для исходящего изображения
     * @param mask ссылка на ресурс изображения маски
     */
    public ChatStyle setOutgoingImageMask(@DrawableRes int mask) {
        this.outgoingImageBubbleMask = mask;
        return this;
    }

    /**
     * Устанавливает маску для исходящего баббла сообщения
     * @param mask ссылка на ресурс изображения маски
     */
    public ChatStyle setOutgoingBubbleMask(@DrawableRes int mask) {
        this.outgoingMessageBubbleBackground = mask;
        return this;
    }

    /**
     * Устанавливает размер баббла для сообщений, содержащих изображение
     * @param size размер изображения в процентах от ширины экрана, где 0.0 - баббла нет, 1.0 - весь экран.
     * По умолчанию 0.66.
     */
    public ChatStyle setImageBubbleSize(float size) {
        this.imageBubbleSize = size;
        return this;
    }

    public ChatStyle setOutgoingMargin(
            @DimenRes int left,
            @DimenRes int top,
            @DimenRes int right,
            @DimenRes int bottom
    ) {
        this.bubbleOutgoingMarginLeft = left;
        this.bubbleOutgoingMarginTop = top;
        this.bubbleOutgoingMarginRight = right;
        this.bubbleOutgoingMarginBottom = bottom;
        return this;
    }

    public ChatStyle setIngoingMargin(
            @DimenRes int left,
            @DimenRes int top,
            @DimenRes int right,
            @DimenRes int bottom
    ) {
        this.bubbleIncomingMarginLeft = left;
        this.bubbleIncomingMarginTop = top;
        this.bubbleIncomingMarginRight = right;
        this.bubbleIncomingMarginBottom = bottom;
        return this;
    }

    public ChatStyle setInputFieldPadding(
            @DimenRes int left,
            @DimenRes int top,
            @DimenRes int right,
            @DimenRes int bottom
    ) {
        this.inputFieldPaddingLeft = left;
        this.inputFieldPaddingTop = top;
        this.inputFieldPaddingRight = right;
        this.inputFieldPaddingBottom = bottom;
        return this;
    }

    public ChatStyle setInputFieldMargin(
            @DimenRes int left,
            @DimenRes int top,
            @DimenRes int right,
            @DimenRes int bottom
    ) {
        this.inputFieldMarginLeft = left;
        this.inputFieldMarginTop = top;
        this.inputFieldMarginRight = right;
        this.inputFieldMarginBottom = bottom;
        return this;
    }

    public ChatStyle setOutgoingTimeTextSize(@DimenRes int textSize) {
        this.outgoingMessageTimeTextSize = textSize;
        return this;
    }

    public ChatStyle setIncomingTimeTextSize(@DimenRes int textSize) {
        this.incomingMessageTimeTextSize = textSize;
        return this;
    }

    /**
     * Устанавливает стиль(цвет текста, размер текста и цвет фона Toast уведомлений)
     *
     * @param toastTextSize
     * @param toastTextColor
     * @param toastBackgroundColor
     * @return объект ChatStyle
     */
    public ChatStyle setToastStyle(@DimenRes int toastTextSize,
                                   @ColorRes int toastTextColor,
                                   @ColorRes int toastBackgroundColor
    ) {
        this.toastTextSize = toastTextSize;
        this.toastTextColor = toastTextColor;
        this.toastBackgroundColor = toastBackgroundColor;
        return this;
    }

    /**
     * Определяет стилизован ли Toast
     *
     * @return возвращает true или false в зависимости от того, стилизован ли Toast
     */
    public boolean isToastStylable() {
        return  toastTextSize != 0 || toastTextColor != 0 || toastBackgroundColor != 0;
    }

    /**
     * Устанавливает иконку и цвет для исходящего сообщения со статусом "edited". Можно передать null для значения,
     * таким образом изменив одно из значений
     * @param messageEditedIconResId - ресурс id иконки. По умолчанию - R.drawable.ecc_message_image_edited
     * @param messageEditedIconColorResId - ресурс id цвета для иконки. По умолчанию - R.color.ecc_user_message_timestamp
     */
    public ChatStyle setMessageEditedResources(
            @DrawableRes Integer messageEditedIconResId,
            @ColorRes Integer messageEditedIconColorResId
    ) {
        if (messageEditedIconResId != null) this.messageEditedIconResId = messageEditedIconResId;
        if (messageEditedIconColorResId != null) this.messageEditedIconColorResId = messageEditedIconColorResId;
        return this;
    }

    /**
     * Устанавливает иконку и цвет для исходящего сообщения со статусом "sending". Можно передать null для значения,
     * таким образом изменив одно из значений
     * @param messageSendingIconResId - ресурс id иконки. По умолчанию - R.drawable.ecc_message_image_sending
     * @param messageSendingIconColorResId - ресурс id цвета для иконки. По умолчанию - R.color.ecc_white
     */
    public ChatStyle setMessageSendingResources(
            @DrawableRes Integer messageSendingIconResId,
            @ColorRes Integer messageSendingIconColorResId
    ) {
        if (messageSendingIconResId != null) this.messageSendingIconResId = messageSendingIconResId;
        if (messageSendingIconColorResId != null) this.messageSendingIconColorResId = messageSendingIconColorResId;
        return this;
    }

    /**
     * Устанавливает иконку и цвет для исходящего сообщения со статусом "sent". Можно передать null для значения,
     * таким образом изменив одно из значений
     * @param messageSentIconResId - ресурс id иконки. По умолчанию - R.drawable.ecc_message_image_sending
     * @param messageSentIconColorResId - ресурс id цвета для иконки. По умолчанию - R.color.ecc_white
     */
    public ChatStyle setMessageSentResources(
            @DrawableRes Integer messageSentIconResId,
            @ColorRes Integer messageSentIconColorResId
    ) {
        if (messageSentIconResId != null) this.messageSentIconResId = messageSentIconResId;
        if (messageSentIconColorResId != null) this.messageSentIconColorResId = messageSentIconColorResId;
        return this;
    }

    /**
     * Устанавливает иконку и цвет для исходящего сообщения со статусом "delivered". Можно передать null для значения,
     * таким образом изменив одно из значений
     * @param messageDeliveredIconResId - русурс id иконки. По умолчанию - R.drawable.ecc_message_image_delivered
     * @param messageDeliveredIconColorResId - ресурс id цвета для иконки. По умолчанию - R.color.ecc_white
     */
    public ChatStyle setMessageDeliveredResources(
            @DrawableRes Integer messageDeliveredIconResId,
            @ColorRes Integer messageDeliveredIconColorResId
    ) {
        if (messageDeliveredIconResId != null) this.messageDeliveredIconResId = messageDeliveredIconResId;
        if (messageDeliveredIconColorResId != null) this.messageDeliveredIconColorResId = messageDeliveredIconColorResId;
        return this;
    }

    /**
     * Устанавливает иконку и цвет для исходящего сообщения со статусом "read". Можно передать null для значения,
     * таким образом изменив одно из значений
     * @param messageReadIconResId - русурс id иконки. По умолчанию - R.drawable.ecc_image_message_read
     * @param messageReadIconColorResId - ресурс id цвета для иконки. По умолчанию - R.color.ecc_white
     */
    public ChatStyle setMessageReadResources(
            @DrawableRes Integer messageReadIconResId,
            @ColorRes Integer messageReadIconColorResId
    ) {
        if (messageReadIconResId != null) this.messageReadIconResId = messageReadIconResId;
        if (messageReadIconColorResId != null) this.messageReadIconColorResId = messageReadIconColorResId;
        return this;
    }

    /**
     * Устанавливает иконку и цвет для исходящего сообщения со статусом "failed".  Можно передать null для значения,
     * таким образом изменив одно из значений
     * @param messageFailedIconResId - русурс id иконки. По умолчанию - R.drawable.ecc_message_image_failed
     * @param messageFailedIconColorResId - ресурс id цвета для иконки. По умолчанию - R.color.ecc_white
     */
    public ChatStyle setMessageFailedResources(
            @DrawableRes Integer messageFailedIconResId,
            @ColorRes Integer messageFailedIconColorResId
    ) {
        if (messageFailedIconResId != null) this.messageFailedIconResId = messageFailedIconResId;
        if (messageFailedIconColorResId != null) this.messageFailedIconColorResId = messageFailedIconColorResId;
        return this;
    }

    /**
     * Устанавливает цвет текста для цитат
     * @param quoteHeaderTextColor - ресурс id цвета для автора цитаты. По умолчанию - R.color.ecc_incoming_message_text
     * @param quoteTextTextColor - ресурс id цвета для текста цитаты. По умолчанию - R.color.ecc_incoming_message_text
     */
    public ChatStyle setQuoteTextColors(
            @ColorRes int quoteHeaderTextColor,
            @ColorRes int quoteTextTextColor
    ) {
        this.quoteHeaderTextColor = quoteHeaderTextColor;
        this.quoteTextTextColor = quoteTextTextColor;
        return this;
    }

    /**
     * Устанавливает цвета текста ошибки(под баблом)
     * @param errorMessageTextColor - ресурс id цвета для текста ошибки. По умолчанию - R.color.ecc_error_red_df0000
     */
    public ChatStyle setErrorMessageTextColor(
            @ColorRes int errorMessageTextColor
    ) {
        this.errorMessageTextColor = errorMessageTextColor;
        return this;
    }

    /**
     * Включает предпросмотр для ссылок (OpenGraph). По умолчанию отключено.
     */
    public ChatStyle enableLinkPreview() {
        linkPreviewEnabled = true;
        return this;
    }

    /**
     * Устанавливает цвет для полос-разделителей внутри элементов чата
     * @param incomingDelimitersColor - ресурс id цвета для разделителей входящих сообщений. По умолчанию - R.color.ecc_chat_toolbar
     * @param outgoingDelimitersColor - ресурс id цвета для разделителей изходящих сообщений. По умолчанию - R.color.ecc_outgoing_message_text
     */
    public ChatStyle setDelimitersColors(
            @ColorRes int incomingDelimitersColor,
            @ColorRes int outgoingDelimitersColor
    ) {
        this.incomingDelimitersColor = incomingDelimitersColor;
        this.outgoingDelimitersColor = outgoingDelimitersColor;
        return this;
    }

    /**
     * Устанавливает видимость элементов для SearchBar чат-фрагмента
     * @param isClearSearchBtnVisible устанавливает видимость кнопки очистки для поля ввода. По умолчанию true
     * @param isSearchLoaderVisible устанавливает видимость лоадера при загрузке результатов поиска для поля ввода. По умолчанию true
     */
    public ChatStyle setSearchBarItemsVisibility(
            boolean isClearSearchBtnVisible,
            boolean isSearchLoaderVisible
    ) {
        this.isClearSearchBtnVisible = isClearSearchBtnVisible;
        this.isSearchLoaderVisible = isSearchLoaderVisible;
        return this;
    }

    /**
     * Устанавливает иконку поиска для лоадера при загрузке результатов поиска в поле ввода. По умолчанию отображается дефолтный лоадер
     * @param searchLoaderDrawableRes иконка поиска для лоадера (ссылка на ресурс)
     */
    public ChatStyle setSearchLoaderDrawable(@DrawableRes int searchLoaderDrawableRes) {
        this.searchLoaderDrawable = searchLoaderDrawableRes;
        return this;
    }

    /**
     * Устанавливает цвет иконки поиска для лоадера при загрузке результатов поиска в поле ввода. По умолчанию #fff
     * @param searchLoaderColorTintRes цвет иконка поиска для лоадера (ссылка на ресурс)
     */
    public ChatStyle setSearchLoaderColorTint(@ColorRes int searchLoaderColorTintRes) {
        this.searchLoaderColorTint = searchLoaderColorTintRes;
        return this;
    }

    /**
     * Устанавливает цвет иконки очистки поля ввода при поиске
     * @param clearIconColorRes ресурс цвета иконки для очистки поля ввода при поиске
     */
    public ChatStyle setSearchClearIconColor(@ColorRes int clearIconColorRes) {
        this.searchClearIconTintColor = clearIconColorRes;
        return this;
    }

    /**
     * Устанавливает ресурс иконки для очистки поля ввода при поиске
     * @param clearIconDrawableRes ресурс иконки для очистки поля ввода при поиске
     */
    public ChatStyle setSearchClearIconDrawable(@DrawableRes int clearIconDrawableRes) {
        this.searchClearIconDrawable = clearIconDrawableRes;
        return this;
    }

    /**
     * Устанавливает цвет разделителя между элементами в результатах поиска
     * @param searchResultsDividerColorRes цвет разделителя (ресурс)
     */
    public ChatStyle setSearchResultsDividerColor(@ColorRes int searchResultsDividerColorRes) {
        this.searchResultsDividerColor = searchResultsDividerColorRes;
        return this;
    }

    /**
     * Устанавливает цвета текста для одного элемента с результатами поиска
     * @param messageTextColorRes ресурс цвета для текста с сообщением. Передайте null, если хотите оставить значение по умолчанию
     * @param nameTextColorRes ресурс цвета для текста с автором сообщения. Передайте null, если хотите оставить значение по умолчанию
     */
    public ChatStyle setSearchResultsTextColors(
            @ColorRes Integer messageTextColorRes,
            @ColorRes Integer nameTextColorRes
    ) {
        if (messageTextColorRes != null) this.searchResultsItemMessageTextColor = messageTextColorRes;
        if (nameTextColorRes != null) this.searchResultsItemNameTextColor = nameTextColorRes;

        return this;
    }

    /**
     * Устанавливает drawable и цвет для иконки "стрелка вправо" в результатах поиска
     * @param rightArrowIconRes ресурс иконки. Передайте null, если не хотите менять данную иконку
     * @param rightArrowTintColorRes ресурс цвета иконки. Передайте null, если хотите оставить значение по умолчанию
     */
    public ChatStyle setSearchResultsRightArrow(
            @DrawableRes Integer rightArrowIconRes,
            @ColorRes Integer rightArrowTintColorRes
    ) {
        if (rightArrowIconRes != null) this.searchResultsItemRightArrowDrawable = rightArrowIconRes;
        if (rightArrowTintColorRes != null) this.searchResultsItemRightArrowTintColor = rightArrowTintColorRes;

        return this;
    }

    /**
     * Устанавливает цвет текста для даты в результатах поиска
     * @param dateTextColorRes ресурс цвета для текста
     */
    public ChatStyle setSearchResultsDateTextColor(@ColorRes int dateTextColorRes) {
        this.searchResultsItemDateTextColor = dateTextColorRes;
        return this;
    }

    /**
     * Устанавливает элементы, отображаемые при отсутствии результатов поиска
     * @param noResultFoundImageRes ресурс изображения. Передайте null, если хотите оставить значение по умолчанию
     * @param noResultFoundTextRes ресурс текста. Передайте null, если хотите оставить значение по умолчанию
     * @param noResultFoundTextColorRes ресурс цвета текста. Передайте null, если хотите оставить значение по умолчанию
     */
    public ChatStyle setSearchResultsNoItemsElements(
            @DrawableRes Integer noResultFoundImageRes,
            @StringRes Integer noResultFoundTextRes,
            @ColorRes Integer noResultFoundTextColorRes
    ) {
        if (noResultFoundImageRes != null) this.searchResultNoItemsImageDrawable = noResultFoundImageRes;
        if (noResultFoundTextRes != null) this.searchResultNoItemsText = noResultFoundTextRes;
        if (noResultFoundTextColorRes != null) this.searchResultNoItemsTextColor = noResultFoundTextColorRes;

        return this;
    }

    /**
     * Устанавливает цвет подсказки и ее текст при поиске сообщений, а также цвет текста при вводе запроса
     * @param hintTextColorRes ресурс текста для подсказки при поиске в поле ввода. Передайте null, если хотите оставить значение по умолчанию
     * @param hintTextRes ресурс цвета текста для подсказки при поиске в поле ввода. Передайте null, если хотите оставить значение по умолчанию
     * @param searchTextColorRes ресурс цвета текста при вводе запроса. Передайте null, если хотите оставить значение по умолчанию
     */
    public ChatStyle setSearchBarText(
            @ColorRes Integer hintTextColorRes,
            @StringRes Integer hintTextRes,
            @ColorRes Integer searchTextColorRes
    ) {
        if (hintTextColorRes != null) this.chatToolbarHintTextColor = hintTextColorRes;
        if (hintTextRes != null) this.searchMessageHintText = hintTextRes;
        if (searchTextColorRes != null) this.searchBarTextColor = searchTextColorRes;

        return this;
    }

    /**
     * Устанавливает тексты ошибок получения настроек, настроек вложений и расписания чата
     * @param loadedSettingsErrorTextRes ресурс текста ошибки полуения настроек. Передайте null, если хотите оставить значение по умолчанию
     * @param loadedAttachmentSettingsErrorTextRes ресурс текста ошибки полуения настроек вложений. Передайте null, если хотите оставить значение по умолчанию
     */
    public ChatStyle setStartLoadErrorTexts(
            @StringRes Integer loadedSettingsErrorTextRes,
            @StringRes Integer loadedAttachmentSettingsErrorTextRes
    ) {
        if (loadedSettingsErrorTextRes != null) {
            this.loadedSettingsErrorText = loadedSettingsErrorTextRes;
        }
        if (loadedAttachmentSettingsErrorTextRes != null) {
            this.loadedAttachmentSettingsErrorText = loadedAttachmentSettingsErrorTextRes;
        }
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        Context context = null;
        try {
            context = BaseConfig.Companion.getInstance().context;
        } catch (Exception ignored) {}

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ChatStyle settings. ");
        if (context != null) {
            stringBuilder.append("windowLightStatusBarResId: ").append(context.getResources().getBoolean(windowLightStatusBarResId)).append(" | ");
            stringBuilder.append("fixedChatTitle: ").append(context.getResources().getBoolean(fixedChatTitle)).append(" | ");
            stringBuilder.append("fixedChatSubtitle: ").append(context.getResources().getBoolean(fixedChatSubtitle)).append(" | ");
            stringBuilder.append("isChatSubtitleVisible: ").append(context.getResources().getBoolean(isChatSubtitleVisible)).append(" | ");
            stringBuilder.append("isChatTitleShadowVisible: ").append(context.getResources().getBoolean(isChatTitleShadowVisible)).append(" | ");
            stringBuilder.append("searchEnabled: ").append(context.getResources().getBoolean(searchEnabled)).append(" | ");
        }
        stringBuilder.append("linkPreviewEnabled: ").append(linkPreviewEnabled).append(" | ");
        stringBuilder.append("arePermissionDescriptionDialogsEnabled: ").append(arePermissionDescriptionDialogsEnabled).append(" | ");
        stringBuilder.append("showBackButton: ").append(showBackButton).append(" | ");
        stringBuilder.append("isToolbarTextCentered: ").append(isToolbarTextCentered).append(" | ");
        stringBuilder.append("chatSubtitleShowOrgUnit: ").append(chatSubtitleShowOrgUnit).append(" | ");
        stringBuilder.append("imageBubbleSize: ").append(imageBubbleSize).append(" | ");
        stringBuilder.append("showConsultSearching: ").append(showConsultSearching).append(" | ");
        stringBuilder.append("scrollChatToEndIfUserTyping: ").append(scrollChatToEndIfUserTyping).append(" | ");
        stringBuilder.append("inputEnabledDuringQuickReplies: ").append(inputEnabledDuringQuickReplies).append(" | ");
        stringBuilder.append("canShowSpecialistInfo: ").append(canShowSpecialistInfo).append(" | ");
        stringBuilder.append("voiceMessageEnabled: ").append(voiceMessageEnabled).append(",");

        return stringBuilder.toString();
    }

    /**
     * Кастомизирует внешний вид цитат во входящих сообщениях
     * @param quoteIncomingBackgroundColorRes ресурс цвета фона цитаты
     * @param quoteIncomingDelimiterColorRes ресурс цвета разделителя
     * @param quoteIncomingAuthorTextColorRes ресурс цвета текста автора цитируемого сообщения
     * @param quoteIncomingTextColorRes ресурс цвета текста цитируемого сообщения
     */
    public ChatStyle setIncomingQuoteViewStyle(
            @ColorRes Integer quoteIncomingBackgroundColorRes,
            @ColorRes Integer quoteIncomingDelimiterColorRes,
            @ColorRes Integer quoteIncomingAuthorTextColorRes,
            @ColorRes Integer quoteIncomingTextColorRes
    ) {
        if (quoteIncomingBackgroundColorRes != null) {
            this.quoteIncomingBackgroundColorRes = quoteIncomingBackgroundColorRes;
        }
        if (quoteIncomingDelimiterColorRes != null) {
            this.quoteIncomingDelimiterColorRes = quoteIncomingDelimiterColorRes;
        }
        if (quoteIncomingAuthorTextColorRes != null) {
            this.quoteIncomingAuthorTextColorRes = quoteIncomingAuthorTextColorRes;
        }
        if (quoteIncomingTextColorRes != null) {
            this.quoteIncomingTextColorRes = quoteIncomingTextColorRes;
        }
        return this;
    }

    /**
     * Кастомизирует внешний вид цитат в исходящих сообщениях
     * @param quoteIncomingBackgroundColorRes ресурс цвета фона цитаты
     * @param quoteIncomingDelimiterColorRes ресурс цвета разделителя
     * @param quoteIncomingAuthorTextColorRes ресурс цвета текста автора цитируемого сообщения
     * @param quoteIncomingTextColorRes ресурс цвета текста цитируемого сообщения
     */
    public ChatStyle setOutgoingQuoteViewStyle(
            @ColorRes Integer quoteOutgoingBackgroundColorRes,
            @ColorRes Integer quoteOutgoingDelimiterColorRes,
            @ColorRes Integer quoteOutgoingAuthorTextColorRes,
            @ColorRes Integer quoteOutgoingTextColorRes
    ) {
        if (quoteOutgoingBackgroundColorRes != null) {
            this.quoteOutgoingBackgroundColorRes = quoteOutgoingBackgroundColorRes;
        }
        if (quoteOutgoingDelimiterColorRes != null) {
            this.quoteOutgoingDelimiterColorRes = quoteOutgoingDelimiterColorRes;
        }
        if (quoteOutgoingAuthorTextColorRes != null) {
            this.quoteOutgoingAuthorTextColorRes = quoteOutgoingAuthorTextColorRes;
        }
        if (quoteOutgoingTextColorRes != null) {
            this.quoteOutgoingTextColorRes = quoteOutgoingTextColorRes;
        }
        return this;
    }
}
