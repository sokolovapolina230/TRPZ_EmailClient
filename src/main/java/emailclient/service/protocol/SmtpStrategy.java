package emailclient.service.protocol;

import emailclient.model.Account;

public class SmtpStrategy {

    private boolean connected = false;

    public void connect(Account account) {
        if (account.getSmtpHost() == null || account.getSmtpHost().isBlank()) {
            throw new IllegalStateException("SMTP сервер не налаштовано для акаунта");
        }

        System.out.println("📨 SMTP CONNECT → "
                + account.getSmtpHost() + ":" + account.getSmtpPort());

        connected = true;
    }

    public void disconnect() {
        if (connected) {
            System.out.println("🔌 SMTP DISCONNECT");
        }
        connected = false;
    }

    public void send(String from, String to, String subject, String body) {
        if (!connected) {
            throw new IllegalStateException("SMTP не підключений");
        }

        System.out.println("======================================");
        System.out.println("📤 SMTP надсилання листа:");
        System.out.println("Від: " + from);
        System.out.println("Кому: " + to);
        System.out.println("Тема: " + subject);
        System.out.println("Текст:");
        System.out.println(body);
        System.out.println("======================================");
        System.out.println("✔ SMTP: Лист 'успішно' відправлено");
    }
}
