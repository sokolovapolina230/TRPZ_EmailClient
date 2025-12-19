package emailclient.service.protocol;

import emailclient.model.Account;
import emailclient.model.Message;
import emailclient.service.MessageService;

import java.util.List;

public class ImapStrategy implements MailProtocol {

    private final MessageService messageService = new MessageService();
    private boolean connected = false;

    @Override
    public void connect(Account account) {
        System.out.println("IMAP CONNECT " + account.getImapHost() + ":" + account.getImapPort());
        connected = true;
    }

    @Override
    public List<Message> fetchMessages(int accountId, int inboxFolderId) {
        if (!connected)
            throw new IllegalStateException("IMAP не підключений");

        messageService.createIncoming(
                inboxFolderId,
                "server@gmail.com",
                "polin009@ukr.net",
                "IMAP лист",
                "Тест: надісланий контент."
        );
        return messageService.getMessages(inboxFolderId);
    }

    @Override
    public void disconnect() {
        System.out.println("IMAP DISCONNECT");
        connected = false;
    }
}
