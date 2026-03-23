-- V3__create_users_and_tokens.sql
CREATE TABLE IF NOT EXISTS tb_users
(
    id       BIGSERIAL PRIMARY KEY,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50),
    status   VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS tb_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(500) UNIQUE,
    token_type VARCHAR(50),
    revoked    BOOLEAN,
    expired    BOOLEAN,
    user_id    BIGINT,
    FOREIGN    KEY (user_id) REFERENCES tb_users (id)
);
