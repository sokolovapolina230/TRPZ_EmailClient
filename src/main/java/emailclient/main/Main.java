package emailclient.main;

import emailclient.database.DatabaseInitializer;
import emailclient.view.SceneManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // 1. Ініціалізація бази
            DatabaseInitializer.initialize();


            // 2. Ініціалізація SceneManager
            SceneManager.init(stage);

            stage.setTitle("Email Client");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/mail.png")));

            // 3. Відкриваємо Login
            SceneManager.showLogin();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}