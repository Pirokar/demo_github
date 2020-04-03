package im.threads.internal.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

import im.threads.internal.Config;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.model.FileDescription;

public final class FileUtils {
    private static final int JPEG = 0;
    private static final int PNG = 1;
    private static final int PDF = 2;
    private static final int OTHER_DOC_FORMATS = 3;
    private static final int UNKNOWN = -1;

    private FileUtils() {
    }

    public static String getLastPathSegment(@Nullable String path) {
        if (path == null || !path.contains("/")) {
            return null;
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String convertRelativeUrlToAbsolute(@Nullable String relativeUrl) {
        if (TextUtils.isEmpty(relativeUrl) || relativeUrl.startsWith("http")) {
            return relativeUrl;
        }
        return MetaDataUtils.getDatastoreUrl(Config.instance.context) + "files/" + relativeUrl;
    }

    public static boolean isSupportedFile(@Nullable final FileDescription fileDescription) {
        return FileUtils.isImage(fileDescription) || FileUtils.isDoc(fileDescription);
    }

    public static boolean isImage(@Nullable final FileDescription fileDescription) {
        return fileDescription != null
                && (getExtensionFromPath(fileDescription.getFilePath()) == JPEG
                || getExtensionFromPath(fileDescription.getFilePath()) == PNG
                || getExtensionFromPath(fileDescription.getIncomingName()) == PNG
                || getExtensionFromPath(fileDescription.getIncomingName()) == JPEG);
    }

    public static boolean isDoc(@Nullable final FileDescription fileDescription) {
        return fileDescription != null
                && (getExtensionFromPath(fileDescription.getFilePath()) == PDF
                || getExtensionFromPath(fileDescription.getFilePath()) == OTHER_DOC_FORMATS
                || getExtensionFromPath(fileDescription.getIncomingName()) == PDF
                || getExtensionFromPath(fileDescription.getIncomingName()) == OTHER_DOC_FORMATS);
    }

    private static int getExtensionFromPath(@Nullable String path) {
        if (path == null || !path.contains(".")) {
            return UNKNOWN;
        }
        String extension = path.substring(path.lastIndexOf(".") + 1);
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
            return JPEG;
        }
        if (extension.equalsIgnoreCase("png")) {
            return PNG;
        }
        if (extension.equalsIgnoreCase("pdf")) {
            return PDF;
        }
        if (extension.equalsIgnoreCase("txt")
                || extension.equalsIgnoreCase("doc")
                || extension.equalsIgnoreCase("docx")
                || extension.equalsIgnoreCase("xls")
                || extension.equalsIgnoreCase("xlsx")
                || extension.equalsIgnoreCase("xlsm")
                || extension.equalsIgnoreCase("xltx")
                || extension.equalsIgnoreCase("xlt")) {
            return OTHER_DOC_FORMATS;
        }
        return UNKNOWN;
    }

    @NonNull
    public static String getMimeType(File file) {
        Context context = Config.instance.context;
        String type;
        type = context.getContentResolver().getType(FileProviderHelper.getUriForFile(context, file));
        return type != null ? type : "*/*";
    }
}
