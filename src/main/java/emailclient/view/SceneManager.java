package emailclient.view;

import emailclient.controller.MailboxController;
import emailclient.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    private static void setScene(String fxml, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxml));
            if (controller != null) loader.setController(controller);

            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            throw new RuntimeException("Не вдалося завантажити сцену: " + fxml, e);
        }
    }

    public static void showLogin() {
        setScene("/ui/LoginView.fxml", null);
    }

    public static void showRegister() {
        setScene("/ui/RegisterView.fxml", null);
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
            throw new RuntimeException("Помилка відкриття Mailbox", e);
        }
    }
}

