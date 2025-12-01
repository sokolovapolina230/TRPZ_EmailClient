package emailclient.view;

import emailclient.controller.CreateAccountController;
import emailclient.controller.LoginController;
import emailclient.controller.MailboxController;
import emailclient.model.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage primaryStage;

    // ======================================================
    //                 INIT PRIMARY STAGE
    // ======================================================

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(true);
    }

    // ======================================================
    //                 UNIVERSAL LOADER
    // ======================================================

    private static Scene loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            return new Scene(loader.load());
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося завантажити FXML: " + fxmlPath, e);
        }
    }

    private static <T> T loadController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            loader.load();
            return loader.getController();
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося завантажити контролер: " + fxmlPath, e);
        }
    }

    // ======================================================
    //                      LOGIN
    // ======================================================

    public static void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/ui/LoginView.fxml"));
            Scene scene = new Scene(loader.load());

            LoginController controller = loader.getController();

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            throw new RuntimeException("Помилка відкриття LoginView", e);
        }
    }

    // ======================================================
    //                CREATE ACCOUNT
    // ======================================================

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


    // ======================================================
    //                      MAILBOX
    // ======================================================

    public static void showMailbox(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/ui/MailboxView.fxml"));
            Scene scene = new Scene(loader.load());

            MailboxController controller = loader.getController();
            controller.init(user);

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Помилка відкриття MailboxView", e);
        }
    }


    // ============================
    //      REGISTER USER
    // ============================

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
}

