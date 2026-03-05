-- Run manually on Railway Postgres before deploying the backend with user sync support.
-- No Flyway — apply with: psql <connection-url> -f V1__create_users_schema.sql

CREATE SCHEMA IF NOT EXISTS users;

CREATE TABLE IF NOT EXISTS users.profiles (
    firebase_uid   VARCHAR(128) PRIMARY KEY,
    is_anonymous   BOOLEAN      NOT NULL DEFAULT TRUE,
    plan_type      VARCHAR(20)  NOT NULL DEFAULT 'free',
    last_login_at  TIMESTAMP    NOT NULL DEFAULT now(),
    created_at     TIMESTAMP    NOT NULL DEFAULT now()
);
