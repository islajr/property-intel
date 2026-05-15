--1. Create 'auth' schema
CREATE SCHEMA IF NOT EXISTS auths;

-- 2. Create user table
CREATE TABLE IF NOT EXISTS auths.users
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               TEXT NOT NULL UNIQUE ,
    password            TEXT NOT NULL,
    role                TEXT NOT NULL DEFAULT 'USER',
    is_email_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. Create refresh tokens table
CREATE TABLE IF NOT EXISTS auths.refresh_tokens
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES auths.users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_refresh_tokens_user ON auths.refresh_tokens(user_id);
