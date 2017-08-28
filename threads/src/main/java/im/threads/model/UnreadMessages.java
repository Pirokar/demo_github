package im.threads.model;

import android.content.Context;

import im.threads.R;

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

    public String getMessage(Context context) {
        return context.getResources().getQuantityString(R.plurals.unread_messages, count, count);
    }
}
