package im.threads.model;

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public String getMessage() {
        String phrase;
        if (Locale.getDefault().getLanguage().toLowerCase().equals("ru")) {
            if (count == 1 || (count % 10 == 1) && count != 11)
                return count + " непрочитанное сообщение";
            else if ((count > 1 && count < 5)
                    || (count > 20 && (count % 10 == 2 || count % 10 == 3 || count % 10 == 4)))
                return count + " непрочитанных сообщения";
            else return count + " непрочитанных сообщений";
        } else {
            if (count == 1) {
                phrase = "1 unread message";
            } else {
                phrase = count + " unread messages";
            }
        }
        return phrase;
    }

    @Override
    public String toString() {
        return "UnreadMessages{" +
                "timeStamp=" + timeStamp +
                ", count=" + count +
                '}';
    }
}
