package emailclient.controller;

import emailclient.model.Message;
import emailclient.model.enums.Importance;
import emailclient.service.MailService;
import emailclient.service.MessageService;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.scene.control.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageFormController {

    @FXML private TextField toField;
    @FXML private TextField subjectField;
    @FXML private TextArea bodyArea;
    @FXML private ListView<String> attachmentsList;

    private final MessageService messageService = new MessageService();
    private MailService mailService;

    private final List<File> newAttachments = new ArrayList<>();

    private Integer folderId;     // папка (Sent або Drafts)
    private Integer accountId;
    private Message editingDraft; // якщо редагуємо чернетку

    // ----------------------------------------------------------
    //  ІНІЦІАЛІЗАЦІЯ ФОРМИ
    // ----------------------------------------------------------
    public void init(MailService mailService, int folderId, int accountId) {
        this.mailService = mailService;
        this.folderId = folderId;
        this.accountId = accountId;
    }

    public void loadDraft(Message draft) {
        this.editingDraft = draft;

        toField.setText(draft.getRecipient());
        subjectField.setText(draft.getSubject());
        bodyArea.setText(draft.getBody());
    }

    // ----------------------------------------------------------
    //  ДОДАТИ ВКЛАДЕННЯ
    // ----------------------------------------------------------
    @FXML
    private void handleAddAttachment() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Виберіть файл");

        File file = chooser.showOpenDialog(attachmentsList.getScene().getWindow());
        if (file == null) return;

        newAttachments.add(file);
        attachmentsList.getItems().add(file.getName());
    }

    // ----------------------------------------------------------
    //  ВИДАЛИТИ ВКЛАДЕННЯ
    // ----------------------------------------------------------
    @FXML
    private void handleRemoveAttachment() {
        int index = attachmentsList.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        attachmentsList.getItems().remove(index);
        newAttachments.remove(index);
    }

    // ----------------------------------------------------------
    //  ЗБЕРЕГТИ ЧЕРНЕТКУ
    // ----------------------------------------------------------
    @FXML
    private void handleSaveDraft() {
        String to = toField.getText();
        String subject = subjectField.getText();
        String body = bodyArea.getText();

        try {
            if (editingDraft == null) {
                // Новая чернетка
                int draftId = mailService.createDraft(
                        folderId,
                        mailService.getAccount().getEmailAddress(),
                        to,
                        subject,
                        body,
                        Importance.NORMAL,
                        newAttachments
                );

                showInfo("Чернетку збережено. ID = " + draftId);

            } else {
                // Оновити чернетку
                mailService.updateDraft(editingDraft.getId(),
                        subject,
                        body,
                        to,
                        Importance.NORMAL,
                        newAttachments
                );

                showInfo("Чернетку оновлено.");
            }

        } catch (Exception e) {
            showError("Помилка збереження: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------
    //  НАДІСЛАТИ
    // ----------------------------------------------------------
    @FXML
    private void handleSend() {
        String to = toField.getText();
        String subject = subjectField.getText();
        String body = bodyArea.getText();

        try {
            if (editingDraft == null) {
                // Надсилаємо новий лист
                int id = mailService.sendMessage(
                        folderId,
                        mailService.getAccount().getEmailAddress(),
                        to,
                        subject,
                        body,
                        Importance.NORMAL,
                        newAttachments
                );

                showInfo("Лист надіслано. ID = " + id);

            } else {
                // Надсилаємо чернетку
                mailService.sendDraft(
                        editingDraft.getId(),
                        folderId,
                        newAttachments
                );

                showInfo("Чернетку надіслано.");

            }

        } catch (Exception e) {
            showError("Помилка надсилання: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------
    //  ALERTS
    // ----------------------------------------------------------
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).show();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).show();
    }
}
