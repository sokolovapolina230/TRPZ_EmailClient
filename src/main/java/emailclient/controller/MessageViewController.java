package emailclient.controller;

import emailclient.model.Attachment;
import emailclient.model.Message;
import emailclient.service.AttachmentService;
import emailclient.service.MessageService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageViewController {

    @FXML private Label subjectLabel;
    @FXML private Label fromLabel;
    @FXML private Label toLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea bodyArea;
    @FXML private ListView<Attachment> attachmentsList;

    private final MessageService messageService = new MessageService();
    private final AttachmentService attachmentService = new AttachmentService();

    private Message message;

    public void setMessage(Message msg) {
        this.message = msg;

        subjectLabel.setText(msg.getSubject());
        fromLabel.setText("Від: " + msg.getSender());
        toLabel.setText("Кому: " + (msg.getRecipient() == null ? "-" : msg.getRecipient()));

        if (msg.getDateSent() != null) {
            dateLabel.setText("Дата: " + msg.getDateSent().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        }

        bodyArea.setText(msg.getBody());

        List<Attachment> attachments = attachmentService.getAttachments(msg.getId());
        attachmentsList.getItems().setAll(attachments);
    }

    // кнопка "Видалити"
    @FXML
    private void handleDeleteMessage() {
        messageService.delete(message.getId());
    }

    // кнопка "Перемістити"
    @FXML
    private void handleMove() {
        // можна додати вікно вибору папки
    }

    // кнопка "Завантажити"
    @FXML
    private void handleDownloadAttachment() {
        Attachment selected = attachmentsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // просто копіюємо у Downloads
        System.out.println("Завантаження: " + selected.getFileName());
    }
}
