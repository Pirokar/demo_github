package im.threads.internal.utils;

public abstract class Callback<R,E extends Throwable> {
    public abstract void onSuccess(R result);
    public abstract void onError(E error);
}
