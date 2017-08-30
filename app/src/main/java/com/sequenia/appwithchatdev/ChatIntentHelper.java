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
                .setDefaultFontBold("fonts/lato-bold.ttf")
                .setDefaultFontLight("fonts/lato-light.ttf")
                .setDefaultFontRegular("fonts/lato-regular.ttf")
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
                        R.dimen.operator_avatar_size,// размер аватара агента для обычных сообщений operatorAvatarSize
                        R.dimen.operator_system_avatar_size,// размер аватара агента для системных сообщений (подключился/ отключился/ поставил на паузу) operatorSystemAvatarSize
                        R.drawable.no_image,//заглушка вместо картинки imagePlaceholder
                        R.style.FileDialogStyle,//стиль диалога выбора файла fileBrowserDialogStyleResId
                        true, //показывать загрузку при поиске консультанта showConsultSearching
                        false, // всегда прокручивать чат к последнему сообщению, если пользователь начал ввод alwaysScrollToEnd
                        R.color.purple,
                        "fonts/lato-regular.ttf")
                .setGoogleAnalyticsEnabled(false)
                .setPushNotificationStyle(R.drawable.ic_push_notification,
                        R.string.app_name1,
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
                .setSurveyStyle(ChatStyle.INVALID, // Иконка бинарного опроса для положительного невыбранного ответа binarySurveyLikeUnselectedIconResId
                        ChatStyle.INVALID, // Иконка бинарного опроса для положительного выбранного ответа binarySurveyLikeSelectedIconResId
                        ChatStyle.INVALID, // Иконка бинарного опроса для отрицательного невыбранного ответа binarySurveyDislikeUnselectedIconResId
                        ChatStyle.INVALID, // Иконка бинарного опроса для отрицательного выбранного ответа binarySurveyDislikeSelectedIconResId
                        ChatStyle.INVALID, // Иконка небинарного опроса для невыбранного ответа (напр, пустая звездочка) optionsSurveyUnselectedIconResId
                        ChatStyle.INVALID, // Иконка небинарного опроса для выбранного ответа (напр, звездочка с заливкой) optionsSurveySelectedIconResId
                        R.color.chatbody_icons_tint, // Цветовой фильтр на иконку выбранного ответа (касается и бинарного и не бинарного опросов) surveySelectedColorFilterResId
                        R.color.chatbody_icons_tint, // Цветовой фильтр на иконку невыбранного ответа (касается и бинарного и не бинарного опросов) surveyUnselectedColorFilterResId
                        R.color.welcome_screen_text) // Цвет основного текста опроса surveyTextColorResId
                ;
    }

}
