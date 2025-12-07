package emailclient.model;

public class Attachment {

    private int id;
    private final int messageId;
    private final String fileName;
    private final String filePath;
    private final long size;

    private Attachment(Builder b) {
        this.id = b.id;
        this.messageId = b.messageId;
        this.fileName = b.fileName;
        this.filePath = b.filePath;
        this.size = b.size;
    }

    public static class Builder {
        private int id;
        private int messageId;
        private String fileName;
        private String filePath;
        private long size;

        public Builder id(int id) { this.id = id; return this; }
        public Builder messageId(int messageId) { this.messageId = messageId; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder size(long size) { this.size = size; return this; }

        public Attachment build() {
            if (messageId <= 0)
                throw new IllegalStateException("messageId не може бути <= 0");
            if (fileName == null || fileName.isBlank())
                throw new IllegalStateException("fileName порожній");
            if (filePath == null || filePath.isBlank())
                throw new IllegalStateException("filePath порожній");
            if (size < 0)
                throw new IllegalStateException("size < 0");

            return new Attachment(this);
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMessageId() { return messageId; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public long getSize() { return size; }

    @Override
    public String toString() {
        return fileName;
    }

}
