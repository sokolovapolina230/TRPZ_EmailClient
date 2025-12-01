package emailclient.controller;

import emailclient.facade.MailFacade;
import emailclient.model.User;
import emailclient.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;

    private final MailFacade facade = MailFacade.getInstance();

    @FXML
    private void handleRegister() {

        String u = usernameField.getText();
        String p1 = passwordField.getText();
        String p2 = repeatPasswordField.getText();

        if (u.isBlank() || p1.isBlank() || p2.isBlank()) {
            show("Заповніть всі поля!");
            return;
        }

        if (!p1.equals(p2)) {
            show("Паролі не збігаються!");
            return;
        }

        User created = facade.register(u, p1);

        if (created == null) {
            show("Користувач із таким логіном вже існує.");
            return;
        }

        show("Акаунт створено! Тепер увійдіть.");
        SceneManager.showCreateAccount(created);
    }

    @FXML
    private void handleBack() {
        SceneManager.showLogin();
    }

    private void show(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}
