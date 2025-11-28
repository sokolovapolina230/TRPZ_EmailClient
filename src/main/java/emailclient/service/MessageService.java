package emailclient.service;

import emailclient.model.Message;
import emailclient.model.enums.Importance;
import emailclient.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.List;

public class MessageService {

    private final MessageRepository repo = new MessageRepository();

    // Чернетки
    public int createDraft(int draftFolderId, String sender, String recipient, String subject, String body, Importance importance) {
        Message draft = new Message.Builder()
                .folderId(draftFolderId)
                .sender(sender)
                .recipient(recipient)
                .subject(subject)
                .body(body)
                .importance(importance)
                .isDraft(true)
                .build();
        return repo.add(draft);
    }

    public void updateDraft(int messageId, String subject, String body, String recipient, Importance importance) {
        Message draft = requireMessage(messageId);
        if (!draft.isDraft()) throw new IllegalStateException("Це не чернетка");
        draft.updateDraft(subject, body, recipient);
        draft.markAsImportant(importance);
        repo.update(draft);
    }

    public void markDraftAsSent(int messageId, int sentFolderId) {
        Message draft = requireMessage(messageId);
        if (!draft.isDraft()) throw new IllegalStateException("Це не чернетка");
        draft.markAsSent();
        draft.moveToFolder(sentFolderId);
        repo.update(draft);
    }

    // Вхідні / Вихідні
    public int createIncoming(int inboxFolderId, String sender, String subject, String body) {
        Message incoming = new Message.Builder()
                .folderId(inboxFolderId)
                .sender(sender)
                .recipient(null)
                .subject(subject)
                .body(body)
                .isDraft(false)
                .isRead(false)
                .importance(Importance.NORMAL)
                .dateSent(LocalDateTime.now())
                .build();
        return repo.add(incoming);
    }

    public int sendNow(int sentFolderId, String sender, String recipient, String subject, String body, Importance importance) {
        Message outgoing = new Message.Builder()
                .folderId(sentFolderId)
                .sender(sender)
                .recipient(recipient)
                .subject(subject)
                .body(body)
                .importance(importance)
                .isDraft(false)
                .isRead(true)
                .dateSent(LocalDateTime.now())
                .build();
        return repo.add(outgoing);
    }

    // Операції
    public void markAsRead(int messageId) {
        Message msg = requireMessage(messageId);
        msg.markAsRead();
        repo.update(msg);
    }

    public void setImportance(int messageId, Importance level) {
        Message msg = requireMessage(messageId);
        msg.markAsImportant(level);
        repo.update(msg);
    }

    public void move(int messageId, int targetFolderId) {
        Message msg = requireMessage(messageId);
        msg.moveToFolder(targetFolderId);
        repo.update(msg);
    }

    public void delete(int messageId) {
        repo.delete(messageId);
    }

    // Запити
    public List<Message> getMessages(int folderId) {
        return repo.getByFolderId(folderId);
    }

    public Message getById(int messageId) {
        return repo.getById(messageId);
    }

    // Helpers
    private Message requireMessage(int id) {
        Message m = repo.getById(id);
        if (m == null) throw new IllegalArgumentException("Повідомлення не знайдено: id=" + id);
        return m;
    }
}
