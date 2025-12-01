package emailclient.service;

import emailclient.model.Folder;
import emailclient.model.enums.FolderType;
import emailclient.repository.FolderRepository;

import java.util.List;

public class FolderService {

    private final FolderRepository folderRepository = new FolderRepository();

    public List<Folder> getFoldersByAccount(int accountId) {
        try {
            return folderRepository.getByAccountId(accountId);
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося отримати папки акаунта", e);
        }
    }

    public void createCustomFolder(int accountId, String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Назва папки не може бути порожньою");
        }

        Folder folder = buildFolder(accountId, name.trim(), FolderType.CUSTOM);

        try {
            folderRepository.add(folder);
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося створити кастомну папку", e);
        }
    }

    public void createDefaultFolders(int accountId) {
        createSystemFolder(accountId, "Вхідні", FolderType.INBOX);
        createSystemFolder(accountId, "Відправлені", FolderType.SENT);
        createSystemFolder(accountId, "Чернетки", FolderType.DRAFTS);
        createSystemFolder(accountId, "Спам", FolderType.SPAM);
        createSystemFolder(accountId, "Кошик", FolderType.TRASH);
    }

    private void createSystemFolder(int accountId, String name, FolderType type) {
        Folder folder = buildFolder(accountId, name, type);

        try {
            folderRepository.add(folder);
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося створити системну папку", e);
        }
    }

    public void deleteFolder(Folder folder) {
        if (folder.getType().isSystem()) {
            throw new RuntimeException("Неможливо видалити системну папку!");
        }

        try {
            folderRepository.delete(folder.getId());
        } catch (Exception e) {
            throw new RuntimeException("Помилка при видаленні папки", e);
        }
    }

    public void renameFolder(Folder folder, String newName) {

        if (folder.getType().isSystem()) {
            throw new RuntimeException("Системні папки не можна перейменовувати!");
        }

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Нова назва не може бути порожньою");
        }

        folder.setName(newName.trim());

        try {
            folderRepository.update(folder);
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося перейменувати папку", e);
        }
    }

    private Folder buildFolder(int accountId, String name, FolderType type) {
        Folder folder = new Folder();
        folder.setAccountId(accountId);
        folder.setName(name);
        folder.setType(type);
        return folder;
    }

}
