package emailclient.controller;

import emailclient.model.Account;
import emailclient.model.Folder;
import emailclient.model.Message;
import emailclient.model.User;
import emailclient.model.enums.FolderType;
import emailclient.model.enums.Importance;
import emailclient.service.AccountService;
import emailclient.service.FolderService;
import emailclient.service.MailService;
import emailclient.service.MessageService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MailboxController {

    // ---------- UI з FXML ----------
    @FXML private ListView<Account> accountsListView;
    @FXML private ListView<Folder>  foldersListView;
    @FXML private TableView<Message> messagesTable;
    @FXML private TableColumn<Message, String> colFrom;
    @FXML private TableColumn<Message, String> colSubject;
    @FXML private TableColumn<Message, String> colDate;
    @FXML private TableColumn<Message, String> colImportance;
    @FXML private VBox messageViewContainer;

    // ---------- Services ----------
    private final AccountService accountService = new AccountService();
    private final FolderService folderService   = new FolderService();
    private final MessageService messageService = new MessageService();

    // ---------- State ----------
    private User user;

    // =============================================================
    //                  PUBLIC API (викликає SceneManager)
    // =============================================================
    public void init(User user) {
        this.user = user;
        setupTableColumns();
        setupListeners();
        loadAccounts();
    }

    // =============================================================
    //                  SETUP COLUMNS
    // =============================================================
    private void setupTableColumns() {
        colFrom.setCellValueFactory(cell -> new SimpleStringProperty(
                safe(cell.getValue().getSender())
        ));
        colSubject.setCellValueFactory(cell -> new SimpleStringProperty(
                safe(cell.getValue().getSubject())
        ));
        colDate.setCellValueFactory(cell -> {
            var dt = cell.getValue().getDateSent();
            String text = (dt == null) ? "-" : dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            return new SimpleStringProperty(text);
        });
        colImportance.setCellValueFactory(cell -> {
            Importance imp = cell.getValue().getImportance();
            String sign = switch (imp) {
                case HIGH   -> "⚠";
                case NORMAL -> "";
                case LOW    -> "·";
            };
            return new SimpleStringProperty(sign);
        });

        // невелика естетика — жирний шрифт для непрочитаних
        messagesTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setStyle("");
                } else {
                    setStyle(msg.isRead() ? "" : "-fx-font-weight: bold;");
                }
            }
        });
    }

    // =============================================================
    //                  LOADERS
    // =============================================================
    private void loadAccounts() {
        List<Account> accounts = accountService.getAccountsByUserId(user.getId());
        accountsListView.getItems().setAll(accounts);

        // Як показувати елементи акаунтів
        accountsListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Account acc, boolean empty) {
                super.updateItem(acc, empty);
                if (empty || acc == null) setText(null);
                else setText(acc.getEmailAddress() + " (" + acc.getProvider() + ")");
            }
        });

        if (!accounts.isEmpty()) {
            accountsListView.getSelectionModel().select(0);
            loadFolders(accounts.get(0));
        }
    }

    private void loadFolders(Account account) {
        List<Folder> folders = folderService.getFoldersByAccount(account.getId());
        foldersListView.getItems().setAll(folders);

        foldersListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Folder f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) setText(null);
                else setText(f.getName());
            }
        });

        // Вибрати INBOX за замовчуванням
        folders.stream()
                .filter(f -> f.getType() == FolderType.INBOX)
                .findFirst()
                .ifPresent(f -> foldersListView.getSelectionModel().select(f));
    }

    private void loadMessages(Folder folder) {
        List<Message> msgs = messageService.getMessages(folder.getId());
        messagesTable.getItems().setAll(msgs);
    }

    // =============================================================
    //                  LISTENERS
    // =============================================================
    private void setupListeners() {
        // вибір акаунта
        accountsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldA, newA) -> {
            if (newA != null) {
                loadFolders(newA);
                clearMessageView();
                messagesTable.getItems().clear();
            }
        });

        // вибір папки
        foldersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldF, newF) -> {
            if (newF != null) {
                loadMessages(newF);
                clearMessageView();
            }
        });

        // відкриття повідомлення
        messagesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldM, newM) -> {
            if (newM != null) {
                openMessageView(newM);
                // Позначити як прочитане
                if (!newM.isRead()) {
                    messageService.markAsRead(newM.getId());
                    newM.markAsRead(); // локально для підсвітки
                    messagesTable.refresh();
                }
            }
        });
    }

    // =============================================================
    //                  OPEN MESSAGE VIEW (embed)
    // =============================================================
    private void openMessageView(Message msg) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MessageView.fxml"));
            Node pane = loader.load();

            MessageViewController controller = loader.getController();
            controller.setMessage(msg);

            messageViewContainer.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Не вдалося відкрити повідомлення");
        }
    }

    private void clearMessageView() {
        messageViewContainer.getChildren().clear();
    }

    // =============================================================
    //                  HANDLERS з FXML (заглушки)
    // =============================================================
    @FXML
    private void handleCreateFolder() {
        Account acc = accountsListView.getSelectionModel().getSelectedItem();
        if (acc == null) { showError("Оберіть акаунт"); return; }

        TextInputDialog d = new TextInputDialog();
        d.setHeaderText("Назва нової папки:");
        d.showAndWait().ifPresent(name -> {
            try {
                folderService.createCustomFolder(acc.getId(), name);
                loadFolders(acc);
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteFolder() {
        Folder f = foldersListView.getSelectionModel().getSelectedItem();
        if (f == null) return;
        try {
            folderService.deleteFolder(f);
            Account acc = accountsListView.getSelectionModel().getSelectedItem();
            if (acc != null) loadFolders(acc);
            messagesTable.getItems().clear();
            clearMessageView();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleNewMessage() {
        // мінімальна демо-логіка: створити чернетку в папці Чернетки, якщо є
        Account acc = accountsListView.getSelectionModel().getSelectedItem();
        if (acc == null) { showError("Оберіть акаунт"); return; }
        Folder drafts = foldersListView.getItems().stream()
                .filter(f -> f.getType() == FolderType.DRAFTS)
                .findFirst().orElse(null);
        if (drafts == null) { showError("Немає папки Чернетки"); return; }

        int draftId = messageService.createDraft(
                drafts.getId(),
                acc.getEmailAddress(),
                null,
                "(нова тема)",
                "",
                Importance.NORMAL
        );
        // перезавантажити список листів, щоб з’явилася чернетка
        loadMessages(drafts);
    }

    @FXML
    private void handleRefresh() {
        Folder f = foldersListView.getSelectionModel().getSelectedItem();
        if (f != null) loadMessages(f);
    }

    @FXML
    private void handleSyncInbox() {
        Account acc = accountsListView.getSelectionModel().getSelectedItem();
        if (acc == null) {
            showError("Оберіть акаунт");
            return;
        }

        Folder inbox = foldersListView.getItems().stream()
                .filter(f -> f.getType() == FolderType.INBOX)
                .findFirst().orElse(null);

        if (inbox == null) {
            showError("Немає папки Вхідні");
            return;
        }

        try {
            MailService mail = new MailService(acc.getId());
            List<Message> newMessages = mail.syncInbox(inbox.getId());

            // оновлюємо таблицю
            messagesTable.getItems().setAll(newMessages);

        } catch (Exception e) {
            showError("Помилка синхронізації: " + e.getMessage());
        }
    }


    @FXML
    private void handleLogout() {
        // TODO: виклик SceneManager.showLogin();
        // поки що просто очистимо все
        accountsListView.getItems().clear();
        foldersListView.getItems().clear();
        messagesTable.getItems().clear();
        clearMessageView();
    }

    // =============================================================
    //                  HELPERS
    // =============================================================
    private String safe(String s) { return s == null ? "" : s; }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR, text, ButtonType.OK);
        alert.showAndWait();
    }
}
