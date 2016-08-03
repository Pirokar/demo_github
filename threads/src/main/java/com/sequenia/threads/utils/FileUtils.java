package com.sequenia.threads.utils;

/**
 * Created by yuri on 01.08.2016.
 */
public class FileUtils {
    public static final int JPEG = 0;
    public static final int PNG = 1;
    public static final int PDF = 2;
    public static final int UNKNOWN = -1;

    private FileUtils() {
    }

    public static int getExtensionFromPath(String path) {
        if (path == null || !path.contains(".")) return UNKNOWN;
        String extension = path.substring(path.lastIndexOf(".") + 1);
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) return JPEG;
        if (extension.equalsIgnoreCase("png")) return PNG;
        if (extension.equalsIgnoreCase("pdf")) return PDF;
        return UNKNOWN;
    }

    public static String getLastPathSegment(String path) {
        if (path == null || !path.contains("/")) return null;
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
