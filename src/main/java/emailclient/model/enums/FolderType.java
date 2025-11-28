package emailclient.model.enums;

public enum FolderType {

    INBOX(true),
    SENT(true),
    DRAFTS(true),
    SPAM(true),
    TRASH(true),
    CUSTOM(false);

    private final boolean system;

    FolderType(boolean system) {
        this.system = system;
    }

    public boolean isSystem() {
        return system;
    }
}
