package im.threads.model;

/**
 * Created by Admin on 28.03.2017.
 */

class FileUploadResponseOptional {
    private String name;
    private String type;
    private Long size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}