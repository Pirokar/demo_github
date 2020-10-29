package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

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

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        return otherItem instanceof RequestResolveThread;
    }

    @Override
    public Long getThreadId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestResolveThread that = (RequestResolveThread) o;
        return phraseTimeStamp == that.phraseTimeStamp &&
                ObjectsCompat.equals(hideAfter, that.hideAfter);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(hideAfter, phraseTimeStamp);
    }
}
