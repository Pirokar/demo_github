package im.threads.internal.utils;

import android.os.Handler;
import android.os.Looper;

public final class ThreadUtils {
    public static void runOnUiThread (Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }
}
