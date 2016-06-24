package com.sequenia.threads.model;

/**
 * Created by yuri on 24.06.2016.
 */
public abstract class DatabaseResponse<T> {
    private boolean isSuccessful;
    private T data;
    private String message;


    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public abstract void onComplete(T data);

    public abstract void onError();

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DatabaseResponse{" +
                "isSuccessful=" + isSuccessful +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
