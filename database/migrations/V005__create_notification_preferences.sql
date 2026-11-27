CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id BIGINT PRIMARY KEY,
    email_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE
);
