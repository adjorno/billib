-- Run manually on Railway Postgres before deploying.
-- No Flyway — apply with: psql <connection-url> -f V2__add_email_display_name.sql

ALTER TABLE users.profiles
    ADD COLUMN IF NOT EXISTS email        VARCHAR(255),
    ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);
