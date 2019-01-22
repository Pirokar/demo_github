package im.threads.helpers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import im.threads.model.ChatStyle;

public class FileHelper {

    private static String LOG_TAG = FileHelper.class.getSimpleName();

    public static File createImageFile(Context context) {

        String filename = "thr" + System.currentTimeMillis() + ".jpg";
        File output = null;

        try {
            output = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    filename);
            if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                Log.d(LOG_TAG, "File genereated into ExternalStoragePublicDirectory, DCIM : " + output.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "Could not create file in public storage");
            if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                Log.d(LOG_TAG, "", e);
            }
        }

        if (output == null) {
            output = new File(context.getFilesDir(), filename);
            if (ChatStyle.getInstance().isDebugLoggingEnabled) {
                Log.d(LOG_TAG, "File genereated into filesDir : " + output.getAbsolutePath());
            }
        }

        return output;
    }

    public static boolean isThreadsImage(File file) {
        return file.getName().startsWith("thr") && file.getName().endsWith(".jpg");
    }

}
