package com.sequenia.threads.model;

import android.os.Message;

/**
 * Created by yuri on 24.06.2016.
 */
public abstract class DatabaseResponse<T> {
    private boolean isSuccessful;
    private String message;


    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public abstract void onComplete(T data);

    public abstract void onError(Throwable e, Message message);

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DatabaseResponse{" +
                "isSuccessful=" + isSuccessful +
                ", message='" + message + '\'' +
                '}';
    }
}
