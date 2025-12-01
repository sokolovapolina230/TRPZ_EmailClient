package emailclient.controller;

import emailclient.facade.MailFacade;
import emailclient.model.Message;
import emailclient.model.enums.Importance;
import emailclient.service.MailService;
import emailclient.util.ValidationUtils;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageFormController {

    @FXML private TextField toField;
    @FXML private TextField subjectField;
    @FXML private TextArea bodyArea;
    @FXML private ListView<String> attachmentsList;
    @FXML private Label errorLabel;

    private MailService mailService;
    private final MailFacade facade = MailFacade.getInstance();

    private int draftsFolderId;
    private int sentFolderId;

    private Message editingDraft;
    private final List<File> newAttachments = new ArrayList<>();

    public void init(MailService mailService, int draftsFolderId, int sentFolderId) {
        this.mailService = mailService;
        this.draftsFolderId = draftsFolderId;
        this.sentFolderId = sentFolderId;
    }

    public void loadDraft(Message draft) {
        this.editingDraft = draft;
        toField.setText(draft.getRecipient());
        subjectField.setText(draft.getSubject());
        bodyArea.setText(draft.getBody());
    }

    @FXML
    private void handleAddAttachment() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Виберіть файл");
        File file = chooser.showOpenDialog(attachmentsList.getScene().getWindow());
        if (file == null) return;

        try {
            long maxSize = 10 * 1024 * 1024;
            if (file.length() > maxSize) {
                showError("Файл завеликий (максимум 10 MB)");
                return;
            }

            newAttachments.add(file);
            attachmentsList.getItems().add(file.getName());

        } catch (Exception e) {
            showError("Не вдалося додати файл: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveAttachment() {
        int idx = attachmentsList.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;

        attachmentsList.getItems().remove(idx);
        newAttachments.remove(idx);
    }

    @FXML
    private void handleSaveDraft() {

        String to = toField.getText().trim();
        String subject = subjectField.getText().trim();
        String body = bodyArea.getText();

        clearErrors();

        if (subject.isEmpty() && body.isEmpty()) {
            showError("Чернетка не може бути порожньою");
            return;
        }

        try {
            int accId = mailService.getAccount().getId();

            if (editingDraft == null) {
                int draftId = facade.saveDraft(
                        accId,
                        draftsFolderId,
                        mailService.getAccount().getEmailAddress(),
                        to,
                        subject,
                        body,
                        Importance.NORMAL,
                        newAttachments
                );

                showInfo("Чернетку збережено.");

            } else {
                facade.updateDraft(
                        accId,
                        editingDraft.getId(),
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

    @FXML
    private void handleSend() {

        String to = toField.getText().trim();
        String subject = subjectField.getText().trim();
        String body = bodyArea.getText();

        clearErrors();

        if (!ValidationUtils.isValidEmail(to)) {
            ValidationUtils.showError(toField, errorLabel, "Введіть коректний Email отримувача");
            return;
        }

        if (subject.isEmpty()) {
            ValidationUtils.showError(subjectField, errorLabel, "Введіть тему листа");
            return;
        }

        try {
            int accId = mailService.getAccount().getId();

            if (editingDraft == null) {

                int id = facade.sendMessage(
                        accId,
                        sentFolderId,
                        mailService.getAccount().getEmailAddress(),
                        to,
                        subject,
                        body,
                        Importance.NORMAL,
                        newAttachments
                );

                showInfo("Лист надіслано");

            } else {

                facade.sendDraft(
                        accId,
                        editingDraft.getId(),
                        sentFolderId,
                        newAttachments
                );

                showInfo("Чернетку надіслано.");
            }

        } catch (Exception e) {
            showError("Помилка надсилання: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.getStyleClass().add("error-label");
    }

    private void clearErrors() {
        errorLabel.setText("");
        errorLabel.getStyleClass().remove("error-label");

        ValidationUtils.clearError(toField, errorLabel);
        ValidationUtils.clearError(subjectField, errorLabel);
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).show();
    }
}

