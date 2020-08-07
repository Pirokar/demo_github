package im.threads.internal.transport.models;

public class AttachmentSettings {

    private String clientId;
    private Content content;

    public String getClientId() {
        return clientId;
    }

    public Content getContent() {
        return content;
    }

    public static class Content {
        private int maxSize;
        private String[] fileExtensions;

        public Content() {
        }

        public Content(int maxSize, String[] fileExtensions) {
            this.maxSize = maxSize;
            this.fileExtensions = fileExtensions;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public String[] getFileExtensions() {
            return fileExtensions;
        }
    }
}
