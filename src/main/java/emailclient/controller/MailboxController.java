package emailclient.controller;

import emailclient.facade.MailFacade;
import emailclient.model.*;
import emailclient.model.enums.FolderType;
import emailclient.model.enums.Importance;
import emailclient.service.DemoMailInitializer;
import emailclient.service.MailService;
import emailclient.view.SceneManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
        SceneManager.setMailboxController(this);
        this.user = user;

        setupColumns();
        setupRowFactory();
        loadAccounts();
        setupListeners();
    }

    public void setRightPanel(BorderPane pane) {
        messageViewContainer.getChildren().setAll(pane);
        messageViewContainer.setPrefWidth(600);
    }

    public void clearRightPanel() {
        messageViewContainer.getChildren().clear();
        messageViewContainer.setPrefWidth(0);
    }

    public void refreshCurrentFolder() {
        Folder folder = foldersList.getSelectionModel().getSelectedItem();
        if (folder != null) {
            loadMessages(folder);
        }
    }

    // Налаштування таблиці

    private void setupColumns() {

        colFrom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSender()));
        colSubject.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSubject()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss   dd.MM.yyyy");
        colDate.setCellValueFactory(d -> {
            if (d.getValue().getDateSent() == null)
                return new SimpleStringProperty("");
            return new SimpleStringProperty(d.getValue().getDateSent().format(formatter));
        });

        colImportance.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getImportance().name())
        );


        // Автоматичний розподіл ширини
        messagesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        colFrom.setMinWidth(150);
        colDate.setMinWidth(130);
        colImportance.setMinWidth(60);

        colImportance.setMaxWidth(60);
        colDate.setMaxWidth(180);
        colFrom.setMaxWidth(220);

        colSubject.setMinWidth(200);
        colSubject.setMaxWidth(Double.MAX_VALUE);

        colImportance.setResizable(false);

        // Сортування важливості
        colImportance.setComparator((a, b) -> {
            List<String> order = List.of("HIGH", "NORMAL", "LOW");
            return Integer.compare(order.indexOf(a), order.indexOf(b));
        });

        colImportance.setSortable(true);
    }


    // Row Factory

    private void setupRowFactory() {

        messagesTable.setRowFactory(table -> {
            TableRow<Message> row = new TableRow<>();

            // Стиль жирний для непрочитаних
            row.itemProperty().addListener((obs, oldMsg, newMsg) -> {
                if (newMsg == null) {
                    row.setStyle("");
                } else if (!newMsg.isRead()) {
                    row.setStyle("-fx-font-weight: bold;");
                } else {
                    row.setStyle("-fx-font-weight: normal;");
                }
            });

            // Правий клік - меню
            row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {

                if (row.isEmpty()) return;

                if (event.getButton() == MouseButton.SECONDARY) {
                    event.consume();

                    ContextMenu menu = buildRowContextMenu(row);
                    assert menu != null;
                    menu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            // Лівий клік - відкрити повідомлення
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY &&
                        event.getClickCount() == 1 &&
                        !row.isEmpty() &&
                        !event.isConsumed()) {

                    openMessageView(row.getItem());
                }
            });

            row.setOnContextMenuRequested(Event::consume);

            return row;
        });
    }

    private ContextMenu buildRowContextMenu(TableRow<Message> row) {

        Message msg = row.getItem();
        if (msg == null) return null;

        Menu importanceMenu = new Menu("Установити важливість");

        Map<Importance, String> labels = Map.of(
                Importance.HIGH, "Висока",
                Importance.NORMAL, "Нормальна",
                Importance.LOW, "Низька"
        );

        labels.forEach((importance, label) -> {
            MenuItem item = new MenuItem(label);

            if (msg.getImportance() == importance) {
                item.setText("✓ " + label);
                item.setStyle("-fx-font-weight: bold;");
            }

            item.setOnAction(e -> updateMessageImportance(msg, importance));
            importanceMenu.getItems().add(item);
        });

        return new ContextMenu(importanceMenu);
    }

    private void updateMessageImportance(Message msg, Importance level) {
        try {
            facade.updateImportance(msg.getId(), level);
            msg.setImportance(level);
            messagesTable.refresh();

            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Важливість оновлено: " + level);
            alert.show();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Помилка оновлення важливості:\n" + e.getMessage()).show();
        }
    }


    // Завантаження даних

    private void loadAccounts() {
        List<Account> accounts = facade.getAccountsByUser(user.getId());
        accountsList.getItems().setAll(accounts);

        if (!accounts.isEmpty()) {
            accountsList.getSelectionModel().select(0);
            loadFolders(accounts.getFirst());
        }
    }

    private void loadFolders(Account account) {
        List<Folder> folders = facade.getFolders(account.getId());
        foldersList.getItems().setAll(folders);

        folders.stream()
                .filter(f -> f.getType() == FolderType.INBOX)
                .findFirst()
                .ifPresent(inbox -> {
                    // вибираємо INBOX у списку
                    foldersList.getSelectionModel().select(inbox);

                    // ініціалізуємо демо-листи, якщо INBOX порожній
                    new DemoMailInitializer()
                            .initInboxIfEmpty(inbox.getId(), account.getEmailAddress());

                    // одразу оновлюємо список повідомлень
                    loadMessages(inbox);
                });
    }

    private void loadMessages(Folder folder) {
        messagesTable.getItems().setAll(facade.getMessages(folder.getId()));
        clearRightPanel();
    }


    // Listeners

    private void setupListeners() {

        accountsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, acc) -> {
            if (acc != null) loadFolders(acc);
        });

        foldersList.getSelectionModel().selectedItemProperty().addListener((obs, o, f) -> {
            if (f != null) loadMessages(f);
        });

        messagesTable.getSelectionModel().selectedItemProperty().addListener((obs, o, msg) -> {
            if (msg != null) openMessageView(msg);
        });
    }

    // Відкриття повідомлення
    private void openMessageView(Message msg) {
        try {

            Message fresh = facade.getMessage(msg.getId());

            if (fresh == null) {
                showError("Повідомлення не знайдено");
                return;
            }

            if (!fresh.isRead()) {
                facade.markRead(fresh.getId(), true);
                fresh.setRead(true);
                messagesTable.refresh();
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/MessageView.fxml")
            );

            BorderPane pane = loader.load();
            MessageViewController controller = loader.getController();

            Folder folder = foldersList.getSelectionModel().getSelectedItem();
            Account acc = accountsList.getSelectionModel().getSelectedItem();

            controller.setMessage(fresh, folder, acc, () -> loadMessages(folder));

            setRightPanel(pane);

        } catch (Exception e) {
            showError("Не вдалося відкрити повідомлення");
        }
    }



    // Дії

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
                    .findFirst().orElse(null);

            Folder sent = foldersList.getItems().stream()
                    .filter(f -> f.getType() == FolderType.SENT)
                    .findFirst().orElse(null);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/MessageFormView.fxml")
            );

            BorderPane form = loader.load();

            MessageFormController controller = loader.getController();
            assert drafts != null;
            assert sent != null;
            controller.init(new MailService(acc.getId()), drafts.getId(), sent.getId());

            setRightPanel(form);

        } catch (Exception e) {
            showError("Не вдалося створити лист");
        }
    }

    @FXML
    private void handleLogout() {
        SceneManager.showLogin();
    }

    @FXML
    private void handleRefresh() {
        Folder folder = foldersList.getSelectionModel().getSelectedItem();
        if (folder != null) loadMessages(folder);
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
                .findFirst().orElse(null);

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
            try {
                facade.createFolder(acc.getId(), name);
                loadFolders(acc);
            } catch (Exception e) {
                showError(e.getMessage());
            }
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
