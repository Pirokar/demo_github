package im.threads.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;

public class FileHelper {

    public static File createImageFile(Context context) {

        String filename = "thr" + System.currentTimeMillis() + ".jpg";
        File output = null;
        try {

            output = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    filename);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                output = new File(context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).dataDir + File.separator + filename);
            } catch (PackageManager.NameNotFoundException e1) {
                e1.printStackTrace();
            }

        }
        if (output == null) {
            output = new File(context.getFilesDir(), filename);
        }

        return output;
    }

}
