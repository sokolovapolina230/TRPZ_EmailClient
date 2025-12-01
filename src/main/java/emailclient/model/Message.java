package emailclient.model;

import emailclient.model.enums.Importance;
import java.time.LocalDateTime;

public class Message {

    private int id;
    private int folderId;

    private String sender;
    private String recipient;
    private String subject;
    private String body;

    private boolean isRead;
    private Importance importance;

    private LocalDateTime dateSent;
    private boolean isDraft;


    public int getId() { return id; }
    public int getFolderId() { return folderId; }

    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }

    public boolean isRead() { return isRead; }
    public Importance getImportance() { return importance; }

    public LocalDateTime getDateSent() { return dateSent; }
    public boolean isDraft() { return isDraft; }


    public void setId(int id) { this.id = id; }
    public void setFolderId(int folderId) { this.folderId = folderId; }

    public void setSender(String sender) { this.sender = sender; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setBody(String body) { this.body = body; }

    public void setRead(boolean read) { isRead = read; }
    public void setImportance(Importance importance) { this.importance = importance; }

    public void setDateSent(LocalDateTime dateSent) { this.dateSent = dateSent; }
    public void setDraft(boolean draft) { isDraft = draft; }
}
