package im.threads.internal.utils;

import android.util.Log;

import im.threads.internal.Config;

public final class ThreadsLogger {

    private ThreadsLogger() {
    }

    public static void v(String tag, String msg) {
        if (Config.instance.isDebugLoggingEnabled) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (Config.instance.isDebugLoggingEnabled) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (Config.instance.isDebugLoggingEnabled) {
            Log.d(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if (Config.instance.isDebugLoggingEnabled) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }
}
