package emailclient.repository;

import emailclient.database.DatabaseConnection;
import emailclient.model.Message;
import emailclient.model.enums.Importance;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    public int add(Message m) {
        String sql = """
          INSERT INTO messages
          (folder_id, sender, recipient, subject, body, is_read, importance, date_sent, is_draft)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, m.getFolderId());
            ps.setString(2, m.getSender());
            ps.setString(3, m.getRecipient());
            ps.setString(4, m.getSubject());
            ps.setString(5, m.getBody());
            ps.setBoolean(6, m.isRead());
            ps.setString(7, m.getImportance().name());
            if (m.getDateSent() != null) ps.setString(8, m.getDateSent().toString());
            else ps.setNull(8, Types.VARCHAR);
            ps.setBoolean(9, m.isDraft());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            throw new RuntimeException("Не вдалося отримати ID");
        } catch (SQLException e) {
            throw new RuntimeException("Помилка insert: " + e.getMessage(), e);
        }
    }

    public Message getById(int id) {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка getById: " + e.getMessage(), e);
        }
    }

    public List<Message> getByFolderId(int folderId) {
        String sql = "SELECT * FROM messages WHERE folder_id = ? ORDER BY COALESCE(date_sent,'9999-12-31') DESC, id DESC";
        List<Message> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, folderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка getByFolderId: " + e.getMessage(), e);
        }
    }

    // ОДИН загальний update — без бізнес-логіки
    public void update(Message m) {
        String sql = """
            UPDATE messages SET 
                folder_id = ?, recipient = ?, subject = ?, body = ?,
                is_read = ?, importance = ?, date_sent = ?, is_draft = ?
            WHERE id = ?
        """;
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, m.getFolderId());
            ps.setString(2, m.getRecipient());
            ps.setString(3, m.getSubject());
            ps.setString(4, m.getBody());
            ps.setBoolean(5, m.isRead());
            ps.setString(6, m.getImportance().name());
            if (m.getDateSent() != null) ps.setString(7, m.getDateSent().toString());
            else ps.setNull(7, Types.VARCHAR);
            ps.setBoolean(8, m.isDraft());
            ps.setInt(9, m.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка update: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка delete: " + e.getMessage(), e);
        }
    }

    private Message map(ResultSet rs) throws SQLException {
        String dateStr = rs.getString("date_sent");
        LocalDateTime sent = (dateStr != null) ? LocalDateTime.parse(dateStr) : null;

        return new Message.Builder()
                .id(rs.getInt("id"))
                .folderId(rs.getInt("folder_id"))
                .sender(rs.getString("sender"))
                .recipient(rs.getString("recipient"))
                .subject(rs.getString("subject"))
                .body(rs.getString("body"))
                .isRead(rs.getBoolean("is_read"))
                .importance(Importance.valueOf(rs.getString("importance")))
                .dateSent(sent)
                .isDraft(rs.getBoolean("is_draft"))
                .build();
    }
}

