package im.threads.internal.model;

public class RequestResolveThread implements ChatItem {
    private Long hideAfter;
    private long phraseTimeStamp;

    public RequestResolveThread(final Long hideAfter, final long phraseTimeStamp) {
        this.hideAfter = hideAfter;
        this.phraseTimeStamp = phraseTimeStamp;
    }

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
