-- =============================================================================
-- JouleTracker Platform - Database Migration V001
-- Description: Align MySQL Schema with Spring Boot 4 / Hibernate 7 JPA Specification
-- Target DBMS: MySQL 8.0.46+
-- Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- Safety: Non-destructive; preserves existing roles, tables, and constraints.
-- Idempotency: Fully idempotent; safe to execute multiple times.
-- Note on UUIDs: Identifiers are generated and assigned by Java (UUID.randomUUID()).
-- =============================================================================

USE `jouletracker_db`;

-- -----------------------------------------------------------------------------
-- 1. Table `users`: Add email_verified and set default for status
-- -----------------------------------------------------------------------------
SET @col_email_verified_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'users' 
      AND COLUMN_NAME = 'email_verified'
);

SET @sql_users_email_verified = IF(@col_email_verified_exists = 0,
    'ALTER TABLE `users` ADD COLUMN `email_verified` TINYINT(1) NOT NULL DEFAULT 0 AFTER `password_hash`',
    'SELECT "Column users.email_verified already exists" AS `status_info`'
);
PREPARE stmt_users_ev FROM @sql_users_email_verified;
EXECUTE stmt_users_ev;
DEALLOCATE PREPARE stmt_users_ev;

ALTER TABLE `users`
    MODIFY COLUMN `status` VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING_VERIFICATION';

-- -----------------------------------------------------------------------------
-- 2. Table `user_roles`: Ensure column types match JPA specification
-- -----------------------------------------------------------------------------
ALTER TABLE `user_roles`
    MODIFY COLUMN `id` CHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY COLUMN `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- -----------------------------------------------------------------------------
-- 3. Table `email_verification_tokens`: Create if missing, or align user_id to CHAR(36)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `email_verification_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `token` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `user_id` CHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `expires_at` DATETIME(6) NOT NULL,
    `used_at` DATETIME(6) DEFAULT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tokens_token` (`token`),
    KEY `idx_tokens_token` (`token`),
    KEY `idx_tokens_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ensure user_id column is CHAR(36) matching users.id
ALTER TABLE `email_verification_tokens`
    MODIFY COLUMN `user_id` CHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

-- -----------------------------------------------------------------------------
-- 4. Foreign Key: email_verification_tokens.user_id -> users.id
-- -----------------------------------------------------------------------------
-- Count orphaned tokens whose user_id does not exist in users.id
SET @orphaned_tokens_count = (
    SELECT COUNT(*) 
    FROM `email_verification_tokens` 
    WHERE `user_id` NOT IN (SELECT `id` FROM `users`)
);

-- Check if foreign key constraint already exists
SET @fk_tokens_user_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'email_verification_tokens'
      AND CONSTRAINT_NAME = 'fk_email_verification_tokens_user'
);

-- If orphaned tokens exist, do NOT create constraint; require manual review.
-- Otherwise, create constraint idempotently.
SET @sql_fk_tokens_user = IF(@orphaned_tokens_count > 0,
    'SELECT CONCAT("MIGRATION_PAUSED: Found ", @orphaned_tokens_count, " orphaned token(s) in email_verification_tokens. Manual review required before creating foreign key.") AS `status_info`',
    IF(@fk_tokens_user_exists = 0,
        'ALTER TABLE `email_verification_tokens` ADD CONSTRAINT `fk_email_verification_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE',
        'SELECT "Constraint fk_email_verification_tokens_user already exists" AS `status_info`'
    )
);
PREPARE stmt_fk_tokens FROM @sql_fk_tokens_user;
EXECUTE stmt_fk_tokens;
DEALLOCATE PREPARE stmt_fk_tokens;

-- -----------------------------------------------------------------------------
-- 5. Verification query to confirm schema integrity
-- -----------------------------------------------------------------------------
SELECT 
    TABLE_NAME, 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME IN ('users', 'roles', 'user_roles', 'email_verification_tokens')
ORDER BY TABLE_NAME, ORDINAL_POSITION;
