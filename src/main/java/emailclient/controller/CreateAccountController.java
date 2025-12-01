package emailclient.controller;

import emailclient.facade.MailFacade;
import emailclient.model.Account;
import emailclient.model.User;
import emailclient.service.AutoConfigService;
import emailclient.util.ValidationUtils;
import emailclient.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CreateAccountController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AutoConfigService autoConfig = new AutoConfigService();
    private final MailFacade facade = MailFacade.getInstance();

    private User user;

    public void init(User user) {
        this.user = user;
    }

    @FXML
    private void handleCreate() {
        String email = emailField.getText().trim();
        String pass = passwordField.getText().trim();

        ValidationUtils.clearError(emailField, errorLabel);
        ValidationUtils.clearError(passwordField, errorLabel);

        if (!ValidationUtils.isValidEmail(email)) {
            ValidationUtils.showError(emailField, errorLabel, "Некоректний email");
            return;
        }

        if (pass.length() < 6) {
            ValidationUtils.showError(passwordField, errorLabel, "Пароль має містити не менше 6 символів");
            return;
        }

        try {
            Account acc = autoConfig.setupAccount(email, pass, user.getId());
            boolean ok = facade.createAccount(acc);

            if (!ok) {
                ValidationUtils.showError(emailField, errorLabel, "Помилка створення акаунта");
                return;
            }

            SceneManager.showMailbox(user);

        } catch (Exception e) {
            ValidationUtils.showError(emailField, errorLabel,
                    "Помилка створення акаунту: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.showLogin();
    }
}
