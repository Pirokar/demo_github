package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

public final class RequestResolveThread implements ChatItem {
    private String uuid;
    private Long hideAfter;
    private long phraseTimeStamp;
    private Long threadId;

    public RequestResolveThread(final String uuid, final Long hideAfter, final long phraseTimeStamp, final Long threadId) {
        this.uuid = uuid;
        this.hideAfter = hideAfter;
        this.phraseTimeStamp = phraseTimeStamp;
        this.threadId = threadId;
    }

    public String getUuid() {
        return uuid;
    }

    public Long getHideAfter() {
        return hideAfter;
    }

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }

    @Override
    public Long getThreadId() {
        return threadId;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        if (otherItem instanceof RequestResolveThread) {
            return ObjectsCompat.equals(this.uuid, ((RequestResolveThread) otherItem).uuid);
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestResolveThread that = (RequestResolveThread) o;
        return ObjectsCompat.equals(uuid, that.uuid) &&
                phraseTimeStamp == that.phraseTimeStamp &&
                ObjectsCompat.equals(hideAfter, that.hideAfter) &&
                ObjectsCompat.equals(threadId, that.threadId);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(uuid, hideAfter, phraseTimeStamp, threadId);
    }
}
