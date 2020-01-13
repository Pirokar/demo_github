package im.threads.internal.model;

public final class RequestResolveThread implements ChatItem, Hidable {
    private Long hideAfter;
    private long phraseTimeStamp;

    public RequestResolveThread(final Long hideAfter, final long phraseTimeStamp) {
        this.hideAfter = hideAfter;
        this.phraseTimeStamp = phraseTimeStamp;
    }

    @Override
    public Long getHideAfter() {
        return hideAfter;
    }

    public void setHideAfter(Long hideAfter) {
        this.hideAfter = hideAfter;
    }

    public void setTimeStamp(long phraseTimeStamp) {
        this.phraseTimeStamp = phraseTimeStamp;
    }

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }
}
