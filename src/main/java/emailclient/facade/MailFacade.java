package emailclient.facade;

import emailclient.model.*;
import emailclient.model.enums.Importance;
import emailclient.repository.MessageRepository;
import emailclient.service.*;

import java.io.File;
import java.util.List;

public class MailFacade {

    private static final MailFacade INSTANCE = new MailFacade();

    // Services
    private final UserService userService = new UserService();
    private final AccountService accountService = new AccountService();
    private final FolderService folderService = new FolderService();
    private final MessageService messageService = new MessageService();
    private final AttachmentService attachmentService = new AttachmentService();
    private final MessageRepository messageRepository = new MessageRepository();

    private MailFacade() {}

    public static MailFacade getInstance() {
        return INSTANCE;
    }

    // USER

    public User login(String username, String password) {
        return userService.login(username, password);
    }

    public User register(String u, String p) {
        return userService.register(u, p);
    }

    // ACCOUNT

    public boolean createAccount(Account account) {
        return accountService.createAccount(account);
    }

    public List<Account> getAccountsByUser(int userId) {
        return accountService.getAccountsByUserId(userId);
    }

    public Account getAccount(int id) {
        return accountService.getById(id);
    }

    // FOLDERS

    public List<Folder> getFolders(int accountId) {
        return folderService.getFoldersByAccount(accountId);
    }

    public void createFolder(int accountId, String name) {
        folderService.createCustomFolder(accountId, name);
    }

    public void deleteFolder(Folder f) {
        folderService.deleteFolder(f);
    }

    // MESSAGES

    public List<Message> getMessages(int folderId) {
        return messageService.getMessages(folderId);
    }

    public Message getMessage(int id) {
        return messageService.getById(id);
    }

    public void markRead(int id, boolean read) {
        messageService.updateReadStatus(id, read);
    }

    public void updateImportance(int messageId, Importance imp) {
        messageRepository.updateImportance(messageId, imp);
    }

    public void deleteMessage(int id) {
        attachmentService.deleteAllByMessageId(id);
        messageService.delete(id);
    }

    public void moveToTrash(int messageId, int trashFolderId) {
        messageService.updateFolder(messageId, trashFolderId);
    }

    public void copyMessage(int id, int targetFolderId) {
        messageService.copyMessage(id, targetFolderId);
    }

    public void moveDraft(int id, int newFolderId) {
        messageService.updateFolder(id, newFolderId);
    }

    // ATTACHMENTS

    public List<Attachment> getAttachments(int msgId) {
        return attachmentService.getAttachments(msgId);
    }

    public void addAttachment(int messageId, File file) {
        attachmentService.addAttachment(messageId, file);
    }

    public void deleteAttachment(int attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
    }


    // MAIL / SMTP / SYNC

    public void syncInbox(int accountId, int inboxFolderId) {
        MailService mail = new MailService(accountId);
        mail.syncInbox(inboxFolderId);
    }

    public void sendMessage(int accountId,
                            int sentFolderId,
                            String sender,
                            String recipient,
                            String subject,
                            String body,
                            Importance importance,
                            List<File> attachments) {

        MailService mail = new MailService(accountId);
        mail.sendMessage(sentFolderId, sender, recipient, subject, body, importance, attachments);
    }

    public void saveDraft(int accountId,
                          int draftFolderId,
                          String sender,
                          String recipient,
                          String subject,
                          String body,
                          Importance importance,
                          List<File> attachments) {

        MailService mail = new MailService(accountId);
        mail.createDraft(
                draftFolderId,
                sender,
                recipient,
                subject,
                body,
                importance,
                attachments
        );
    }

    public void updateDraft(int accountId,
                            int draftId,
                            String subject,
                            String body,
                            String recipient,
                            Importance importance,
                            List<File> attachments) {

        MailService mail = new MailService(accountId);
        mail.updateDraft(draftId, subject, body, recipient, importance, attachments);
    }

    public void sendDraft(int accountId,
                          int draftId,
                          int sentFolderId,
                          List<File> attachments) {

        MailService mail = new MailService(accountId);
        mail.sendDraft(draftId, sentFolderId, attachments);
    }
}
