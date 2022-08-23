package im.threads.business.models;

public enum MessageState {
    STATE_SENDING,
    STATE_SENT,
    STATE_WAS_READ,
    STATE_NOT_SENT;

    public static MessageState fromOrdinal(int ordinal) {
        return MessageState.values()[ordinal];
    }

}
