package com.sequenia.threads.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * helper filter to filter up pictures
 */
public class MyFileFilter implements FileFilter, FilenameFilter {
    private static final ArrayList<IgnoreCaseString> acceptedPicFormats = new ArrayList<>();
    private static final String TAG = "MyFileFilter";


    public MyFileFilter() {
        acceptedPicFormats.add(new IgnoreCaseString(".jpg"));
        acceptedPicFormats.add(new IgnoreCaseString(".jpeg"));
        acceptedPicFormats.add(new IgnoreCaseString(".png"));
        acceptedPicFormats.add(new IgnoreCaseString(".pdf"));
    }

    @Override
    public boolean accept(File file) {
        String filename = file.toString();

        int lastDotIndex = filename.lastIndexOf(".");
        if (file.isDirectory() || lastDotIndex == -1) {
            return false;
        }
        String ext = filename.substring(lastDotIndex);
        return acceptedPicFormats.contains(new IgnoreCaseString(ext));
    }

    @Override
    public boolean accept(File dir, String filename) {
        if (dir == null || filename == null) {
            return false;
        }
        File f = new File(filename);
        int lastDotIndex = filename.lastIndexOf(".");
        String ext = filename.substring(lastDotIndex);
        if (f.isDirectory() || lastDotIndex == -1) {
            return false;
        }
        return acceptedPicFormats.contains(new IgnoreCaseString(ext));
    }

    private class IgnoreCaseString {
        private String str;

        public IgnoreCaseString(String str) {
            this.str = str;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IgnoreCaseString that = (IgnoreCaseString) o;

            return !(str != null ? !str.equalsIgnoreCase(that.str) : that.str != null);

        }

        @Override
        public int hashCode() {
            return str != null ? str.hashCode() : 0;
        }
    }
}
