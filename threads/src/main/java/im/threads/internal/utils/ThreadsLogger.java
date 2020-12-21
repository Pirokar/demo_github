package im.threads.internal.utils;

import android.util.Log;

import im.threads.internal.Config;

public final class ThreadsLogger {

    private static final String PREFIX = "EDNA_THREADS_LIB: ";

    private ThreadsLogger() {
    }

    public static void v(String tag, String msg) {
        if (Config.instance.isDebugLoggingEnabled) {
            Log.v(tag, PREFIX + (msg != null ? msg : ""));
        }
    }

    public static void d(String tag, String msg) {
        if (Config.instance.isDebugLoggingEnabled) {
            Log.d(tag, PREFIX + (msg != null ? msg : ""));
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (Config.instance.isDebugLoggingEnabled) {
            Log.d(tag, PREFIX + (msg != null ? msg : ""), tr);
        }
    }

    public static void i(String tag, String msg) {
        if (Config.instance.isDebugLoggingEnabled) {
            Log.i(tag, PREFIX + (msg != null ? msg : ""));
        }
    }

    public static void w(String tag, String msg) {
        Log.w(tag, PREFIX + (msg != null ? msg : ""));
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(tag, PREFIX + (msg != null ? msg : ""), tr);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, PREFIX + (msg != null ? msg : ""));
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, PREFIX + (msg != null ? msg : ""), tr);
    }
}
