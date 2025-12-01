package emailclient.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = loadSQLScript("/sql/schema.sql");
            if (sql == null) {
                System.err.println("SQL-файл schema.sql не знайдено.");
                return;
            }

            for (String command : sql.split(";")) {
                String c = command.trim();
                if (!c.isEmpty()) {
                    stmt.execute(c);
                }
            }

            System.out.println("Схему БД створено або оновлено.");

        } catch (Exception e) {
            System.err.println("Помилка ініціалізації БД: " + e.getMessage());
        }
    }

    private static String loadSQLScript(String path) {
        try (InputStream is = DatabaseInitializer.class.getResourceAsStream(path)) {
            if (is == null) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            System.err.println("Помилка читання SQL-файлу: " + e.getMessage());
            return null;
        }
    }
}
