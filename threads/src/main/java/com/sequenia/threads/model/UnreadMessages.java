package com.sequenia.threads.model;

import java.util.Locale;

/**
 * Created by yuri on 24.08.2016.
 */
public class UnreadMessages implements ChatItem {
    private long timeStamp;
    private int count;

    public UnreadMessages(long timeStamp, int count) {
        this.timeStamp = timeStamp;
        this.count = count;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public String getMessage() {
        String phrase;
        if (Locale.getDefault().getLanguage().toLowerCase().equals("ru")) {
            if ((count == 1 || (count % 10 == 1)) && count != 11) {
                phrase = count + " непрочитанное сообщение";
            } else {
                phrase = count + " непрочитанных сообщений";
            }
        } else {
            if (count == 1) {
                phrase = "1 unread message";
            } else {
                phrase = count + " unread messages";
            }
        }
        return phrase;
    }
}
