package im.threads.utils;

import android.content.Context;
import android.text.TextUtils;

import im.threads.model.FileDescription;
import im.threads.model.Quote;

/**
 * Created by yuri on 01.08.2016.
 */
public class FileUtils {
    public static final int JPEG = 0;
    public static final int PNG = 1;
    public static final int PDF = 2;
    public static final int OTHER_DOC_FORMATS = 3;
    public static final int OTHER_UNKNOWNS_FORMAT = 4;
    public static final int UNKNOWN = -1;

    private FileUtils() {
    }

    public static int getExtensionFromPath(String path) {
        if (path == null || !path.contains(".")) return UNKNOWN;
        String extension = path.substring(path.lastIndexOf(".") + 1);
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) return JPEG;
        if (extension.equalsIgnoreCase("png")) return PNG;
        if (extension.equalsIgnoreCase("pdf")) return PDF;
        if (extension.equalsIgnoreCase("doc")
                || extension.equalsIgnoreCase("docx")
                || extension.equalsIgnoreCase("xls")
                || extension.equalsIgnoreCase("xlsx")
                || extension.equalsIgnoreCase("ppt")
                || extension.equalsIgnoreCase("pptx")) return OTHER_DOC_FORMATS;
        return OTHER_UNKNOWNS_FORMAT;
    }

    public static int getExtensionFromQuote(Quote quote) {
        if (quote == null
                || quote.getFileDescription() == null)
            return UNKNOWN;
        String path = quote.getFileDescription().getIncomingName();
        if (TextUtils.isEmpty(path)) path = quote.getFileDescription().getFilePath();
        String extension = path.substring(path.lastIndexOf(".") + 1);
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) return JPEG;
        if (extension.equalsIgnoreCase("png")) return PNG;
        if (extension.equalsIgnoreCase("pdf")) return PDF;
        if (extension.equalsIgnoreCase("doc")
                || extension.equalsIgnoreCase("docx")
                || extension.equalsIgnoreCase("xls")
                || extension.equalsIgnoreCase("xlsx")
                || extension.equalsIgnoreCase("ppt")
                || extension.equalsIgnoreCase("pptx")) return OTHER_DOC_FORMATS;
        return UNKNOWN;
    }

    public static int getExtensionFromFileDescription(FileDescription fileDescription) {
        if (fileDescription == null || (fileDescription.getIncomingName() == null && fileDescription.getFilePath() == null))
            return UNKNOWN;
        if (getExtensionFromPath(fileDescription.getFilePath()) == UNKNOWN && getExtensionFromPath(fileDescription.getIncomingName()) == UNKNOWN)
            return UNKNOWN;

        return getExtensionFromPath(fileDescription.getFilePath()) != UNKNOWN ? getExtensionFromPath(fileDescription.getFilePath()) : getExtensionFromPath(fileDescription.getIncomingName());
    }

    public static String getLastPathSegment(String path) {
        if (path == null || !path.contains("/")) return null;
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String convertRelativeUrlToAbsolute(Context context, String relativeUrl) {
        if (TextUtils.isEmpty(relativeUrl) || relativeUrl.startsWith("http")) {
            return relativeUrl;
        }
        return PrefUtils.getServerUrlMetaInfo(context) + "files/" + relativeUrl;
    }

    public static boolean isImage(final FileDescription fileDescription) {
        return fileDescription != null
                && (getExtensionFromPath(fileDescription.getFilePath()) == JPEG
                || getExtensionFromPath(fileDescription.getFilePath()) == PNG
                || getExtensionFromPath(fileDescription.getIncomingName()) == PNG
                || getExtensionFromPath(fileDescription.getIncomingName()) == JPEG);
    }
}
