package im.threads.internal.model;

/**
 * Объект-пустышка для обработки сообщений, относящихся к чату (что бы они не прошли дальше),
 * но не имеющих действия в МП
 */
public class EmptyChatItem implements ChatItem {

    private String mType;

    public EmptyChatItem(final String type) {
        mType = type;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    public String getType() {
        return mType;
    }
}
