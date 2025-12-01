package emailclient.service;

import emailclient.model.Account;
import emailclient.model.Message;
import emailclient.model.enums.Importance;
import emailclient.model.enums.ProtocolType;
import emailclient.repository.AccountRepository;
import emailclient.service.protocol.*;

import java.io.File;
import java.util.List;

public class MailService {

    private final AccountRepository accountRepo = new AccountRepository();
    private final MessageService messageService = new MessageService();
    private final AttachmentService attachmentService = new AttachmentService();
    private final SmtpStrategy smtpStrategy = new SmtpStrategy();

    private final MailProtocol protocolStrategy;
    private final Account account;

    public MailService(int accountId) {

        Account a = accountRepo.getById(accountId);
        if (a == null)
            throw new IllegalArgumentException("Акаунт не знайдено: id=" + accountId);

        this.account = a;

        if (a.getProtocol() == ProtocolType.IMAP)
            this.protocolStrategy = new ImapStrategy();
        else
            this.protocolStrategy = new Pop3Strategy();
    }

    public Account getAccount() {
        return account;
    }

    public List<Message> syncInbox(int inboxFolderId) {
        try {
            protocolStrategy.connect(account);

            List<Message> msgs = protocolStrategy.fetchMessages(account.getId(), inboxFolderId);

            protocolStrategy.disconnect();
            return msgs;

        } catch (Exception e) {
            throw new RuntimeException("Помилка синхронізації пошти: " + e.getMessage(), e);
        }
    }

    public int createDraft(int draftFolderId,
                           String sender,
                           String recipient,
                           String subject,
                           String body,
                           Importance importance,
                           List<File> attachments) {

        int draftId = messageService.createDraft(
                draftFolderId,
                sender,
                recipient,
                subject,
                body,
                importance
        );

        if (attachments != null) {
            for (File f : attachments)
                attachmentService.addAttachment(draftId, f);
        }

        return draftId;
    }

    public void updateDraft(int messageId,
                            String subject,
                            String body,
                            String recipient,
                            Importance importance,
                            List<File> attachments) {

        messageService.updateDraft(messageId, subject, body, recipient, importance);

        if (attachments != null) {
            for (File f : attachments)
                attachmentService.addAttachment(messageId, f);
        }
    }

    public int sendMessage(int sentFolderId,
                           String sender,
                           String recipient,
                           String subject,
                           String body,
                           Importance importance,
                           List<File> attachments) {

        // створюємо лист у SENT
        int messageId = messageService.createIncoming(
                sentFolderId,
                sender,
                recipient,
                subject,
                body
        );

        // Вкладення
        if (attachments != null) {
            for (File f : attachments)
                attachmentService.addAttachment(messageId, f);
        }

        // SMTP
        try {
            smtpStrategy.connect(account);
            smtpStrategy.send(sender, recipient, subject, body);
            smtpStrategy.disconnect();
        } catch (Exception e) {
            attachmentService.deleteAllByMessageId(messageId);
            messageService.delete(messageId);
            throw new RuntimeException("SMTP помилка: " + e.getMessage(), e);
        }

        return messageId;
    }

    public void sendDraft(int messageId, int sentFolderId, List<File> attachments) {

        Message draft = messageService.getById(messageId);
        if (draft == null)
            throw new IllegalArgumentException("Чернетку не знайдено");

        if (attachments != null) {
            for (File f : attachments)
                attachmentService.addAttachment(messageId, f);
        }

        // SMTP
        smtpStrategy.connect(account);
        smtpStrategy.send(draft.getSender(), draft.getRecipient(), draft.getSubject(), draft.getBody());
        smtpStrategy.disconnect();

        // переносимо у SENT
        messageService.markDraftAsSent(messageId, sentFolderId);
    }

    public void deleteDraft(int messageId) {
        attachmentService.deleteAllByMessageId(messageId);
        messageService.delete(messageId);
    }

    public List<emailclient.model.Attachment> getAttachments(int messageId) {
        return attachmentService.getAttachments(messageId);
    }
}
