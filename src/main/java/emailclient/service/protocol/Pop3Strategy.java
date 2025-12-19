package emailclient.service.protocol;

import emailclient.model.Account;
import emailclient.model.Message;
import emailclient.service.MessageService;

import java.util.List;

public class Pop3Strategy implements MailProtocol {

    private final MessageService messageService = new MessageService();
    private boolean connected = false;

    @Override
    public void connect(Account account) {
        System.out.println("POP3 CONNECT " + account.getPop3Host() + ":" + account.getPop3Port());
        connected = true;
    }

    @Override
    public List<Message> fetchMessages(int accountId, int inboxFolderId) {
        if (!connected)
            throw new IllegalStateException("POP3 не підключений");

        messageService.createIncoming(
                inboxFolderId,
                "pop3@gmail.com",
                "polin009@ukr.net",
                "POP3 лист",
                "Тест: надісланий контент."
        );

        return messageService.getMessages(inboxFolderId);
    }


    @Override
    public void disconnect() {
        System.out.println("POP3 DISCONNECT");
        connected = false;
    }
}
