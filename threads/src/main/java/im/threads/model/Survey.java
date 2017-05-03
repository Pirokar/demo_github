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

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }
}
