package emailclient.repository;

import emailclient.database.DatabaseConnection;
import emailclient.model.Attachment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttachmentRepository {

    public int add(Attachment a) {
        String sql = """
            INSERT INTO attachments (message_id, file_name, file_path, size)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getMessageId());
            ps.setString(2, a.getFileName());
            ps.setString(3, a.getFilePath());
            ps.setLong(4, a.getSize());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    a.setId(id);
                    return id;
                }
            }

            throw new RuntimeException("Не вдалося отримати ID вкладення");

        } catch (SQLException e) {
            throw new RuntimeException("Помилка додавання вкладення: " + e.getMessage(), e);
        }
    }

    public Attachment getById(int id) {
        String sql = "SELECT * FROM attachments WHERE id = ?";

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Помилка getById: " + e.getMessage(), e);
        }
    }

    public List<Attachment> getByMessageId(int messageId) {
        String sql = "SELECT * FROM attachments WHERE message_id = ?";
        List<Attachment> list = new ArrayList<>();

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, messageId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Помилка getByMessageId: " + e.getMessage(), e);
        }

        return list;
    }

    public void delete(int id) {
        String sql = "DELETE FROM attachments WHERE id = ?";

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка delete: " + e.getMessage(), e);
        }
    }

    private Attachment map(ResultSet rs) throws SQLException {
        return new Attachment.Builder()
                .id(rs.getInt("id"))
                .messageId(rs.getInt("message_id"))
                .fileName(rs.getString("file_name"))
                .filePath(rs.getString("file_path"))
                .size(rs.getLong("size"))
                .build();
    }
}
