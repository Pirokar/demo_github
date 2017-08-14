package com.sequenia.appwithchat.polarbear;

import android.app.Activity;

import im.threads.model.ChatStyle;

/**
 * Created by chybakut2004 on 14.04.17.
 */

public class ChatIntentHelper {

    public static ChatStyle.IntentBuilder getIntentBuilder(Activity activity, String clientId, String userName) {
        return ChatStyle.IntentBuilder
                .getBuilder(activity, clientId, userName)
                .setInputQuotedMessageAuthorFont("comic.ttf")
                .setDefaultFontBold("comic.ttf")
                .setPlaceholderTitleFont("comic.ttf")
                .setPlaceholderSubtitleFont("comic.ttf")
                .setBubbleMessageFont("comic.ttf")
                .setBubbleTimeFont("comic.ttf")
                .setTypingFont("comic.ttf")
                .setMessageHeaderFont("comic.ttf")
                .setSpecialistConnectTitleFont("comic.ttf")
                .setInputQuotedMessageFont("comic.ttf")
                .setPlaceholderTitleFont("comic.ttf")
                .setChatTitleStyle(R.string.contact_center,//заголовок ToolBar chatTitleTextResId
                        R.color.toolbar_background,//ToolBar background chatToolbarColorResId
                        R.color.toolbar_widget,//Toolbar widget chatToolbarTextColorResId
                        R.color.status_bar,//status bar chatStatusBarColorResId
                        R.color.menu_item_text,//menu item text menuItemTextColorResId
                        R.color.toolbar_edit_text_hint,//Toolbar EditText hint color chatToolbarHintTextColor
                        true)// показывать кнопку назад showBackButton
                .setChatBodyStyle(
                        R.color.chat_background,//фон чата chatBackgroundColor
                        R.color.toolbar_background_transparent,//подсветка выделения элементов chatHighlightingColor
                        R.color.chat_message_hint_input_text,//подсказка в EditText chatMessageInputHintTextColor
                        R.color.chat_message_input_background,//заливка EditText, галереи и при пересылке в тулбаре chatMessageInputColor
                        R.color.incoming_message_bubble_background,//заливка бабла входящего сообщения incomingMessageBubbleColor
                        R.color.outgoing_message_bubble_background,//заливка бабла исходящего сообщения outgoingMessageBubbleColor
                        R.color.incoming_message_text,//цвет текста входящего сообщения incomingMessageTextColor
                        R.color.outgoing_message_text,//цвет текста исходящего сообщения outgoingMessageTextColor
                        R.color.chatbody_icons_tint,//цвет иконок в поле сообщения chatBodyIconsTint
                        R.color.connection_message_text_color,//цвет текста сообщения о соединениии connectionMessageTextColor
                        R.color.files_and_media_screen_bg_color,//цвет фона экрана Медиа и файлы filesAndMediaScreenBackgroundColor
                        R.drawable.blank_avatar_round_main,//аватар по умолчанию входящего сообщения defaultIncomingMessageAvatar
                        R.drawable.no_image,//заглушка вместо картинки imagePlaceholder
                        R.style.FileDialogStyle,//стиль диалога выбора файла fileBrowserDialogStyleResId
                        true, //показывать загрузку при поиске консультанта showConsultSearching
                        false, // всегда прокручивать чат к последнему сообщению, если пользователь начал ввод
                        R.color.purple,
                        "comic.ttf")
                //.setGoogleAnalyticsEnabled(false)
                .setPushNotificationStyle(R.drawable.logo_2,
                        R.string.default_title,
                        R.color.colorAccent,
                        ChatStyle.INVALID)
                .setWelcomeScreenStyle(
                        R.drawable.welcom_screen_image//логотип экрана приветствия welcomeScreenLogoResId
                        , R.string.welcome//заголовок экрана приветствия welcomeScreenTitleTextResId
                        , R.string.subtitle_text//подзаголовок экрана приветствия welcomeScreenSubtitleTextResId
                        , R.color.welcome_screen_text//цвет текста на экране приветствия welcomeScreenTextColorResId
                        , 18//размер шрифта заголовка welcomeScreenTitleSizeInSp
                        , 14)//размер шрифта подзаголовка welcomeScreenSubtitleSizeInSp
                .setScheduleMessageStyle(
                        R.drawable.schedule_icon, // Иконка сообщения о расписании scheduleMessageIconResId
                        R.color.schedule_message_text_color) // Цвет текста в сообщении о расписании scheduleMessageTextColorResId
                .setRequestResolveThreadStyle(R.string.resolve_thread,
                        R.string.resolve_thread_approve,
                        R.string.resolve_thread_deny)
                .setSurveyStyle(ChatStyle.INVALID,
                        ChatStyle.INVALID,
                        ChatStyle.INVALID,
                        ChatStyle.INVALID,
                        ChatStyle.INVALID,
                        ChatStyle.INVALID,
                        R.color.chatbody_icons_tint,
                        R.color.chatbody_icons_tint,
                        R.color.welcome_screen_text)
                ;
    }

}
