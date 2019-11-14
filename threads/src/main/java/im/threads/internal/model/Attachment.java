package im.threads.internal.model;

public final class Attachment {
    private String result;
    private boolean isSelfie;
    private Optional optional;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Optional getOptional() {
        return optional;
    }

    public void setOptional(Optional optional) {
        this.optional = optional;
    }

    public boolean isSelfie() {
        return isSelfie;
    }

    public void setSelfie(boolean selfie) {
        isSelfie = selfie;
    }
}
