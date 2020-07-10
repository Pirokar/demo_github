package im.threads.internal.helpers;

import android.content.Context;

import java.io.File;

import im.threads.internal.utils.ThreadsLogger;

public final class FileHelper {

    private static String TAG = FileHelper.class.getSimpleName();

    private FileHelper() {
    }

    public static File createImageFile(Context context) {
        String filename = "thr" + System.currentTimeMillis() + ".jpg";
        File output = new File(context.getFilesDir(), filename);
        ThreadsLogger.d(TAG, "File genereated into filesDir : " + output.getAbsolutePath());
        return output;
    }
}
