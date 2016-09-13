package com.sequenia.threads.model;

/**
 * Created by yuri on 10.06.2016.
 */
public enum MessageState {
    STATE_SENDING(0), STATE_SENT(1), STATE_WAS_READ(2), STATE_NOT_SENT(3);
    private int num;

    MessageState(int i) {
        this.num = i;
    }

    public int getType() {
        return num;
    }

}
