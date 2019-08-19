package im.threads.internal.model;

import java.util.List;

public final class Survey implements ChatItem {
    private long sendingId;
    private List<QuestionDTO> questions;
    private Long hideAfter;
    private long phraseTimeStamp;
    private MessageState sentState;

    public Survey(Long surveySendingId, Long hideAfter, long phraseTimeStamp, MessageState messageState) {
        this.sendingId = surveySendingId;
        this.hideAfter = hideAfter;
        this.phraseTimeStamp = phraseTimeStamp;
        this.sentState = messageState;
    }

    public Survey(Long surveySendingId, long phraseTimeStamp, MessageState messageState) {
        this(surveySendingId, null, phraseTimeStamp, messageState);
    }

    public long getSendingId() {
        return sendingId;
    }

    public void setSendingId(long sendingId) {
        this.sendingId = sendingId;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
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

    public MessageState getSentState() {
        return sentState;
    }

    public void setSentState(MessageState sentState) {
        this.sentState = sentState;
    }

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Survey)) {
            return false;
        } else {
            Survey otherSurvey = (Survey) obj;
            if (sendingId > 0) {
                return sendingId == otherSurvey.sendingId;
            }
            return false;
        }
    }

    @Override
    public int hashCode() {
        return String.valueOf(sendingId).hashCode();
    }
}
