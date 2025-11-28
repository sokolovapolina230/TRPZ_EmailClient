-- Скидання старих таблиць
DROP TABLE IF EXISTS attachments;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS folders;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;

-- Створення таблиць для Email Client

-- 1. Користувачі (локальні)
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL
);

-- 2. Акаунти (поштові скриньки користувачів)
CREATE TABLE accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    email_address TEXT NOT NULL,
    password TEXT NOT NULL,

    provider TEXT NOT NULL,
    protocol TEXT NOT NULL,

    imap_host TEXT,
    imap_port INTEGER,

    pop3_host TEXT,
    pop3_port INTEGER,

    smtp_host TEXT NOT NULL,
    smtp_port INTEGER NOT NULL
);


-- 3. Папки (INBOX, SENT, DRAFTS, SPAM, TRASH, CUSTOM)
CREATE TABLE folders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);


-- 4. Повідомлення (листи)
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    folder_id INTEGER NOT NULL,
    sender TEXT NOT NULL,
    recipient TEXT,
    subject TEXT,
    body TEXT,
    is_read INTEGER NOT NULL,
    importance TEXT NOT NULL,
    date_sent TEXT,
    is_draft INTEGER NOT NULL,
    FOREIGN KEY (folder_id) REFERENCES folders(id)
);


-- 5. Вкладення
CREATE TABLE attachments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    message_id INTEGER NOT NULL,
    file_name TEXT NOT NULL,
    file_path TEXT NOT NULL,
    size INTEGER NOT NULL,
    FOREIGN KEY (message_id) REFERENCES messages(id)
);
