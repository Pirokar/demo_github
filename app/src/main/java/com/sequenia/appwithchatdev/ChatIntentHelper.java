package com.sequenia.appwithchatdev;

import android.app.Activity;

import im.threads.model.ChatStyle;

/**
 * Created by chybakut2004 on 14.04.17.
 */

public class ChatIntentHelper {

    public static ChatStyle.IntentBuilder getIntentBuilder(Activity activity, String clientId, String userName) {
        return ChatStyle.IntentBuilder
                .getBuilder(activity, clientId, userName)
                .setChatTitleStyle(R.string.contact_center,//заголовок ToolBar chatTitleTextResId
                        R.color.toolbar_background,//ToolBar background chatTitleBackgroundColorResId
                        R.color.toolbar_widget,//Toolbar widget chatTitleWidgetsColorResId
                        R.color.status_bar,//status bar chatStatusBarColorResId
                        R.color.menu_item_text,//menu item text menuItemTextColorResId
                        R.color.toolbar_edit_text_hint)//Toolbar EditText hint color chatToolbarHintTextColor
                .setChatBodyStyle(
                        R.color.chat_background,//фон чата chatBackgroundColor
                        R.color.toolbar_background_transparent,//подсветка выделения элементов chatHighlightingColor
                        R.color.chat_message_hint_input_text,//подсказка в EditText chatMessageHintInputTextColor
                        R.color.chat_message_input_background,//заливка EditText chatMessageInputBackgroundColor
                        R.color.incoming_message_bubble_background,//заливка бабла входящего сообщения incomingMessageBubbleColor
                        R.color.outgoing_message_bubble_background,//заливка бабла исходящего сообщения outgoingMessageBubbleColor
                        R.color.incoming_message_text,//цвет текста входящего сообщения incomingMessageTextColor
                        R.color.outgoing_message_text,//цвет текста исходящего сообщения outgoingMessageTextColor
                        R.color.chatbody_icons_tint,//цвет иконок в поле сообщения chatBodyIconsTint
                        R.color.connection_message_text_color,//цвет текста сообщения о соединениии connectionMessageTextColor
                        R.drawable.blank_avatar_round_main,//аватар по умолчанию входящего сообщения defaultIncomingMessageAvatar
                        R.drawable.blank_avatar_round_main,//заглушка картинки тайпинга imagePlaceholder
                        R.style.FileDialogStyle,//стиль диалога выбора файла fileBrowserDialogStyleResId
                        5) // количество звезд для оценки
                //.setGoogleAnalyticsEnabled(false)
                .setPushNotificationStyle(R.drawable.push_icon_def,
                        R.string.default_title,
                        ChatStyle.INVALID,
                        ChatStyle.INVALID)
                .setWelcomeScreenStyle(
                        R.drawable.welcom_screen_image//логотип экрана приветствия welcomeScreenLogoResId
                        , R.string.welcome//заголовок экрана приветствия welcomeScreenTitleTextResId
                        , R.string.subtitle_text//подзаголовок экрана приветствия welcomeScreenSubtitleTextResId
                        , R.color.welcome_screen_text//цвет текста на экране приветствия welcomeScreenTextColorResId
                        , 18//размер шрифта заголовка titleSizeInSp
                        , 14)//размер шрифта подзаголовка subtitleSizeInSp
                .setScheduleMessageStyle(
                        R.drawable.schedule_icon, // Иконка сообщения о расписании
                        R.color.schedule_message_text_color); // Цвет текста в сообщении о расписании
    }

}