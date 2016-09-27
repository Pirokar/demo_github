package com.sequenia.threads.utils;

import android.text.TextUtils;

import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.Quote;

/**
 * Created by yuri on 01.08.2016.
 */
public class FileUtils {
    public static final int JPEG = 0;
    public static final int PNG = 1;
    public static final int PDF = 2;
    public static final int OTHER_DOC_FORMATS = 3;
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
        return UNKNOWN;
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


}
