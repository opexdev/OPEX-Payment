CREATE TABLE IF NOT EXISTS service_provider
(
    id            SERIAL PRIMARY KEY,
    name       VARCHAR(255),
    url           TEXT NOT NULL,
    access_token  TEXT NOT NULL,
    refresh_token TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS invoice
(
    id               SERIAL PRIMARY KEY,
    user_id          VARCHAR(255) NOT NULL,
    reference        VARCHAR(255) NOT NULL,
    amount           DECIMAL      NOT NULL,
    callback_url     TEXT         NOT NULL,
    currency         VARCHAR(100) NOT NULL,
    create_date      TIMESTAMP    NOT NULL,
    update_date      TIMESTAMP    NOT NULL,
    status           VARCHAR(100) NOT NULL,
    service_provider INTEGER REFERENCES service_provider (id),
    remote_status    VARCHAR(100),
    description      TEXT,
    card_number      VARCHAR(16),
    national_code    VARCHAR(10),
    UNIQUE (user_id, reference)
);

