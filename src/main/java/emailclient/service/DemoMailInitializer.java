package emailclient.service;

import emailclient.model.enums.Importance;
import emailclient.repository.MessageRepository;

public class DemoMailInitializer {

    private final MessageService messageService = new MessageService();
    private final MessageRepository messageRepository = new MessageRepository();

    public void initInboxIfEmpty(int inboxFolderId, String recipientEmail) {
        if (!messageService.getMessages(inboxFolderId).isEmpty()) return;

        createDemoMessage(
                inboxFolderId,
                recipientEmail,
                "support@gmail.com",
                "Вітаємо!",
                "Дякуємо за реєстрацію.",
                Importance.HIGH
        );

        createDemoMessage(
                inboxFolderId,
                recipientEmail,
                "news@ukr.net",
                "Головні новини",
                "Вибірка найважливіших подій за день.",
                Importance.NORMAL
        );

        createDemoMessage(
                inboxFolderId,
                recipientEmail,
                "trade@i.ua",
                "Спеціальні умови для нових клієнтів",
                "Отримайте знижку на наші послуги у якості вітального бонусу.",
                Importance.LOW
        );
    }

    private void createDemoMessage(int folderId,
                                   String recipient,
                                   String sender,
                                   String subject,
                                   String body,
                                   Importance importance) {

        int id = messageService.createIncoming(
                folderId,
                sender,
                recipient,
                subject,
                body
        );

        try {
            messageRepository.updateImportance(id, importance);
        } catch (Exception ignored) {
        }
    }
}

