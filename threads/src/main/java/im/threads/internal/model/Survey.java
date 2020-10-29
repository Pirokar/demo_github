package im.threads.internal.model;

import androidx.core.util.ObjectsCompat;

import java.util.List;

public final class Survey implements ChatItem, Hidable {
    private long sendingId;
    private List<QuestionDTO> questions;
    private Long hideAfter;
    private long phraseTimeStamp;
    private MessageState sentState;

    public Survey(long surveySendingId, Long hideAfter, long phraseTimeStamp, MessageState messageState) {
        this.sendingId = surveySendingId;
        this.hideAfter = hideAfter;
        this.phraseTimeStamp = phraseTimeStamp;
        this.sentState = messageState;
    }

    public Survey(long surveySendingId, long phraseTimeStamp, MessageState messageState) {
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
    public boolean isTheSameItem(ChatItem otherItem) {
        if (otherItem instanceof Survey) {
            return this.sendingId == ((Survey) otherItem).sendingId;
        }
        return false;
    }

    @Override
    public Long getThreadId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Survey survey = (Survey) o;
        return sendingId == survey.sendingId &&
                phraseTimeStamp == survey.phraseTimeStamp &&
                ObjectsCompat.equals(questions, survey.questions) &&
                ObjectsCompat.equals(hideAfter, survey.hideAfter) &&
                sentState == survey.sentState;
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(sendingId, questions, hideAfter, phraseTimeStamp, sentState);
    }
}
