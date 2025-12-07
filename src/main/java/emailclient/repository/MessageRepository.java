package emailclient.repository;

import emailclient.database.DatabaseConnection;
import emailclient.model.Message;
import emailclient.model.enums.Importance;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    private Message map(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setFolderId(rs.getInt("folder_id"));
        m.setSender(rs.getString("sender"));
        m.setRecipient(rs.getString("recipient"));
        m.setSubject(rs.getString("subject"));
        m.setBody(rs.getString("body"));
        m.setRead(rs.getInt("is_read") == 1);
        m.setImportance(Importance.valueOf(rs.getString("importance")));
        String ds = rs.getString("date_sent");
        m.setDateSent(ds == null ? null : LocalDateTime.parse(ds));
        m.setDraft(rs.getInt("is_draft") == 1);
        return m;
    }

    public Message getById(int id) {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException("getById помилка", e);
        }
    }

    public List<Message> getByFolder(int folderId) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE folder_id = ? ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, folderId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("getByFolder помилка", e);
        }
        return list;
    }

    public int createIncoming(int folderId, Message m) {
        String sql = """
            INSERT INTO messages
            (folder_id, sender, recipient, subject, body, is_read, importance, date_sent, is_draft)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, folderId);
            st.setString(2, m.getSender());
            st.setString(3, m.getRecipient());
            st.setString(4, m.getSubject());
            st.setString(5, m.getBody());
            st.setInt(6, m.isRead() ? 1 : 0);
            st.setString(7, m.getImportance().name());
            st.setString(8, m.getDateSent() == null ? null : m.getDateSent().toString());
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new RuntimeException("createIncoming помилка", e);
        }
    }

    public int createDraft(int folderId, Message m) {
        String sql = """
            INSERT INTO messages
            (folder_id, sender, recipient, subject, body, is_read, importance, date_sent, is_draft)
            VALUES (?, ?, ?, ?, ?, 0, ?, NULL, 1)
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, folderId);
            st.setString(2, m.getSender());
            st.setString(3, m.getRecipient());
            st.setString(4, m.getSubject());
            st.setString(5, m.getBody());
            st.setString(6, m.getImportance().name());
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new RuntimeException("createDraft помилка", e);
        }
    }

    public void updateDraft(int id, String subject, String body, String recipient, Importance importance) {
        String sql = """
            UPDATE messages
            SET subject = ?, body = ?, recipient = ?, importance = ?
            WHERE id = ? AND is_draft = 1
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, subject);
            st.setString(2, body);
            st.setString(3, recipient);
            st.setString(4, importance.name());
            st.setInt(5, id);
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("updateDraft помилка", e);
        }
    }

    public void markDraftAsSent(int id, int sentFolderId) {
        String sql = """
            UPDATE messages
            SET folder_id = ?, is_draft = 0, date_sent = ?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, sentFolderId);
            st.setString(2, LocalDateTime.now().toString());
            st.setInt(3, id);
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("markDraftAsSent помилка", e);
        }
    }

    public void updateFolder(int id, int newFolderId) {
        String sql = "UPDATE messages SET folder_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, newFolderId);
            st.setInt(2, id);
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("updateFolder помилка", e);
        }
    }

    public int copyMessage(Message m, int targetFolderId) {
        String sql = """
            INSERT INTO messages
            (folder_id, sender, recipient, subject, body, is_read, importance, date_sent, is_draft)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, targetFolderId);
            st.setString(2, m.getSender());
            st.setString(3, m.getRecipient());
            st.setString(4, m.getSubject());
            st.setString(5, m.getBody());
            st.setInt(6, m.isRead() ? 1 : 0);
            st.setString(7, m.getImportance().name());
            st.setString(8, m.getDateSent() == null ? null : m.getDateSent().toString());
            st.setInt(9, m.isDraft() ? 1 : 0);
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new RuntimeException("copyMessage помилка", e);
        }
    }

    public void setRead(int id, boolean read) {
        String sql = "UPDATE messages SET is_read = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, read ? 1 : 0);
            st.setInt(2, id);
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Помилка updateRead", e);
        }
    }

    public void updateImportance(int id, Importance importance) {
        String sql = "UPDATE messages SET importance = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, importance.name());
            st.setInt(2, id);
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("updateImportance помилка", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("delete помилка", e);
        }
    }
}
