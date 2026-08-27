ALTER TABLE user
    ADD COLUMN username VARCHAR(50) NULL AFTER openid,
    ADD COLUMN password VARCHAR(100) NULL AFTER username;

ALTER TABLE user
    ADD UNIQUE KEY uk_user_username (username),
    ADD UNIQUE KEY uk_user_phone (phone);
