package emailclient.controller;

import emailclient.facade.MailFacade;
import emailclient.model.Attachment;
import emailclient.model.Message;
import emailclient.model.enums.Importance;
import emailclient.service.AutoConfigService;
import emailclient.service.MailService;
import emailclient.util.ValidationUtils;
import emailclient.view.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;

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


    private final List<Attachment> existingAttachments = new ArrayList<>(); // з БД
    private final List<File> newFiles = new ArrayList<>(); // нові файли
    private final Set<Integer> deletedAttachmentIds= new HashSet<>();   // що треба видалити

    private final AutoConfigService autoConfig = new AutoConfigService();

    public void init(MailService mailService, int draftsFolderId, int sentFolderId) {
        this.mailService = mailService;
        this.draftsFolderId = draftsFolderId;
        this.sentFolderId   = sentFolderId;

        this.editingDraft = null;

        // Скидаємо моделі стану
        existingAttachments.clear();
        newFiles.clear();
        deletedAttachmentIds.clear();
        attachmentsList.getItems().clear();
        clearErrors();
    }

    public void loadDraft(Message draft) {
        this.editingDraft = draft;

        toField.setText(draft.getRecipient());
        subjectField.setText(draft.getSubject());
        bodyArea.setText(draft.getBody());

        existingAttachments.clear();
        existingAttachments.addAll(facade.getAttachments(draft.getId()));

        newFiles.clear();
        deletedAttachmentIds.clear();

        refreshAttachmentListView();
    }

    // Вкладення

    @FXML
    private void handleAddAttachment() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Виберіть файл");
        File file = chooser.showOpenDialog(attachmentsList.getScene().getWindow());
        if (file == null) return;

        if (file.length() == 0) {
            showError("Файл порожній");
            return;
        }

        if (file.length() > 10 * 1024 * 1024) {
            showError("Файл завеликий (максимум 10 MB)");
            return;
        }

        newFiles.add(file);
        refreshAttachmentListView();
    }

    @FXML
    private void handleRemoveAttachment() {
        int idx = attachmentsList.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;
        int existingCount = (int) existingAttachments.stream()
                .filter(a -> !deletedAttachmentIds.contains(a.getId()))
                .count();

        if (idx < existingCount) {

            int seen = 0;
            for (Attachment a : existingAttachments) {
                if (deletedAttachmentIds.contains(a.getId())) continue;
                if (seen == idx) {
                    deletedAttachmentIds.add(a.getId());
                    break;
                }
                seen++;
            }
        } else {

            int newIdx = idx - existingCount;
            if (newIdx >= 0 && newIdx < newFiles.size()) {
                newFiles.remove(newIdx);
            }
        }

        refreshAttachmentListView();
    }

    private void refreshAttachmentListView() {
        List<String> items = new ArrayList<>();

        for (Attachment a : existingAttachments) {
            if (!deletedAttachmentIds.contains(a.getId())) {
                items.add(a.getFileName() + " (" + (a.getSize() / 1024) + " KB)");
            }
        }
        for (File f : newFiles) {
            items.add(f.getName() + " (" + (f.length() / 1024) + " KB)");
        }
        attachmentsList.getItems().setAll(items);
    }

    // Збереження

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
                facade.saveDraft(
                        accId,
                        draftsFolderId,
                        mailService.getAccount().getEmailAddress(),
                        to,
                        subject,
                        body,
                        Importance.NORMAL,
                        new ArrayList<>(newFiles)
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
                        List.of()
                );

                // Видаляємо відмічені вкладення
                for (Integer attId : deletedAttachmentIds) {
                    try {
                        facade.deleteAttachment(attId);
                    } catch (Exception ignore) {}
                }

                // Додаємо нові файли
                for (File f : newFiles) {
                    facade.addAttachment(editingDraft.getId(), f);
                }

                showInfo("Чернетку оновлено.");
            }

            // Очищаємо форму, оновлюємо список та закриваємо панель
            SceneManager.getMailboxController().refreshCurrentFolder();
            SceneManager.getMailboxController().clearRightPanel();

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
            ValidationUtils.showError(toField, errorLabel, "Некоректний формат email");
            return;
        }
        if (!autoConfig.isProviderSupported(to)) {
            ValidationUtils.showError(toField, errorLabel, "Провайдер не підтримується");
            return;
        }
        if (subject.isEmpty()) {
            ValidationUtils.showError(subjectField, errorLabel, "Введіть тему листа");
            return;
        }

        try {
            int accId = mailService.getAccount().getId();

            if (editingDraft == null) {
                // надсилаємо новий
                facade.sendMessage(
                        accId,
                        sentFolderId,
                        mailService.getAccount().getEmailAddress(),
                        to, subject, body,
                        Importance.NORMAL,
                        new ArrayList<>(newFiles)
                );
                showInfo("Лист надіслано.");
            } else {
                for (Integer attId : deletedAttachmentIds) {
                    try { facade.deleteAttachment(attId); } catch (Exception ignore) {}
                }
                for (File f : newFiles) {
                    facade.addAttachment(editingDraft.getId(), f);
                }
                facade.sendDraft(accId, editingDraft.getId(), sentFolderId, List.of());
                showInfo("Чернетку надіслано.");
            }

            SceneManager.getMailboxController().clearRightPanel();

        } catch (Exception e) {
            showError("Помилка надсилання: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        SceneManager.getMailboxController().clearRightPanel();
    }

    // Допоміжні

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    private void clearErrors() {
        errorLabel.setText("");
        ValidationUtils.clearError(toField, errorLabel);
        ValidationUtils.clearError(subjectField, errorLabel);
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}
