package com.sequenia.appwithchatdev;

import android.app.Activity;

import im.threads.model.ChatStyle;

public class ChatBuilderHelper {

    public static ChatStyle buildChatStyle(Activity activity, String clientId, String userName, String data) {
        return ChatStyle.ChatStyleBuilder
                .getBuilder(activity, clientId, userName, data) // в последнем параметре в виде строки можно передать любую дополнительную информацию, напр. "{balance:"1000.00", fio:"Vasya Pupkin"}"
                .setDefaultFontBold("fonts/lato-bold.ttf")
                .setDefaultFontLight("fonts/lato-light.ttf")
                .setDefaultFontRegular("fonts/lato-regular.ttf")
                .showChatBackButton(true)// показывать кнопку назад showBackButton
                .setGoogleAnalyticsEnabled(false)
                .setShowConsultSearching(true) //показывать загрузку при поиске консультанта
                .setScrollChatToEndIfUserTyping(false) // всегда прокручивать чат к последнему сообщению, если пользователь начал ввод
                .build();
    }
}
