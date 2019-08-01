package im.threads.internal.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by broomaservice on 07/05/2018.
 */

public class ThreadUtils {

    public static void runOnUiThread (Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

}
