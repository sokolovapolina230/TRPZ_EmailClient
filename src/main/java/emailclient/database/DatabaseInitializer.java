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

            String sql = loadSQLScript();
            if (sql == null) {
                System.err.println("ERROR: schema.sql не знайдено!");
                return;
            }

            String[] commands = sql.split(";");

            for (String cmd : commands) {
                String c = cmd.trim();
                if (c.isEmpty()) continue;

                try {
                    stmt.execute(c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String loadSQLScript() {
        try {

            InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("sql/schema.sql");

            if (is == null) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
