package im.threads.model;

/**
 * Created by Admin on 03.05.2017.
 */

public class QuestionDTO implements ChatItem {

    private long id;
    private long sendingId;
    private String text;
    private int scale;
    private boolean simple;
    private Integer rate;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public void setPhraseTimeStamp(long phraseTimeStamp) {
        this.phraseTimeStamp = phraseTimeStamp;
    }

    @Override
    public long getTimeStamp() {
        return phraseTimeStamp;
    }
}
