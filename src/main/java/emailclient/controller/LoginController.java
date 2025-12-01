package emailclient.controller;

import emailclient.facade.MailFacade;
import emailclient.model.User;
import emailclient.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final MailFacade facade = MailFacade.getInstance();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Заповніть усі поля");
            return;
        }

        User user = facade.login(username, password);

        if (user == null) {
            showError("Невірний логін або пароль");
            return;
        }

        SceneManager.showMailbox(user);
    }

    @FXML
    private void handleOpenCreate() {
        SceneManager.showRegister();
    }

    private void showError(String text) {
        errorLabel.setText(text);
        errorLabel.setVisible(true);
    }
}
