package im.threads.model;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by Admin on 03.05.2017.
 */

public class Survey implements ChatItem {

    private String uuid;
    private long sendingId;
    private List<QuestionDTO> questions;
    private Long hideAfter;
    private long phraseTimeStamp;
    private MessageState sentState;

    public Survey(Long surveySendingId, Long hideAfter, String uuid, long phraseTimeStamp, MessageState messageState) {
        this.sendingId = surveySendingId;
        this.hideAfter = hideAfter;
        this.uuid = uuid;
        this.phraseTimeStamp = phraseTimeStamp;
        this.sentState = messageState;
    }

    public Survey(Long surveySendingId, String uuid, long phraseTimeStamp, MessageState messageState) {
        this(surveySendingId, null, uuid, phraseTimeStamp, messageState);
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

            if (!TextUtils.isEmpty(uuid)) {
                return uuid.equals(otherSurvey.uuid);
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
