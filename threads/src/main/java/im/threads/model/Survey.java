package im.threads.model;

import java.util.List;

/**
 * Created by Admin on 03.05.2017.
 */

public class Survey implements ChatItem {

    private String messageId;
    private long id;
    private long sendingId;
    private List<QuestionDTO> questions;
    private Long hideAfter;
    private long phraseTimeStamp;
    private MessageState sentState;

    public Survey() {

    }

    public Survey(long id, long surveySendingId, long hideAfter, String messageId, long phraseTimeStamp, MessageState messageState) {
        this.id = id;
        sendingId = surveySendingId;
        this.hideAfter = hideAfter;
        this.messageId = messageId;
        this.phraseTimeStamp = phraseTimeStamp;
        this.sentState = messageState;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

            if (sendingId != 0) {
                return sendingId == otherSurvey.sendingId;
            }

//            if (messageId != null && otherSurvey.messageId != null) {
//                return messageId.equals(otherSurvey.messageId);
//            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = Long.valueOf(sendingId).hashCode();
//        result = 31 * result + (messageId != null ? messageId.hashCode() : 0);
        return result;
    }
}
