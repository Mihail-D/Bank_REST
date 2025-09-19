-- users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL
);

-- cards
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    encrypted_card_number VARCHAR(255) NOT NULL,
    card_holder VARCHAR(100) NOT NULL,
    expiration_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_cards_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- transactions
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    from_card_id BIGINT NOT NULL,
    to_card_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255),
    CONSTRAINT fk_transactions_from_card FOREIGN KEY (from_card_id) REFERENCES cards(id),
    CONSTRAINT fk_transactions_to_card FOREIGN KEY (to_card_id) REFERENCES cards(id)
);
