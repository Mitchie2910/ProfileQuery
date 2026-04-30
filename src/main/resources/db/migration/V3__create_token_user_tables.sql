-- =========================
-- Schema
-- =========================
CREATE SCHEMA IF NOT EXISTS mapping;

-- =========================
-- RefreshTokenMapping
-- =========================
CREATE TABLE IF NOT EXISTS mapping.refresh_table (
    token_id     VARCHAR(255) PRIMARY KEY,
    github_id    VARCHAR(255) NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at   TIMESTAMP    NOT NULL
);

-- Index on github_id
CREATE INDEX idx_refresh_table_github_id
    ON mapping.refresh_table (github_id);



-- =========================
-- User
-- =========================
CREATE TABLE IF NOT EXISTS mapping.app_user (
    id             UUID PRIMARY KEY,
    github_id      VARCHAR(255) NOT NULL UNIQUE,
    username       VARCHAR(255),
    email          VARCHAR(255),
    avatar_url     TEXT,
    role           VARCHAR(50) NOT NULL,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at  TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Index on github_id (important for OAuth lookup)
CREATE INDEX idx_user_github_id
    ON mapping.app_user (github_id);