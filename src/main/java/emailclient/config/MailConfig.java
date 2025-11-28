package emailclient.config;

import emailclient.model.enums.ProviderType;
import emailclient.model.enums.ProtocolType;

/**
 * Клас для автоматичної конфігурації поштових акаунтів.
 * Використовує патерн Factory Method.
 */
public class MailConfig {

    private String imapHost;
    private int imapPort;

    private String pop3Host;
    private int pop3Port;

    private String smtpHost;
    private int smtpPort;

    private ProtocolType protocol;

    private MailConfig() {}

    // -------------------- GETTERS --------------------
    public String getImapHost() { return imapHost; }
    public int getImapPort() { return imapPort; }

    public String getPop3Host() { return pop3Host; }
    public int getPop3Port() { return pop3Port; }

    public String getSmtpHost() { return smtpHost; }
    public int getSmtpPort() { return smtpPort; }

    public ProtocolType getProtocol() { return protocol; }

    // -------------------- FACTORY METHOD --------------------
    public static MailConfig createConfig(ProviderType provider, ProtocolType protocol) {
        MailConfig config = new MailConfig();
        config.protocol = protocol;

        switch (provider) {
            case GMAIL -> configureGmail(config);
            case UKRNET -> configureUkrNet(config);
            case IUA -> configureIUA(config);
            default -> throw new IllegalArgumentException("Невідомий провайдер!");
        }

        return config;
    }

    // -------------------- CONFIGURATION METHODS --------------------

    private static void configureGmail(MailConfig config) {
        config.imapHost = "imap.gmail.com";
        config.imapPort = 993;

        config.pop3Host = "pop.gmail.com";
        config.pop3Port = 995;

        config.smtpHost = "smtp.gmail.com";
        config.smtpPort = 587;
    }

    private static void configureUkrNet(MailConfig config) {
        config.imapHost = "imap.ukr.net";
        config.imapPort = 993;

        config.pop3Host = "pop3.ukr.net";
        config.pop3Port = 995;

        config.smtpHost = "smtp.ukr.net";
        config.smtpPort = 465;
    }

    private static void configureIUA(MailConfig config) {
        config.imapHost = "imap.i.ua";
        config.imapPort = 993;

        config.pop3Host = "pop.i.ua";
        config.pop3Port = 995;

        config.smtpHost = "smtp.i.ua";
        config.smtpPort = 465;
    }
}
