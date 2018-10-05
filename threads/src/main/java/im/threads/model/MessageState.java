package im.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public enum MessageState {
    STATE_SENDING,
    STATE_SENT,
    STATE_WAS_READ,
    STATE_NOT_SENT;

    public static MessageState fromOrdinal(int ordinal) {
        return MessageState.values()[ordinal];
    }

}
