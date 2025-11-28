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
        // краще напряму getById
        Account a = accountRepo.getById(accountId);
        if (a == null) {
            throw new IllegalArgumentException("Акаунт не знайдено: id=" + accountId);
        }
        this.account = a;

        if (account.getProtocol() == ProtocolType.IMAP) {
            this.protocolStrategy = new ImapStrategy();
        } else {
            this.protocolStrategy = new Pop3Strategy();
        }
    }

    public Account getAccount() {
        return account;
    }

    // -------------------- INBOX SYNC --------------------
    public List<Message> syncInbox(int inboxFolderId) {
        try {
            protocolStrategy.connect(account);
            try {
                return protocolStrategy.fetchMessages(account.getId(), inboxFolderId);
            } finally {
                protocolStrategy.disconnect();
            }
        } catch (Exception e) {
            throw new RuntimeException("Помилка синхронізації: " + e.getMessage(), e);
        }
    }

    // -------------------- DRAFTS --------------------
    public int createDraft(int draftFolderId,
                           String sender,
                           String recipient,
                           String subject,
                           String body,
                           Importance importance,
                           List<File> attachments) {

        int draftId = messageService.createDraft(draftFolderId, sender, recipient, subject, body, importance);

        if (attachments != null && !attachments.isEmpty()) {
            try {
                for (File f : attachments) {
                    attachmentService.addAttachment(draftId, f);
                }
            } catch (Exception ex) {
                // rollback
                messageService.delete(draftId);
                throw new RuntimeException("Помилка додавання вкладень до чернетки", ex);
            }
        }
        return draftId;
    }

    public void updateDraft(int messageId,
                            String subject,
                            String body,
                            String recipient,
                            Importance importance,
                            List<File> newAttachments) {

        messageService.updateDraft(messageId, subject, body, recipient, importance);

        if (newAttachments != null && !newAttachments.isEmpty()) {
            for (File f : newAttachments) {
                attachmentService.addAttachment(messageId, f);
            }
        }
    }

    public void sendDraft(int messageId, int sentFolderId, List<File> newAttachments) {
        Message draft = messageService.getById(messageId);
        if (draft == null) throw new IllegalArgumentException("Повідомлення не знайдено: id=" + messageId);
        if (!draft.isDraft()) throw new IllegalStateException("Це не чернетка: id=" + messageId);

        // додаємо нові вкладення (якщо є)
        if (newAttachments != null) {
            for (File f : newAttachments) {
                attachmentService.addAttachment(messageId, f);
            }
        }

        try {
            smtpStrategy.connect(account);
            try {
                smtpStrategy.send(
                        draft.getSender(),
                        draft.getRecipient(),
                        draft.getSubject(),
                        draft.getBody()
                );
            } finally {
                smtpStrategy.disconnect();
            }
        } catch (Exception e) {
            throw new RuntimeException("Помилка SMTP надсилання: " + e.getMessage(), e);
        }

        // після успішного SMTP — оновлюємо статус
        try {
            messageService.markDraftAsSent(messageId, sentFolderId);
        } catch (Exception e) {
            // лист уже надіслано — повідомимо про проблему апдейта
            throw new RuntimeException("Лист надіслано, але не вдалося оновити статус у БД", e);
        }
    }

    // -------------------- SEND NEW --------------------
    public int sendMessage(int sentFolderId,
                           String sender,
                           String recipient,
                           String subject,
                           String body,
                           Importance importance,
                           List<File> attachments) {

        // 1) локально зберігаємо в SENT (щоб мати id)
        int messageId = messageService.sendNow(sentFolderId, sender, recipient, subject, body, importance);

        // 2) додаємо вкладення (rollback при збої)
        if (attachments != null && !attachments.isEmpty()) {
            try {
                for (File f : attachments) {
                    attachmentService.addAttachment(messageId, f);
                }
            } catch (Exception e) {
                // відкат: видаляємо все
                attachmentService.deleteAllByMessageId(messageId);
                messageService.delete(messageId);
                throw new RuntimeException("Помилка додавання вкладень", e);
            }
        }

        // 3) SMTP — останнім
        try {
            smtpStrategy.connect(account);
            try {
                smtpStrategy.send(sender, recipient, subject, body);
            } finally {
                smtpStrategy.disconnect();
            }
            return messageId;
        } catch (Exception e) {
            // відкат локального
            attachmentService.deleteAllByMessageId(messageId);
            messageService.delete(messageId);
            throw new RuntimeException("Помилка SMTP надсилання: " + e.getMessage(), e);
        }
    }

    // -------------------- Attachments ops passthrough --------------------
    public void deleteAttachment(int attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
    }

    public List<emailclient.model.Attachment> getAttachments(int messageId) {
        return attachmentService.getAttachments(messageId);
    }

    // -------------------- Delete draft --------------------
    public void deleteDraft(int messageId) {
        Message m = messageService.getById(messageId);
        if (m == null) throw new IllegalArgumentException("Повідомлення не знайдено: id=" + messageId);
        if (!m.isDraft()) throw new IllegalStateException("Це не чернетка: id=" + messageId);

        attachmentService.deleteAllByMessageId(messageId);
        messageService.delete(messageId);
    }
}
