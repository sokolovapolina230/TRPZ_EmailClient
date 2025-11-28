package emailclient.repository;

import emailclient.database.DatabaseConnection;
import emailclient.model.Folder;
import emailclient.model.enums.FolderType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FolderRepository {

    /**
     * Додавання папки
     */
    public void add(Folder folder) {
        String sql = "INSERT INTO folders (account_id, name, type) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, folder.getAccountId());
            stmt.setString(2, folder.getName());
            stmt.setString(3, folder.getType().name());
            stmt.executeUpdate();

            System.out.println("📁 Додано папку: " + folder.getName());

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при додаванні папки", e);
        }
    }

    /**
     * Отримання папок акаунта
     */
    public List<Folder> getByAccountId(int accountId) {
        List<Folder> list = new ArrayList<>();
        String sql = "SELECT * FROM folders WHERE account_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Folder folder = new Folder();
                folder.setId(rs.getInt("id"));
                folder.setAccountId(rs.getInt("account_id"));
                folder.setName(rs.getString("name"));
                folder.setType(FolderType.valueOf(rs.getString("type")));
                list.add(folder);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при отриманні папок", e);
        }

        return list;
    }

    /**
     * Оновлення папки
     */
    public void update(Folder folder) {
        String sql = "UPDATE folders SET name = ?, type = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, folder.getName());
            stmt.setString(2, folder.getType().name());
            stmt.setInt(3, folder.getId());
            stmt.executeUpdate();

            System.out.println("✏️ Папку оновлено: " + folder.getName());

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при оновленні папки", e);
        }
    }

    /**
     * Видалення папки
     */
    public void delete(int id) {
        String sql = "DELETE FROM folders WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ Папку видалено (id=" + id + ")");

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при видаленні папки", e);
        }
    }

    /**
     * (Опціонально) Видалити всі папки акаунта
     */
    public void deleteAllByAccount(int accountId) {
        String sql = "DELETE FROM folders WHERE account_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка при масовому видаленні папок", e);
        }
    }
}
