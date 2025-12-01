package emailclient.model;

import emailclient.model.enums.ProtocolType;
import emailclient.model.enums.ProviderType;

public class Account {

    private int id;
    private int userId;
    private String emailAddress;
    private String password;

    private ProviderType provider;
    private ProtocolType protocol;

    private String imapHost;
    private int imapPort;

    private String pop3Host;
    private int pop3Port;

    private String smtpHost;
    private int smtpPort;

    public Account() {}


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public ProviderType getProvider() { return provider; }
    public void setProvider(ProviderType provider) { this.provider = provider; }

    public ProtocolType getProtocol() { return protocol; }
    public void setProtocol(ProtocolType protocol) { this.protocol = protocol; }

    public String getImapHost() { return imapHost; }
    public void setImapHost(String imapHost) { this.imapHost = imapHost; }

    public int getImapPort() { return imapPort; }
    public void setImapPort(int imapPort) { this.imapPort = imapPort; }

    public String getPop3Host() { return pop3Host; }
    public void setPop3Host(String pop3Host) { this.pop3Host = pop3Host; }

    public int getPop3Port() { return pop3Port; }
    public void setPop3Port(int pop3Port) { this.pop3Port = pop3Port; }

    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

    public int getSmtpPort() { return smtpPort; }
    public void setSmtpPort(int smtpPort) { this.smtpPort = smtpPort; }

    @Override
    public String toString() {
        return emailAddress;
    }

}
