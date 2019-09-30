package im.threads.internal.helpers;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

public final class FileProviderHelper {

    private static final String AUTHORITY_POSTFIX = ".im.threads.fileprovider";

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getPackageName() + AUTHORITY_POSTFIX,
                file);
    }
}
