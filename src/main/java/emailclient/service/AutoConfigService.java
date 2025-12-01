package emailclient.service;

import emailclient.model.Account;
import emailclient.model.enums.ProviderType;
import emailclient.model.enums.ProtocolType;

import java.util.Map;

public class AutoConfigService {

    private record ProviderCfg(
            ProtocolType protocol,
            String imapHost, int imapPort,
            String pop3Host, int pop3Port,
            String smtpHost, int smtpPort) {
    }

    private static final Map<String, ProviderCfg> REGISTRY = Map.of(
            "gmail.com", new ProviderCfg(
                    ProtocolType.IMAP,
                    "imap.gmail.com", 993,
                    "pop.gmail.com", 995,
                    "smtp.gmail.com", 465
            ),
            "ukr.net", new ProviderCfg(
                    ProtocolType.IMAP,
                    "imap.ukr.net", 993,
                    "pop3.ukr.net", 995,
                    "smtp.ukr.net", 465
            ),
            "i.ua", new ProviderCfg(
                    ProtocolType.IMAP,
                    "imap.i.ua", 993,
                    "pop.i.ua", 995,
                    "smtp.i.ua", 465
            )
    );

    private static final Map<String, ProviderType> DOMAIN_TO_PROVIDER = Map.of(
            "gmail.com", ProviderType.GMAIL,
            "ukr.net",   ProviderType.UKRNET,
            "i.ua",      ProviderType.IUA
    );

    public Account setupAccount(String email, String password, int userId) {
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        ProviderCfg cfg = REGISTRY.get(domain);
        if (cfg == null) {
            throw new IllegalArgumentException("Провайдер не підтримується: " + domain);
        }

        Account acc = new Account();
        acc.setUserId(userId);
        acc.setEmailAddress(email);
        acc.setPassword(password);

        ProviderType provider = DOMAIN_TO_PROVIDER.get(domain);
        if (provider != null) {
            acc.setProvider(provider);
        }

        acc.setProtocol(cfg.protocol);

        acc.setImapHost(cfg.imapHost);
        acc.setImapPort(cfg.imapPort);

        acc.setPop3Host(cfg.pop3Host);
        acc.setPop3Port(cfg.pop3Port);

        acc.setSmtpHost(cfg.smtpHost);
        acc.setSmtpPort(cfg.smtpPort);

        return acc;
    }
}
