package emailclient.service;

import emailclient.model.Message;
import emailclient.model.enums.Importance;
import emailclient.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;

public class MessageService {

    private final MessageRepository repo = new MessageRepository();

    public List<Message> getMessages(int folderId) {
        return repo.getByFolder(folderId);
    }

    public Message getById(int id) {
        return repo.getById(id);
    }

    public int createIncoming(int folderId,
                              String sender,
                              String recipient,
                              String subject,
                              String body) {

        Message m = new Message();
        m.setSender(sender);
        m.setRecipient(recipient);
        m.setSubject(subject);
        m.setBody(body);
        m.setImportance(Importance.NORMAL);
        m.setRead(false);
        m.setDraft(false);
        m.setDateSent(LocalDateTime.now());

        return repo.createIncoming(folderId, m);
    }

    public int createDraft(int folderId,
                           String sender,
                           String recipient,
                           String subject,
                           String body,
                           Importance importance) {

        Message m = new Message();
        m.setSender(sender);
        m.setRecipient(recipient);
        m.setSubject(subject);
        m.setBody(body);
        m.setImportance(importance);
        m.setDraft(true);

        return repo.createDraft(folderId, m);
    }

    public void updateDraft(int id,
                            String subject,
                            String body,
                            String recipient,
                            Importance importance) {

        repo.updateDraft(id, subject, body, recipient, importance);
    }

    public void markDraftAsSent(int id, int sentFolderId) {
        repo.markDraftAsSent(id, sentFolderId);
    }

    public void updateFolder(int messageId, int newFolderId) {
        repo.updateFolder(messageId, newFolderId);
    }

    public void copyMessage(int sourceId, int targetFolderId) {

        // Отримуємо оригінал
        Message original = repo.getById(sourceId);
        if (original == null) {
            throw new IllegalArgumentException("Повідомлення не знайдено");
        }

        // Копіюємо запис у таблиці messages
        int newMessageId = repo.copyMessage(original, targetFolderId);

        // Копіюємо вкладення
        AttachmentService attachmentService = new AttachmentService();
        List<emailclient.model.Attachment> attachments =
                attachmentService.getAttachments(sourceId);

        for (emailclient.model.Attachment a : attachments) {
            attachmentService.copyAttachment(a, newMessageId);
        }

    }


    public void updateReadStatus(int id, boolean read) {
        repo.setRead(id, read);
    }

    public void delete(int id) {
        repo.delete(id);
    }
}
