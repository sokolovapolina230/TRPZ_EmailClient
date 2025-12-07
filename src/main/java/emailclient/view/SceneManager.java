package emailclient.view;

import emailclient.controller.CreateAccountController;
import emailclient.controller.MailboxController;

import emailclient.model.User;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage primaryStage;
    private static MailboxController mailboxController;

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(true);
    }

    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/ui/LoginView.fxml"));
            Scene scene = new Scene(loader.load());

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            throw new RuntimeException("Помилка відкриття LoginView", e);
        }
    }

    public static void showRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/ui/RegisterView.fxml"));
            Scene scene = new Scene(loader.load());

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            throw new RuntimeException("Помилка відкриття RegisterView", e);
        }
    }

    public static void showCreateAccount(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/ui/CreateAccountView.fxml"));
            Scene scene = new Scene(loader.load());

            CreateAccountController controller = loader.getController();
            controller.init(user);

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            throw new RuntimeException("Помилка відкриття CreateAccountView", e);
        }
    }

    public static void showMailbox(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/ui/MailboxView.fxml"));
            Scene scene = new Scene(loader.load());

            MailboxController controller = loader.getController();
            controller.init(user);

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            throw new RuntimeException("Помилка відкриття MailboxView", e);
        }
    }

    public static void setMailboxController(MailboxController controller) {
        mailboxController = controller;
    }

    public static MailboxController getMailboxController() {
        return mailboxController;
    }
}
