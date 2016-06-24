package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public enum MessageState {
    STATE_SENT(1), STATE_SENT_AND_SERVER_RECEIVED(2), STATE_NOT_SENT(3);
    private int num;

    MessageState(int i) {
        this.num = i;
    }
    public int getType(){
        return num;
    }

}
