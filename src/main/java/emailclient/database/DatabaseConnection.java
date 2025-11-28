package emailclient.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton, що керує підключенням до SQLite.
 * Але не зберігає одне постійне з’єднання — лише параметри.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:emailclient.db";

    private DatabaseConnection() { }

    private static class Holder {
        private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    }

    public static DatabaseConnection getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Повертає НОВЕ з’єднання при кожному виклику.
     * Це правильно для SQLite.
     */
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL);

            // Увімкнення foreign keys
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            return conn;

        } catch (SQLException e) {
            throw new RuntimeException("Помилка підключення до бази даних: " + e.getMessage(), e);
        }
    }
}
