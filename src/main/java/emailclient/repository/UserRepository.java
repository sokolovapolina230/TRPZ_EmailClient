package emailclient.repository;

import emailclient.database.DatabaseConnection;
import emailclient.model.User;

import java.sql.*;

public class UserRepository {

    public User getByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                );
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Помилка пошуку користувача: " + e.getMessage());
        }
    }

    public User createUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return new User(rs.getInt(1), username, password);
            }
            return null;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) return null;
            throw new RuntimeException("Помилка створення користувача: " + e.getMessage());
        }
    }
}
