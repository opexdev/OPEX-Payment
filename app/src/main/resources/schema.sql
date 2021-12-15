CREATE TABLE IF NOT EXISTS payment_gateway
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255),
    is_enabled BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS invoice
(
    id                 SERIAL PRIMARY KEY,
    user_id            VARCHAR(255) NOT NULL,
    amount             DECIMAL      NOT NULL,
    callback_url       TEXT         NOT NULL,
    currency           VARCHAR(100) NOT NULL,
    payment_gateway_id INTEGER REFERENCES payment_gateway (id),
    reference          VARCHAR(255) NOT NULL,
    gateway_request_id VARCHAR(255),
    gateway_status     VARCHAR(255),
    status             VARCHAR(100) NOT NULL,
    description        TEXT,
    mobile             VARCHAR(12),
    card_number        VARCHAR(16),
    national_code      VARCHAR(10),
    is_notified        BOOLEAN      NOT NULL,
    create_date        TIMESTAMP    NOT NULL,
    update_date        TIMESTAMP    NOT NULL,
    UNIQUE (user_id, reference)
);

