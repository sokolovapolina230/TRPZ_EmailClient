package emailclient.service;

import emailclient.model.Attachment;
import emailclient.repository.AttachmentRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

public class AttachmentService {

    private static final String ATTACH_DIR;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private final AttachmentRepository repo = new AttachmentRepository();

    static {
        String appData = System.getenv("APPDATA");

        ATTACH_DIR = (appData != null && !appData.isBlank())
                ? appData + File.separator + "EmailClient" + File.separator + "attachments"
                : System.getProperty("user.home") + File.separator + ".emailclient" + File.separator + "attachments";

        new File(ATTACH_DIR).mkdirs();
    }
    public AttachmentService() {}

    public void addAttachment(int messageId, File sourceFile) {

        if (sourceFile == null || !sourceFile.exists())
            throw new IllegalArgumentException("Файл не існує");

        if (sourceFile.length() == 0)
            throw new IllegalArgumentException("Файл порожній");

        if (sourceFile.length() > MAX_FILE_SIZE)
            throw new IllegalArgumentException("Файл завеликий (максимум 10MB)");

        String safeName = Path.of(sourceFile.getName()).getFileName().toString();
        if (safeName.contains(".."))
            throw new IllegalArgumentException("Недопустиме ім'я файла");

        String storedName = UUID.randomUUID() + "_" + safeName;
        Path targetPath = Path.of(ATTACH_DIR, storedName);

        Attachment a = new Attachment.Builder()
                .messageId(messageId)
                .fileName(safeName)
                .filePath(targetPath.toString())
                .size(sourceFile.length())
                .build();

        int id = repo.add(a);

        try {
            Files.copy(sourceFile.toPath(), targetPath,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            repo.delete(id);
            throw new RuntimeException("Помилка копіювання файла", e);
        }

    }

    public void copyAttachment(Attachment original, int newMessageId) {
        try {
            File src = new File(original.getFilePath());
            if (!src.exists()) return;

            String safeName = Path.of(original.getFileName()).getFileName().toString();
            String storedName = UUID.randomUUID() + "_" + safeName;
            Path newPath = Path.of(ATTACH_DIR, storedName);

            Files.copy(src.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);

            Attachment copy = new Attachment.Builder()
                    .messageId(newMessageId)
                    .fileName(original.getFileName())
                    .filePath(newPath.toString())
                    .size(src.length())
                    .build();

            repo.add(copy);

        } catch (Exception e) {
            throw new RuntimeException("Помилка копіювання вкладення", e);
        }
    }

    public void deleteAttachment(int attachmentId) {
        Attachment a = repo.getById(attachmentId);
        if (a == null)
            throw new IllegalArgumentException("Вкладення не знайдено");

        try {
            Files.deleteIfExists(Path.of(a.getFilePath()));
        } catch (IOException ignored) {}

        repo.delete(attachmentId);
    }

    public void deleteAllByMessageId(int messageId) {
        List<Attachment> list = repo.getByMessageId(messageId);
        for (Attachment a : list) {
            try {
                Files.deleteIfExists(Path.of(a.getFilePath()));
            } catch (IOException ignored) {}
            repo.delete(a.getId());
        }
    }

    public List<Attachment> getAttachments(int messageId) {
        return repo.getByMessageId(messageId);
    }
}
