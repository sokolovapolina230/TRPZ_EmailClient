package emailclient.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_DIR;
    private static final String DB_PATH;
    private static final String URL;

    static {
        String appData = System.getenv("APPDATA");

        DB_DIR = (appData != null && !appData.isBlank())
                ? appData + File.separator + "EmailClient"
                : System.getProperty("user.home") + File.separator + ".emailclient";
        DB_PATH = DB_DIR + File.separator + "emailclient.db";

        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        URL = "jdbc:sqlite:" + DB_PATH;

    }

    public static DatabaseConnection getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    }

    private DatabaseConnection() { }

    // Повертає нове з’єднання при кожному виклику
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            return conn;

        } catch (SQLException e) {
            throw new RuntimeException("Помилка підключення до бази даних: " + e.getMessage(), e);
        }
    }
}
