package emailclient.service;

import emailclient.model.Account;
import emailclient.repository.AccountRepository;

import java.util.List;

public class AccountService {

    private final AccountRepository repo = new AccountRepository();
    private final FolderService folderService = new FolderService();

    /**
     * Створення акаунта + створення системних папок.
     */
    public boolean createAccount(Account account) {
        int accountId = -1;

        try {
            // 1. Створюємо акаунт
            accountId = repo.add(account);

            // 2. Створюємо системні папки
            folderService.createDefaultFolders(accountId);

            return true;

        } catch (Exception e) {
            System.err.println("❌ Помилка створення акаунта: " + e.getMessage());

            // rollback акаунта (папок ще немає)
            if (accountId > 0) {
                try { repo.delete(accountId); } catch (Exception ignored) {}
            }

            return false;
        }
    }

    /**
     * Отримати акаунти конкретного користувача
     */
    public List<Account> getAccountsByUserId(int userId) {
        return repo.getAccountsByUserId(userId);
    }

    /**
     * Отримати акаунт за ID
     */
    public Account getById(int id) {
        return repo.getById(id);
    }

    /**
     * Видалити акаунт (без каскаду)
     */
    public void delete(int id) {
        repo.delete(id);
    }
}
