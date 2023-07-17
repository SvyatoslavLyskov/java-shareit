CREATE TABLE IF NOT EXISTS users
(
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    user_name    VARCHAR         NOT NULL,
    user_email   VARCHAR   NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS requests
(
    request_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    request_description   VARCHAR       ,
    creation_time TIMESTAMP WITHOUT TIME ZONE ,
    requester_id  BIGINT REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS items
(
    item_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    item_name         VARCHAR  NOT NULL,
    item_description  VARCHAR NOT NULL,
    is_available BOOLEAN       NOT NULL,
    owner_id     BIGINT REFERENCES users (user_id),
    request_id   BIGINT REFERENCES requests (request_id)

);

CREATE TABLE IF NOT EXISTS bookings
(
    booking_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id    BIGINT REFERENCES items (item_id),
    booker_id  BIGINT REFERENCES users (user_id),
    status     VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS comments
(
    comment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    text       VARCHAR               NOT NULL,
    author_id  BIGINT REFERENCES users (user_id),
    item_id    BIGINT REFERENCES items (item_id),
    created    TIMESTAMP WITHOUT TIME ZONE NOT NULL
);