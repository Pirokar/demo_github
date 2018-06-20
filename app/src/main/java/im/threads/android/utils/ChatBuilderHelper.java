package im.threads.android.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.StringRes;

import im.threads.android.R;
import im.threads.model.ChatStyle;

public class ChatBuilderHelper {

    private static final String LATO_BOLD_FONT_PATH = "fonts/lato-bold.ttf";
    private static final String LATO_LIGHT_FONT_PATH = "fonts/lato-light.ttf";
    private static final String LATO_REGULAR_FONT_PATH = "fonts/lato-regular.ttf";

    public enum ChatDesign {
        GREEN(R.string.design_green_enum),
        BLUE(R.string.design_blue_enum);

        @StringRes
        private int nameResId;

        ChatDesign(@StringRes int nameResId) {
            this.nameResId = nameResId;
        }

        public String getName(Context context) {
            return context.getString(nameResId);
        }

        public static ChatDesign enumOf(Context context, String name) {
            for (ChatDesign design : ChatDesign.values()) {
                if (design.getName(context).equalsIgnoreCase(name)) {
                    return design;
                }
            }
            return GREEN;
        }
    }

    public static ChatStyle buildChatStyle(Activity activity, String appMarker, String clientId, String userName, String data, ChatDesign design) {
        ChatStyle.ChatStyleBuilder builder = ChatStyle.ChatStyleBuilder
                .getBuilder(activity, clientId, userName, data) // в последнем параметре в виде строки можно передать любую дополнительную информацию, напр. "{balance:"1000.00", fio:"Vasya Pupkin"}"
                .setAppMarker(appMarker)
                .setDefaultFontBold(LATO_BOLD_FONT_PATH)
                .setDefaultFontLight(LATO_LIGHT_FONT_PATH)
                .setDefaultFontRegular(LATO_REGULAR_FONT_PATH)
                .showChatBackButton(true)// показывать кнопку назад
                .setShowConsultSearching(true) //показывать загрузку при поиске консультанта
                .setScrollChatToEndIfUserTyping(false) // не прокручивать чат к последнему сообщению, если пользователь начал ввод
                .setDebugLoggingEnabled(true);

        switch (design) {
            case GREEN: {
                configureGreenDesign(builder);
                break;
            }
            case BLUE: {
                configureBlueDesign(builder);
                break;
            }
        }

        return builder.build();
    }

    private static void configureGreenDesign(ChatStyle.ChatStyleBuilder builder) {
        //Do nothing, using default threads design
    }

    private static void configureBlueDesign(ChatStyle.ChatStyleBuilder builder) {

        builder
                .setWelcomeScreenStyle(R.drawable.threads_welcome_logo,
                        R.string.threads_welcome_screen_title_text,
                        R.string.threads_welcome_screen_subtitle_text,
                        R.color.threads_welcome_screen_title,
                        R.color.threads_welcome_screen_subtitle,
                        R.dimen.threads_welcome_screen_title,
                        R.dimen.threads_welcome_screen_subtitle)
                .setChatBodyStyle(R.color.threads_chat_background,
                        R.color.threads_chat_highlighting,
                        R.color.lighter_blue,
                        R.color.threads_blue,
                        R.drawable.thread_incoming_bubble,
                        R.drawable.thread_outgoing_bubble,
                        R.color.threads_incoming_message_text,
                        R.color.threads_outgoing_message_text,
                        R.color.threads_operator_message_timestamp,
                        R.color.threads_user_message_timestamp,
                        R.color.threads_outgoing_message_time,
                        R.color.threads_outgoing_time_underlay,
                        R.color.threads_incoming_message_time,
                        R.color.threads_incoming_time_underlay,
                        R.color.threads_incoming_message_link,
                        R.color.threads_outgoing_message_link,
                        R.color.threads_blue,
                        R.color.threads_chat_connection_message,
                        R.color.threads_files_medias_screen_background,
                        R.color.threads_files_list,
                        R.color.threads_icon_and_separators_color,
                        R.drawable.threads_operator_avatar_placeholder,
                        R.dimen.threads_operator_photo_size,
                        R.dimen.threads_system_operator_photo_size,
                        R.drawable.threads_image_placeholder,
                        R.style.FileDialogStyleTransparent,
                        true,
                        false,
                        R.drawable.threads_scroll_down_btn_back,
                        R.color.threads_chat_unread_msg_sticker_background,
                        R.color.threads_chat_unread_msg_count_text)
                .setChatInputStyle(R.color.threads_input_hint,
                        R.color.threads_input_background,
                        R.color.threads_input_text,
                        LATO_LIGHT_FONT_PATH,
                        R.drawable.threads_ic_attachment_button,
                        R.drawable.threads_ic_send_button,
                        R.string.threads_input_hint,
                        R.dimen.threads_input_height,
                        R.drawable.threads_chat_input_background)
                .setChatTitleStyle(R.string.threads_contact_center,
                        R.string.threads_operator_subtitle,
                        R.color.threads_blue,
                        R.color.threads_chat_toolbar_text,
                        R.color.threads_blue_grey_607d8b,
                        R.color.threads_blue_0F87FF,
                        R.color.lighter_blue,
                        true)
                .setPushNotificationStyle(R.drawable.default_push_icon,
                        R.string.threads_push_title,
                        R.color.threads_blue,
                        R.color.threads_blue_0F87FF,
                        R.color.threads_quick_reply_message_background,
                        R.color.threads_quick_reply_message_text_color)
                .setImagesGalleryStyle(R.color.threads_blue_grey_607d8b,
                        R.color.threads_attachments_background,
                        R.color.threads_attachments_author_text_color,
                        R.color.threads_attachments_date_text_color,
                        R.dimen.threads_attachments_author_text_size,
                        R.dimen.threads_attachments_date_text_size)
                .setRequestResolveThreadStyle(R.string.threads_request_to_resolve_thread,
                        R.string.threads_request_to_resolve_thread_close,
                        R.string.threads_request_to_resolve_thread_open)
                .setScheduleMessageStyle(R.drawable.threads_schedule_icon,
                        R.color.threads_schedule_text)
                .setSurveyStyle(R.drawable.threads_binary_survey_like_unselected,
                        R.drawable.threads_binary_survey_like_selected,
                        R.drawable.threads_binary_survey_dislike_unselected,
                        R.drawable.threads_binary_survey_dislike_selected,
                        R.drawable.threads_options_survey_unselected,
                        R.drawable.threads_options_survey_selected,
                        R.color.threads_blue,
                        R.color.threads_blue,
                        R.color.threads_chat_system_message);

    }
}