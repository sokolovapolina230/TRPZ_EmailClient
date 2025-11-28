package emailclient.service.protocol;

import emailclient.model.Account;
import emailclient.model.Message;

import java.util.List;

public interface MailProtocol {

    void connect(Account account) throws Exception;

    List<Message> fetchMessages(int accountId, int targetFolderId) throws Exception;

    void disconnect();
}
