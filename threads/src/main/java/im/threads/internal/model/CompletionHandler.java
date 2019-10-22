package im.threads.internal.model;

public abstract class CompletionHandler<T> {
    private boolean isSuccessful;
    private String message;

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public abstract void onComplete(T data);

    public abstract void onError(Throwable e, String message, T data);

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "CompletionHandler{" +
                "isSuccessful=" + isSuccessful +
                ", message='" + message + '\'' +
                '}';
    }
}
