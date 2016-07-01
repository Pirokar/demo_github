package com.sequenia.threads.model;

/**
 * Created by yuri on 01.07.2016.
 */
public class FileAndMediaItem implements MediaAndFileItem {
    private final   FileDescription fileDescription;

    public FileAndMediaItem(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
    }

    public FileDescription getFileDescription() {
        return fileDescription;
    }

    @Override
    public long getTimeStamp() {
        return fileDescription.getTimeStamp();
    }

    @Override
    public String toString() {
        return "FileAndMediaItem{" +
                "fileDescription=" + fileDescription +
                '}';
    }
}
