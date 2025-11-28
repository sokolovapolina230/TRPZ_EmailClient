package emailclient.controller;

import emailclient.model.User;
import emailclient.service.UserService;
import emailclient.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = userService.login(username, password);

        if (user == null) {
            new Alert(Alert.AlertType.ERROR, "Невірний логін або пароль!").show();
            return;
        }

        SceneManager.showMailbox(user);
    }

    @FXML
    private void handleShowRegister() {
        SceneManager.showRegister();
    }
}

