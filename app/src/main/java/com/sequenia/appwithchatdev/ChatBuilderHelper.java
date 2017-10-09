package com.sequenia.appwithchatdev;

import android.app.Activity;

import im.threads.model.ChatStyle;

/**
 * Created by chybakut2004 on 14.04.17.
 */

public class ChatBuilderHelper {

    public static ChatStyle buildChatStyle(Activity activity, String clientId, String userName, String data) {
        return ChatStyle.ChatStyleBuilder
                .getBuilder(activity, clientId, userName, data) // в последнем параметре в виде строки можно передать любую дополнительную информацию, напр. "{balance:"1000.00", fio:"Vasya Pupkin"}"
                .setDefaultFontBold("fonts/lato-bold.ttf")
                .setDefaultFontLight("fonts/lato-light.ttf")
                .setDefaultFontRegular("fonts/lato-regular.ttf")
                .showChatBackButton(true)// показывать кнопку назад showBackButton
                .setChatBodyStyle(
                        R.color.chat_background,//фон чата chatBackgroundColor
                        R.color.toolbar_background_transparent,//подсветка выделения элементов chatHighlightingColor
                        R.color.incoming_message_bubble_background,//заливка бабла входящего сообщения incomingMessageBubbleColor
                        R.color.outgoing_message_bubble_background,//заливка бабла исходящего сообщения outgoingMessageBubbleColor
                        R.drawable.app_operator_bubble, // фон для сообщения оператора incomingMessageBubbleBackground
                        R.drawable.app_user_bubble, // фон для сообщения клиента outgoingMessageBubbleBackground
                        R.color.incoming_message_text,//цвет текста входящего сообщения incomingMessageTextColor
                        R.color.outgoing_message_text,//цвет текста исходящего сообщения outgoingMessageTextColor
                        R.color.chatbody_icons_tint,//цвет иконок в поле сообщения chatBodyIconsTint
                        R.color.connection_message_text_color,//цвет текста сообщения о соединениии connectionMessageTextColor
                        R.color.files_and_media_screen_bg_color,//цвет фона экрана Медиа и файлы filesAndMediaScreenBackgroundColor
                        R.color.threads_welcome_screen_title,//цвет сепараторов в опросах, текста оператор печатает, цвет дат и др вспомогательные элементы  iconsAndSeparatorsColor
                        R.drawable.blank_avatar_round_main,//аватар по умолчанию входящего сообщения defaultIncomingMessageAvatar
                        R.dimen.operator_avatar_size,// размер аватара агента для обычных сообщений operatorAvatarSize
                        R.dimen.operator_system_avatar_size,// размер аватара агента для системных сообщений (подключился/ отключился/ поставил на паузу) operatorSystemAvatarSize
                        R.drawable.no_image,//заглушка вместо картинки imagePlaceholder
                        R.style.FileDialogStyle,//стиль диалога выбора файла fileBrowserDialogStyleResId
                        true, //показывать загрузку при поиске консультанта showConsultSearching
                        false, // всегда прокручивать чат к последнему сообщению, если пользователь начал ввод
                        ChatStyle.INVALID, // иконка кнопки прокрутки чата к последнему сообщению scrollDownButtonResId
                        ChatStyle.INVALID, // цвет индикатора о непрочитанных сообщениях unreadMsgStickerColorResId
                        ChatStyle.INVALID) // цвет текста на индикаторе о непрочитанных сообщениях unreadMsgCountTextColorResId
                .setInputTextFont("fonts/lato-regular.ttf") // шрифт поля ввода сообщения inputTextFont
                .setChatInputStyle(
                        R.color.chat_message_hint_input_text,//подсказка в EditText chatMessageInputHintTextColor
                        R.color.chat_message_input_background,//заливка EditText, галереи и при пересылке в тулбаре chatMessageInputColor
                        R.color.purple, // цвет шрифта поля ввода сообщения inputTextColor
                        "fonts/lato-regular.ttf", // шрифт поля ввода сообщения inputTextFont
                        ChatStyle.INVALID, // иконка кнопки прикрепления файлов attachmentsIconResId
                        ChatStyle.INVALID, // иконка кнопки отправки сообщений sendMessageIconResId
                        R.string.input_hint, // текст-подсказка в поле ввода сообщения inputHint
                        ChatStyle.INVALID, // высота поля ввода сообщения в dp inputHeight
                        ChatStyle.INVALID) // фон поля ввода сообщения inputBackground
                .setGoogleAnalyticsEnabled(false)
                .setPushNotificationStyle(R.drawable.ic_push_notification,
                        R.string.app_name1,
                        R.color.colorAccent,
                        ChatStyle.INVALID)
                .setSurveyStyle(ChatStyle.INVALID, // Иконка бинарного опроса для положительного невыбранного ответа binarySurveyLikeUnselectedIconResId
                        ChatStyle.INVALID, // Иконка бинарного опроса для положительного выбранного ответа binarySurveyLikeSelectedIconResId
                        ChatStyle.INVALID, // Иконка бинарного опроса для отрицательного невыбранного ответа binarySurveyDislikeUnselectedIconResId
                        ChatStyle.INVALID, // Иконка бинарного опроса для отрицательного выбранного ответа binarySurveyDislikeSelectedIconResId
                        ChatStyle.INVALID, // Иконка небинарного опроса для невыбранного ответа (напр, пустая звездочка) optionsSurveyUnselectedIconResId
                        ChatStyle.INVALID, // Иконка небинарного опроса для выбранного ответа (напр, звездочка с заливкой) optionsSurveySelectedIconResId
                        R.color.chatbody_icons_tint, // Цветовой фильтр на иконку выбранного ответа (касается и бинарного и не бинарного опросов) surveySelectedColorFilterResId
                        R.color.chatbody_icons_tint, // Цветовой фильтр на иконку невыбранного ответа (касается и бинарного и не бинарного опросов) surveyUnselectedColorFilterResId
                        R.color.threads_welcome_screen_title) // Цвет основного текста опроса surveyTextColorResId
                .build();
    }

}
