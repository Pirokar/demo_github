package im.threads.business.models;

import android.annotation.SuppressLint;

import androidx.core.util.ObjectsCompat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public final class Survey implements ChatItem, Hidable {
    private String uuid;
    private long sendingId;
    private List<QuestionDTO> questions;
    private Long hideAfter;
    private long phraseTimeStamp;
    private boolean displayMessage;
    private MessageStatus sentState;
    private boolean read;

    public Survey(String uuid, long surveySendingId, Long hideAfter, long phraseTimeStamp,
                  MessageStatus messageState, boolean read, boolean displayMessage) {
        this.uuid = uuid;
        this.sendingId = surveySendingId;
        this.hideAfter = hideAfter;
        this.phraseTimeStamp = phraseTimeStamp;
        this.sentState = messageState;
        this.read = read;
        this.displayMessage = displayMessage;
    }

    public Survey(String uuid, long sendingId, List<QuestionDTO> questions, Long hideAfter, long phraseTimeStamp,
                  boolean displayMessage, MessageStatus sentState, boolean read) {
        this.uuid = uuid;
        this.sendingId = sendingId;
        this.questions = questions;
        this.hideAfter = hideAfter;
        this.phraseTimeStamp = phraseTimeStamp;
        this.displayMessage = displayMessage;
        this.sentState = sentState;
        this.read = read;
    }

    public Survey(String uuid, long surveySendingId, long phraseTimeStamp, MessageStatus messageState, boolean read, boolean displayMessage) {
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
        return questions == null ? new ArrayList<>() : questions;
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

    public MessageStatus getSentState() {
        return sentState;
    }

    public void setSentState(MessageStatus sentState) {
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
            Survey other = ((Survey) otherItem);
            return ObjectsCompat.equals(this.sendingId, other.sendingId) && isQuestionsEquals(this.questions, other.questions);
        }
        return false;
    }

    public boolean isCompleted() {
        return (sentState == MessageStatus.SENT || sentState == MessageStatus.READ) && allQuestionsHasRate();
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

    public Survey copy() {
        return new Survey(uuid, sendingId, questions, hideAfter, phraseTimeStamp, displayMessage, sentState, read);
    }

    private boolean isQuestionsEquals(List<QuestionDTO> collection1, List<QuestionDTO> collection2) {
        boolean result = false;

        if (collection1.size() == collection2.size()) {
            for (QuestionDTO coll1Element : collection1) {
                result = false;
                for (QuestionDTO coll2Element : collection2) {
                    boolean ratesEquals = coll2Element.getRate() == coll1Element.getRate();
                    if (ratesEquals && coll2Element.getText().equals(coll1Element.getText())) {
                        result = true;
                        break;
                    }
                }
                if (!result) {
                    break;
                }
            }
        }

        return result;
    }

    private boolean allQuestionsHasRate() {
        boolean result = true;
        int questionsSize = questions.size();

        for (int i = 0; i < questionsSize && result; i++) {
            result = questions.get(i).hasRate();
        }

        return result;
    }
}
