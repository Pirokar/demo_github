package im.threads.utils;

/**
 * Created by yuri on 03.08.2016.
 */
public abstract class Callback<R,E extends Throwable> {
    public abstract void onSuccess(R result);
    public abstract void onFail(E error);
}
