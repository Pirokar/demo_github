package im.threads.model;

public class RequestResolveThread implements ChatItem {

    private String messageId;
    private Long hideAfter;
    private long phraseTimeStamp;

    public RequestResolveThread(final String messageId, final Long hideAfter, final long phraseTimeStamp) {
        this.messageId = messageId;
        this.hideAfter = hideAfter;
        this.phraseTimeStamp = phraseTimeStamp;
    }

    public Long getHideAfter() {
        return hideAfter;
    }

    public void setHideAfter(Long hideAfter) {
        this.hideAfter = hideAfter;
    }

    public void setPhraseTimeStamp(long phraseTimeStamp) {
        this.phraseTimeStamp = phraseTimeStamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }
}
