package emailclient.service;

import emailclient.model.Account;
import emailclient.repository.AccountRepository;

import java.util.List;

public class AccountService {

    private final AccountRepository repo = new AccountRepository();
    private final FolderService folderService = new FolderService();

    public boolean createAccount(Account account) {
        int accountId = -1;

        try {
            // Створюємо акаунт
            accountId = repo.add(account);

            // Створюємо системні папки
            folderService.createDefaultFolders(accountId);

            return true;

        } catch (Exception e) {
            System.err.println("Помилка створення акаунта: " + e.getMessage());

            if (accountId > 0) {
                try { repo.delete(accountId); } catch (Exception ignored) {}
            }

            return false;
        }
    }

    public List<Account> getAccountsByUserId(int userId) {
        return repo.getAccountsByUserId(userId);
    }

    public Account getById(int id) {
        return repo.getById(id);
    }

}
