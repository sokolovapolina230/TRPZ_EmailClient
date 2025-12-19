package emailclient.controller;

import emailclient.facade.MailFacade;
import emailclient.model.*;
import emailclient.model.enums.FolderType;
import emailclient.service.FolderService;
import emailclient.view.SceneManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Desktop;
import java.io.File;

public class MessageViewController {

    @FXML private Label subjectLabel;
    @FXML private Label fromLabel;
    @FXML private Label toLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea bodyArea;
    @FXML private ListView<Attachment> attachmentsList;

    @FXML private Button btnEdit;
    @FXML private Button btnCopy;
    @FXML private Button btnMove;
    @FXML private Button btnDelete;
    @FXML private Button btnClose;

    private Message message;
    private Folder folder;
    private Account account;
    private Runnable refreshCallback;

    private final MailFacade facade = MailFacade.getInstance();
    private final FolderService folderService = new FolderService();


    public void setMessage(Message msg, Folder folder, Account acc, Runnable refreshCallback) {
        this.message = msg;
        this.folder = folder;
        this.account = acc;
        this.refreshCallback = refreshCallback;

        subjectLabel.setText(msg.getSubject());
        fromLabel.setText("Від: " + msg.getSender());
        toLabel.setText("Кому: " + msg.getRecipient());

        if (msg.getDateSent() != null)
            dateLabel.setText(msg.getDateSent().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        bodyArea.setText(msg.getBody());
        attachmentsList.getItems().setAll(facade.getAttachments(msg.getId()));

        attachmentsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Attachment item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null :
                        item.getFileName() + " (" + item.getSize() / 1024 + " KB)");
            }
        });

        attachmentsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Attachment selected = attachmentsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openAttachment(selected);
                }
            }
        });

        updateButtons();
    }

    private void openAttachment(Attachment attachment) {
        try {
            File file = new File(attachment.getFilePath());

            if (!file.exists()) {
                new Alert(Alert.AlertType.ERROR,
                        "Файл не знайдено").show();
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                new Alert(Alert.AlertType.ERROR,
                        "Відкриття файлів не підтримується").show();
                return;
            }

            Desktop.getDesktop().open(file);

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Не вдалося відкрити файл" + e.getMessage()).show();
        }
    }

    private void updateButtons() {

        boolean isDraft = folder.getType() == FolderType.DRAFTS;
        boolean isTrash = folder.getType() == FolderType.TRASH;

        btnEdit.setVisible(isDraft);
        btnMove.setVisible(isDraft);

        btnCopy.setVisible(!isDraft && !isTrash);

        btnDelete.setVisible(true);
        if (isTrash) {
            btnDelete.setText("Видалити назавжди");
        } else {
            btnDelete.setText("Видалити");
        }
    }

    @FXML
    private void handleEdit() {
        if (folder.getType() != FolderType.DRAFTS) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MessageFormView.fxml"));
            BorderPane pane = loader.load();

            MessageFormController form = loader.getController();

            Folder sent = folderService.getFoldersByAccount(account.getId())
                    .stream()
                    .filter(f -> f.getType() == FolderType.SENT)
                    .findFirst().orElseThrow();

            form.init(new emailclient.service.MailService(account.getId()), folder.getId(), sent.getId());
            form.loadDraft(message);

            SceneManager.getMailboxController().setRightPanel(pane);

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    private void handleCopy() {

        if (folder.getType() == FolderType.TRASH) return;

        List<Folder> folders = folderService.getFoldersByAccount(account.getId());
        folders.removeIf(f -> f.getType() == FolderType.TRASH);

        ChoiceDialog<Folder> dialog = new ChoiceDialog<>(null, folders);
        dialog.setTitle("Копіювати лист");
        dialog.setHeaderText("Оберіть папку");

        dialog.showAndWait().ifPresent(targetFolder -> {
            facade.copyMessage(message.getId(), targetFolder.getId());
            refreshCallback.run();
        });
    }

    @FXML
    private void handleMove() {
        if (folder.getType() != FolderType.DRAFTS) return;

        List<Folder> folders = folderService.getFoldersByAccount(account.getId());
        folders.removeIf(f -> f.getType() == FolderType.TRASH);

        ChoiceDialog<Folder> dialog = new ChoiceDialog<>(null, folders);
        dialog.setTitle("Перемістити чернетку");

        dialog.showAndWait().ifPresent(target -> {
            facade.moveDraft(message.getId(), target.getId());
            refreshCallback.run();
            SceneManager.getMailboxController().clearRightPanel();
        });
    }

    @FXML
    private void handleClose() {
        SceneManager.getMailboxController().clearRightPanel();
    }

    @FXML
    private void handleDeleteMessage() {

        if (folder.getType() == FolderType.TRASH) {
            facade.deleteMessage(message.getId());
        } else {
            Folder trash = folderService.getFoldersByAccount(account.getId())
                    .stream()
                    .filter(f -> f.getType() == FolderType.TRASH)
                    .findFirst()
                    .orElseThrow();

            facade.moveToTrash(message.getId(), trash.getId());
        }

        refreshCallback.run();
        SceneManager.getMailboxController().clearRightPanel();
    }

}
