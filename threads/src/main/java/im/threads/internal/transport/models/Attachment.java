package im.threads.internal.transport.models;

public class Attachment {
    private long id;
    private String result;
    private String name;
    private long size;
    private boolean isSelfie;

    public long getId() {
        return id;
    }

    public String getResult() {
        return result;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public boolean isSelfie() {
        return isSelfie;
    }
}
