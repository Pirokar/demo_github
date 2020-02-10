package im.threads.internal.model;

public class OperatorLookupStarted implements ChatItem {

    private final long timeStamp;

    public OperatorLookupStarted(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }
}
