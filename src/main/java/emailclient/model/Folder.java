package emailclient.model;

import emailclient.model.enums.FolderType;

public class Folder {
    private int id;
    private int accountId;
    private String name;
    private FolderType type;

    public Folder() {}

    public Folder(int accountId, String name, FolderType type) {
        this.accountId = accountId;
        this.name = name;
        this.type = type;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public FolderType getType() { return type; }
    public void setType(FolderType type) { this.type = type; }

    @Override
    public String toString() {
        return name;
    }

}
