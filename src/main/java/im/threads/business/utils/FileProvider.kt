package im.threads.business.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

public final class FileProviderHelper {

    private static final String AUTHORITY_POSTFIX = ".im.threads.fileprovider";

    public static Uri getUriForFile(@NonNull Context context, @NonNull File file) {
        return FileProvider.getUriForFile(context,
                context.getPackageName() + AUTHORITY_POSTFIX,
                file);
    }
}
