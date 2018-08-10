package im.threads.utils;

import android.util.Log;

public class LogUtils {

    private static final String DEV_TAG = "DEVELOP";

    public static void logDev(String message) {
        Log.w(DEV_TAG, message);
    }

}
