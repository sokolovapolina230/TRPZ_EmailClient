package emailclient.model;

import emailclient.model.enums.Importance;
import java.time.LocalDateTime;

public class Message {

    private int id;                 // встановлюється репозиторієм
    private int folderId;           // змінюється через moveToFolder()
    private final String sender;    // незмінний

    private String recipient;       // редагується в чернетці
    private String subject;         // редагується в чернетці
    private String body;            // редагується в чернетці (без trim!)

    private boolean isRead;
    private boolean isDraft;
    private Importance importance;
    private LocalDateTime dateSent; // null для чернеток

    private Message(Builder b) {
        this.id         = b.id;
        this.folderId   = b.folderId;
        this.sender     = b.sender;
        this.recipient  = b.recipient;
        this.subject    = b.subject;
        this.body       = b.body;
        this.isRead     = b.isRead;
        this.isDraft    = b.isDraft;
        this.importance = b.importance;
        this.dateSent   = b.dateSent;
    }

    public static class Builder {
        private int id;
        private int folderId;
        private String sender;
        private String recipient;
        private String subject;
        private String body;
        private boolean isRead = false;
        private boolean isDraft = true;
        private Importance importance = Importance.NORMAL;
        private LocalDateTime dateSent = null; // дефолт для чернеток

        public Builder id(int id) {
            this.id = id;
            return this;
        }
        public Builder folderId(int folderId) {
            this.folderId = folderId;
            return this;
        }

        public Builder sender(String sender) {
            this.sender = (sender != null) ? sender.trim() : null;
            return this;
        }
        public Builder recipient(String recipient) {
            this.recipient = (recipient != null) ? recipient.trim() : null;
            return this;
        }
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder isRead(boolean read) {
            this.isRead = read;
            return this;
        }
        public Builder isDraft(boolean draft) {
            this.isDraft = draft;
            return this;
        }
        public Builder importance(Importance importance) {
            this.importance = (importance != null) ? importance : Importance.NORMAL;
            return this;
        }
        public Builder dateSent(LocalDateTime date) { this.dateSent = date;
            return this;
        }

        public Message build() {
            if (folderId <= 0) throw new IllegalStateException("folderId must be > 0");
            if (sender == null || sender.isBlank()) throw new IllegalStateException("sender is required");
            return new Message(this);
        }
    }

    // Бізнес-методи
    public void updateDraft(String subject, String body, String recipient) {
        if (!isDraft) throw new IllegalStateException("Редагувати можна лише чернетку");
        this.subject = (subject != null) ? subject.trim() : null;
        this.body = body; // без trim
        this.recipient = (recipient != null) ? recipient.trim() : null;
    }

    public void markAsSent() {
        if (!isDraft) throw new IllegalStateException("Вже надіслано");
        this.isDraft = false;
        this.isRead  = true;
        this.dateSent = LocalDateTime.now();
    }

    public void markAsRead() { this.isRead = true; }
    public void markAsUnread() { this.isRead = false; }
    public void markAsImportant(Importance level) { this.importance = level; }

    public void moveToFolder(int targetFolderId) {
        if (targetFolderId <= 0) throw new IllegalArgumentException("folderId must be > 0");
        this.folderId = targetFolderId;
    }

    // Getters / для repo setId
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFolderId() { return folderId; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public boolean isRead() { return isRead; }
    public boolean isDraft() { return isDraft; }
    public Importance getImportance() { return importance; }
    public LocalDateTime getDateSent() { return dateSent; }
}
