package im.threads.helpers;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import androidx.core.content.FileProvider;

public class FileProviderHelper {

    private static String AUTHORITY_POSTFIX = ".im.threads.fileprovider";

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getPackageName() + AUTHORITY_POSTFIX,
                file);
    }
}
