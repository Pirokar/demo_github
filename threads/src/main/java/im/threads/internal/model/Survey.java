package im.threads.internal.model;

import android.annotation.SuppressLint;

import androidx.core.util.ObjectsCompat;

import java.util.List;

import io.reactivex.Observable;

public final class Survey implements ChatItem, Hidable {
    private String uuid;
    private long sendingId;
    private List<QuestionDTO> questions;
    private Long hideAfter;
    private long phraseTimeStamp;
    private boolean displayMessage;
    private MessageState sentState;
    private boolean read;

    public Survey(String uuid, long surveySendingId, Long hideAfter, long phraseTimeStamp,
                  MessageState messageState, boolean read, boolean displayMessage) {
        this.uuid = uuid;
        this.sendingId = surveySendingId;
        this.hideAfter = hideAfter;
        this.phraseTimeStamp = phraseTimeStamp;
        this.sentState = messageState;
        this.read = read;
        this.displayMessage = displayMessage;
    }

    public Survey(String uuid, long surveySendingId, long phraseTimeStamp, MessageState messageState, boolean read, boolean displayMessage) {
        this(uuid, surveySendingId, null, phraseTimeStamp, messageState, read, displayMessage);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getSendingId() {
        return sendingId;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    @SuppressLint("CheckResult")
    public void setQuestions(List<QuestionDTO> questions) {
        Observable.fromIterable(questions)
                .filter(question -> question != null)
                .toList()
                .subscribe(list -> this.questions = list);
    }


    public Long getHideAfter() {
        return hideAfter;
    }

    public MessageState getSentState() {
        return sentState;
    }

    public void setSentState(MessageState sentState) {
        this.sentState = sentState;
    }

    public long getPhraseTimeStamp() {
        return phraseTimeStamp;
    }

    public void setPhraseTimeStamp(long phraseTimeStamp) {
        this.phraseTimeStamp = phraseTimeStamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(boolean displayMessage) {
        this.displayMessage = displayMessage;
    }

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }

    @Override
    public boolean isTheSameItem(ChatItem otherItem) {
        if (otherItem instanceof Survey) {
            return ObjectsCompat.equals(this.sendingId, ((Survey) otherItem).sendingId);
        }
        return false;
    }

    public boolean isCompleted() {
        return sentState == MessageState.STATE_SENT || sentState == MessageState.STATE_WAS_READ;
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
