package im.threads.internal.model;

public final class FileAndMediaItem implements MediaAndFileItem {
    private final FileDescription fileDescription;
    private final String fileName;

    public FileAndMediaItem(FileDescription fileDescription, String fileName) {
        this.fileDescription = fileDescription;
        this.fileName = fileName;
    }

    public FileDescription getFileDescription() {
        return fileDescription;
    }

    @Override
    public long getTimeStamp() {
        return fileDescription.getTimeStamp();
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "FileAndMediaItem{" +
                "fileDescription=" + fileDescription +
                '}';
    }
}
