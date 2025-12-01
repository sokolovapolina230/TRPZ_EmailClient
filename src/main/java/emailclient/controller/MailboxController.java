package emailclient.controller;

import emailclient.facade.MailFacade;
import emailclient.model.*;
import emailclient.model.enums.FolderType;
import emailclient.service.MailService;
import emailclient.view.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class MailboxController {

    @FXML private ListView<Account> accountsList;
    @FXML private ListView<Folder> foldersList;
    @FXML private TableView<Message> messagesTable;

    @FXML private TableColumn<Message, String> colFrom;
    @FXML private TableColumn<Message, String> colSubject;
    @FXML private TableColumn<Message, String> colDate;
    @FXML private TableColumn<Message, String> colImportance;

    @FXML private VBox messageViewContainer;

    private final MailFacade facade = MailFacade.getInstance();
    private User user;

    public void init(User user) {
        this.user = user;
        setupColumns();
        loadAccounts();
        setupListeners();
    }

    private void setupColumns() {
        colFrom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSender()));
        colSubject.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSubject()));
        colDate.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDateSent() == null ? "" : d.getValue().getDateSent().toString()));
        colImportance.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getImportance() == null ? "" : d.getValue().getImportance().toString()));

        messagesTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setStyle("");
                } else if (!msg.isRead()) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("-fx-font-weight: normal;");
                }
            }
        });
    }

    private void loadAccounts() {
        List<Account> accounts = facade.getAccountsByUser(user.getId());
        accountsList.getItems().setAll(accounts);

        if (!accounts.isEmpty()) {
            accountsList.getSelectionModel().select(0);
            loadFolders(accounts.get(0));
        }
    }

    private void loadFolders(Account account) {
        List<Folder> folders = facade.getFolders(account.getId());
        foldersList.getItems().setAll(folders);

        folders.stream()
                .filter(f -> f.getType() == FolderType.INBOX)
                .findFirst()
                .ifPresent(f -> foldersList.getSelectionModel().select(f));
    }

    private void loadMessages(Folder folder) {
        messagesTable.getItems().setAll(facade.getMessages(folder.getId()));
    }

    private void setupListeners() {

        accountsList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) loadFolders(n);
        });

        foldersList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) loadMessages(n);
        });

        messagesTable.getSelectionModel().selectedItemProperty().addListener((obs, o, msg) -> {
            if (msg != null) openMessageView(msg);
        });
    }

    private void openMessageView(Message msg) {
        try {
            if (!msg.isRead()) {
                facade.markRead(msg.getId(), true);
                messagesTable.refresh();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MessageView.fxml"));
            BorderPane pane = loader.load();

            MessageViewController controller = loader.getController();

            Folder currentFolder = foldersList.getSelectionModel().getSelectedItem();
            Account currentAccount = accountsList.getSelectionModel().getSelectedItem();

            controller.setMessage(
                    msg,
                    currentFolder,
                    currentAccount,
                    () -> loadMessages(currentFolder)
            );

            messageViewContainer.getChildren().setAll(pane);

        } catch (Exception e) {
            showError("Не вдалося відкрити повідомлення");
        }
    }

    @FXML
    private void handleLogout() {
        SceneManager.showLogin();
    }

    @FXML
    private void handleRefresh() {
        Folder f = foldersList.getSelectionModel().getSelectedItem();
        if (f != null) loadMessages(f);
    }

    @FXML
    private void handleNewMessage() {
        try {
            Account acc = accountsList.getSelectionModel().getSelectedItem();
            if (acc == null) {
                showError("Виберіть акаунт");
                return;
            }

            Folder drafts = foldersList.getItems().stream()
                    .filter(f -> f.getType() == FolderType.DRAFTS)
                    .findFirst()
                    .orElse(null);

            Folder sent = foldersList.getItems().stream()
                    .filter(f -> f.getType() == FolderType.SENT)
                    .findFirst()
                    .orElse(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MessageFormView.fxml"));
            BorderPane form = loader.load();

            MessageFormController controller = loader.getController();
            controller.init(new MailService(acc.getId()), drafts.getId(), sent.getId());

            messageViewContainer.getChildren().setAll(form);

        } catch (Exception e) {
            showError("Не вдалося створити лист");
        }
    }

    @FXML
    private void handleSyncInbox() {
        Account acc = accountsList.getSelectionModel().getSelectedItem();
        if (acc == null) {
            showError("Виберіть акаунт");
            return;
        }

        Folder inbox = foldersList.getItems().stream()
                .filter(f -> f.getType() == FolderType.INBOX)
                .findFirst()
                .orElse(null);

        if (inbox == null) {
            showError("Не знайдено INBOX");
            return;
        }

        facade.syncInbox(acc.getId(), inbox.getId());
        loadMessages(inbox);
    }

    @FXML
    private void handleCreateFolder() {
        Account acc = accountsList.getSelectionModel().getSelectedItem();
        if (acc == null) {
            showError("Виберіть акаунт");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Назва нової папки:");
        dialog.showAndWait().ifPresent(name -> {
            facade.createFolder(acc.getId(), name);
            loadFolders(acc);
        });
    }

    @FXML
    private void handleDeleteFolder() {
        Folder folder = foldersList.getSelectionModel().getSelectedItem();
        if (folder == null) return;

        try {
            facade.deleteFolder(folder);
            loadFolders(accountsList.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String t) {
        new Alert(Alert.AlertType.ERROR, t).show();
    }
}
