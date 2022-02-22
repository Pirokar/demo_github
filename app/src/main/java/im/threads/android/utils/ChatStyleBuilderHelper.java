package im.threads.android.utils;

import android.content.Context;
import android.view.Gravity;

import androidx.annotation.StringRes;

import im.threads.ChatStyle;
import im.threads.android.R;

public class ChatStyleBuilderHelper {

    private static final String LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf";
    private static final String LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf";
    private static final String LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf";

    public static ChatStyle getChatStyle(ChatDesign design) {
        ChatStyle chatStyle = new ChatStyle()
                .setDefaultFontBold(LATO_BOLD_FONT_PATH)
                .setDefaultFontLight(LATO_LIGHT_FONT_PATH)
                .setDefaultFontRegular(LATO_REGULAR_FONT_PATH)
                .showChatBackButton(true)// показывать кнопку назад
                .setUseExternalCameraApp(true)
                .setSelfieEnabled(false)
                .setScrollChatToEndIfUserTyping(false);
        switch (design) {
            case GREEN: {
                configureGreenDesign(chatStyle);
                break;
            }
            case BLUE: {
                configureBlueDesign(chatStyle);
                break;
            }
        }
        return chatStyle;
    }

    private static void configureGreenDesign(ChatStyle chatStyle) {
        chatStyle.setChatSubtitleShowConsultOrgUnit(true)
                .setShowConsultSearching(true)
                .setVoiceMessageEnabled(true);
        //Do nothing, using default threads design
    }

    private static void configureBlueDesign(ChatStyle chatStyle) {
        chatStyle
                .setWelcomeScreenStyle(R.drawable.alt_threads_welcome_logo,
                        R.string.alt_threads_welcome_screen_title_text,
                        R.string.alt_threads_welcome_screen_subtitle_text,
                        R.color.alt_threads_welcome_screen_title,
                        R.color.alt_threads_welcome_screen_subtitle,
                        R.dimen.alt_threads_welcome_screen_title,
                        R.dimen.alt_threads_welcome_screen_subtitle,
                        R.dimen.alt_threads_welcome_screen_width,
                        R.dimen.alt_threads_welcome_screen_height)
                .setChatBodyStyle(R.color.alt_threads_chat_background,
                        R.color.alt_threads_chat_highlighting,
                        R.color.alt_threads_chat_incoming_message_bubble,
                        R.color.alt_threads_chat_outgoing_message_bubble,
                        R.drawable.alt_thread_incoming_bubble,
                        R.drawable.alt_thread_outgoing_bubble,
                        R.color.alt_threads_incoming_message_text,
                        R.color.alt_threads_outgoing_message_text,
                        R.color.alt_threads_operator_message_timestamp,
                        R.color.alt_threads_user_message_timestamp,
                        R.drawable.alt_thread_outgoing_image_mask,
                        R.color.alt_threads_outgoing_message_time,
                        R.color.alt_threads_outgoing_time_underlay,
                        R.drawable.alt_thread_incoming_image_mask,
                        R.color.alt_threads_incoming_message_time,
                        R.color.alt_threads_incoming_time_underlay,
                        R.color.alt_threads_incoming_message_link,
                        R.color.alt_threads_outgoing_message_link,
                        R.color.alt_threads_chat_icons_tint,
                        R.color.alt_threads_chat_connection_message,
                        R.color.alt_threads_files_medias_screen_background,
                        R.color.alt_threads_files_list,
                        R.color.alt_threads_icon_and_separators_color,
                        R.drawable.ic_account_circle,
                        R.dimen.alt_threads_operator_photo_size,
                        R.dimen.alt_threads_system_operator_photo_size,
                        R.drawable.alt_threads_image_placeholder,
                        R.style.AltFileDialogStyleTransparent,
                        false,
                        false)
                .setChatInputStyle(R.color.alt_threads_input_hint,
                        R.color.alt_threads_input_background,
                        R.color.alt_threads_input_text,
                        null,
                        R.drawable.alt_threads_ic_attachment_button,
                        R.drawable.alt_threads_ic_send_button,
                        R.string.alt_threads_input_hint,
                        R.dimen.alt_threads_input_height,
                        R.drawable.alt_threads_chat_input_background)
                .setChatTitleStyle(R.string.alt_threads_contact_center,
                        R.string.alt_threads_operator_subtitle,
                        R.color.alt_threads_chat_toolbar,
                        R.color.alt_threads_chat_context_menu,
                        R.color.alt_threads_chat_toolbar_text,
                        R.color.alt_threads_chat_status_bar,
                        R.bool.alt_threads_chat_is_light_status_bar,
                        R.color.alt_threads_chat_toolbar_menu_item,
                        R.color.alt_threads_chat_toolbar_hint,
                        false)
                .setPushNotificationStyle(R.drawable.alt_default_push_icon,
                        R.string.alt_threads_push_title,
                        R.color.alt_threads_push_background,
                        R.color.alt_threads_nougat_push_accent,
                        R.color.alt_threads_quick_reply_message_background,
                        R.color.alt_threads_quick_reply_message_text_color)
                .setImagesGalleryStyle(
                        R.color.alt_threads_attachments_background,
                        R.color.alt_threads_attachments_author_text_color,
                        R.color.alt_threads_attachments_date_text_color,
                        R.dimen.alt_threads_attachments_author_text_size,
                        R.dimen.alt_threads_attachments_date_text_size)
                .setRequestResolveThreadStyle(R.string.alt_threads_request_to_resolve_thread,
                        R.string.alt_threads_request_to_resolve_thread_close,
                        R.string.alt_threads_request_to_resolve_thread_open)
                .setScheduleMessageStyle(R.drawable.alt_threads_schedule_icon,
                        R.color.alt_threads_schedule_text)
                .setSurveyStyle(R.drawable.alt_threads_binary_survey_like_unselected,
                        R.drawable.alt_threads_binary_survey_like_selected,
                        R.drawable.alt_threads_binary_survey_dislike_unselected,
                        R.drawable.alt_threads_binary_survey_dislike_selected,
                        R.drawable.alt_threads_options_survey_unselected,
                        R.drawable.alt_threads_options_survey_selected,
                        R.color.alt_threads_survey_selected_icon_tint,
                        R.color.alt_threads_survey_unselected_icon_tint,
                        R.color.alt_threads_chat_system_message,
                        R.color.alt_threads_survey_choices_text)
                .setInputEnabledDuringQuickReplies(true)
                .setSystemMessageStyle(
                        null,
                        R.dimen.alt_threads_system_message_text_size,
                        R.color.alt_threads_chat_new_system_message,
                        R.dimen.alt_threads_system_message_left_right_padding,
                        Gravity.START,
                        R.color.alt_threads_system_message_link
                )
                .setSwipeRefreshColors(R.array.alt_threads_swipe_refresh_colors)
                .setScrollDownButtonStyle(
                        R.drawable.alt_threads_scroll_down_icon,
                        R.drawable.alt_threads_scroll_down_background,
                        R.dimen.alt_threads_scroll_down_button_width,
                        R.dimen.alt_threads_scroll_down_button_height,
                        R.dimen.alt_threads_scroll_down_button_margin,
                        R.dimen.alt_threads_scroll_down_button_elevation,
                        R.color.alt_threads_chat_unread_msg_sticker_background,
                        R.color.alt_threads_chat_unread_msg_count_text
                )
                .setRecordButtonStyle(
                        R.drawable.alt_threads_record_button_background,
                        R.color.alt_threads_record_button_background,
                        R.drawable.alt_threads_record_button_icon,
                        R.color.alt_threads_record_button_icon,
                        R.color.alt_threads_record_button_small_mic
                )
                .setPlayPauseButtonStyle(
                        R.color.alt_threads_incoming_play_pause_button,
                        R.color.alt_threads_outgoing_play_pause_button,
                        R.color.alt_threads_preview_play_pause_button,
                        R.drawable.alt_threads_voice_message_play,
                        R.drawable.alt_threads_voice_message_pause
                )
                .setEmptyStateStyle(
                        R.color.alt_threads_empty_state_background,
                        R.color.alt_threads_empty_state_progress,
                        R.color.alt_threads_empty_state_hint
                )
                .setQuickReplyChipChoiceStyle(
                        R.drawable.alt_threads_quick_reply_button_background,
                        R.color.alt_threads_quick_reply_text_color
                )
                .setOutgoingPadding(
                        R.dimen.alt_bubbleOutgoingPaddingLeft,
                        R.dimen.alt_bubbleOutgoingPaddingTop,
                        R.dimen.alt_bubbleOutgoingPaddingRight,
                        R.dimen.alt_bubbleOutgoingPaddingBottom
                )
                .setIngoingPadding(
                        R.dimen.alt_bubbleIncomingPaddingLeft,
                        R.dimen.alt_bubbleIncomingPaddingTop,
                        R.dimen.alt_bubbleIncomingPaddingRight,
                        R.dimen.alt_bubbleIncomingPaddingBottom
                )

                .setFixedChatTitle(R.bool.alt_threads_chat_fixed_chat_title)
                .setMaxGalleryImagesCount(R.integer.alt_max_count_attached_images)
                .setLoaderTextResId(R.string.alt_loading)
                .setMediaAndFilesWindowLightStatusBarResId(R.bool.alt_threads_chat_is_light_status_bar)
                .setMediaAndFilesStatusBarColorResId(R.color.alt_threads_chat_status_bar)
                .setMediaAndFilesToolbarColorResId(R.color.alt_threads_chat_toolbar)
                .setMediaAndFilesToolbarTextColorResId(R.color.alt_threads_chat_toolbar_text)
                .setMediaAndFilesToolbarHintTextColor(R.color.alt_threads_chat_toolbar_hint)
                .setMediaAndFilesScreenBackgroundColor(R.color.alt_threads_files_medias_screen_background)
                .setMediaAndFilesTextColor(R.color.alt_threads_files_list)
                .setEmptyMediaAndFilesHeaderTextResId(R.string.threads_no_media_and_files_alt_header)
                .setEmptyMediaAndFilesHeaderFontPath(LATO_BOLD_FONT_PATH)
                .setEmptyMediaAndFilesHeaderTextSize(R.dimen.alt_threads_attachments_no_files_header_text_size)
                .setEmptyMediaAndFilesHeaderTextColor(R.color.white)
                .setEmptyMediaAndFilesDescriptionTextResId(R.string.threads_no_media_and_files_alt_description)
                .setEmptyMediaAndFilesDescriptionFontPath(LATO_LIGHT_FONT_PATH)
                .setEmptyMediaAndFilesDescriptionTextSize(R.dimen.alt_threads_attachments_no_files_description_text_size)
                .setEmptyMediaAndFilesDescriptionTextColor(R.color.white);
    }

    public enum ChatDesign {
        GREEN(R.string.design_green_enum),
        BLUE(R.string.design_blue_enum);

        @StringRes
        private int nameResId;

        ChatDesign(@StringRes int nameResId) {
            this.nameResId = nameResId;
        }

        public static ChatDesign enumOf(Context context, String name) {
            for (ChatDesign design : ChatDesign.values()) {
                if (design.getName(context).equalsIgnoreCase(name)) {
                    return design;
                }
            }
            return GREEN;
        }

        public static void setTheme(Context context, ChatDesign theme) {
            PrefUtils.storeTheme(context, theme);
        }

        public String getName(Context context) {
            return context.getString(nameResId);
        }
    }
}
