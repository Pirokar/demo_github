package im.threads.internal.model;

/**
 * Created by Admin on 28.03.2017.
 */

public class FileUploadResponse {
    private String result;
    private FileUploadResponseOptional optional;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public FileUploadResponseOptional getOptional() {
        return optional;
    }

    public void setOptional(FileUploadResponseOptional optional) {
        this.optional = optional;
    }
}
