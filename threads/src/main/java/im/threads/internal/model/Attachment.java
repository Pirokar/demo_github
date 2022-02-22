package im.threads.internal.model;

public final class Attachment {
    private String result;
    private boolean isSelfie;
    private Optional optional;
    private AttachmentStateEnum state = AttachmentStateEnum.ANY;
    private ErrorStateEnum errorCode = ErrorStateEnum.ANY;
    private String errorMessage = "";

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

    public AttachmentStateEnum getState() {
        return state;
    }

    public void setState(AttachmentStateEnum state) {
        this.state = state;
    }

    public ErrorStateEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorStateEnum errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}