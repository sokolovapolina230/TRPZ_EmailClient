package emailclient.repository;

import emailclient.database.DatabaseConnection;
import emailclient.model.Account;
import emailclient.model.enums.ProtocolType;
import emailclient.model.enums.ProviderType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    public int add(Account account) {
        String sql = """
            INSERT INTO accounts
            (user_id, email_address, password, provider, protocol,
             imap_host, imap_port, pop3_host, pop3_port, smtp_host, smtp_port)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, account.getUserId());
            ps.setString(2, account.getEmailAddress());
            ps.setString(3, account.getPassword());
            ps.setString(4, account.getProvider().name());
            ps.setString(5, account.getProtocol().name());

            ps.setString(6, account.getImapHost());
            ps.setInt(7, account.getImapPort());

            ps.setString(8, account.getPop3Host());
            ps.setInt(9, account.getPop3Port());

            ps.setString(10, account.getSmtpHost());
            ps.setInt(11, account.getSmtpPort());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка додавання акаунта: " + e.getMessage(), e);
        }

        throw new RuntimeException("Не вдалося отримати ID акаунта");
    }

    public List<Account> getAccountsByUserId(int userId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ?";

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка отримання акаунтів: " + e.getMessage(), e);
        }

        return list;
    }

    public Account getById(int id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? map(rs) : null;

        } catch (SQLException e) {
            throw new RuntimeException("Помилка getById: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM accounts WHERE id = ?";

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка delete: " + e.getMessage(), e);
        }
    }

    private Account map(ResultSet rs) throws SQLException {
        Account a = new Account();

        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setEmailAddress(rs.getString("email_address"));
        a.setPassword(rs.getString("password"));

        a.setProvider(ProviderType.valueOf(rs.getString("provider")));
        a.setProtocol(ProtocolType.valueOf(rs.getString("protocol")));

        a.setImapHost(rs.getString("imap_host"));
        a.setImapPort(rs.getInt("imap_port"));
        a.setPop3Host(rs.getString("pop3_host"));
        a.setPop3Port(rs.getInt("pop3_port"));
        a.setSmtpHost(rs.getString("smtp_host"));
        a.setSmtpPort(rs.getInt("smtp_port"));

        return a;
    }
}
