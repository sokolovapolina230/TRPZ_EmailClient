package emailclient.controller;

import emailclient.facade.MailFacade;
import emailclient.model.User;
import emailclient.util.ValidationUtils;
import emailclient.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private Label errorLabel;

    private final MailFacade facade = MailFacade.getInstance();

    @FXML
    private void handleRegister() {

        String u = usernameField.getText().trim();
        String p1 = passwordField.getText().trim();
        String p2 = repeatPasswordField.getText().trim();

        ValidationUtils.clearError(usernameField, null);
        ValidationUtils.clearError(passwordField, null);
        ValidationUtils.clearError(repeatPasswordField, errorLabel);

        if (u.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
            errorLabel.setText("Заповніть всі поля");
            if (u.isEmpty()) ValidationUtils.showError(usernameField, null, "");
            if (p1.isEmpty()) ValidationUtils.showError(passwordField, null, "");
            if (p2.isEmpty()) ValidationUtils.showError(repeatPasswordField, null, "");
            return;
        }

        if (!p1.equals(p2)) {
            ValidationUtils.showError(repeatPasswordField, errorLabel, "Паролі не збігаються");
            return;
        }

        User created = facade.register(u, p1);

        if (created == null) {
            ValidationUtils.showError(usernameField, errorLabel,
                    "Користувач із таким логіном вже існує");
            return;
        }

        SceneManager.showCreateAccount(created);
    }

    @FXML
    private void handleBack() {
        SceneManager.showLogin();
    }
}
