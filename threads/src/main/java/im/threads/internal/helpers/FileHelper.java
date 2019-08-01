package im.threads.internal.helpers;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import im.threads.internal.utils.ThreadsLogger;

public class FileHelper {

    private static String TAG = FileHelper.class.getSimpleName();

    public static File createImageFile(Context context) {

        String filename = "thr" + System.currentTimeMillis() + ".jpg";
        File output = null;

        try {
            output = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    filename);
            ThreadsLogger.d(TAG, "File genereated into ExternalStoragePublicDirectory, DCIM : " + output.getAbsolutePath());
        } catch (Exception e) {
            ThreadsLogger.w(TAG, "Could not create file in public storage");
            ThreadsLogger.d(TAG, "", e);
        }

        if (output == null) {
            output = new File(context.getFilesDir(), filename);
            ThreadsLogger.d(TAG, "File genereated into filesDir : " + output.getAbsolutePath());
        }

        return output;
    }

    public static boolean isThreadsImage(File file) {
        return file.getName().startsWith("thr") && file.getName().endsWith(".jpg");
    }

}
