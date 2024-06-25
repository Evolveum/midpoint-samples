CREATE TABLE users
(
    id       SERIAL PRIMARY KEY,
    login    VARCHAR(32) NOT NULL,
    fullname VARCHAR(255),
    email    VARCHAR(255)
);

CREATE TABLE document_stores
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE accesses
(
    id                SERIAL PRIMARY KEY,
    user_id           INTEGER REFERENCES users (id) ON DELETE CASCADE ,
    document_store_id INTEGER REFERENCES document_stores (id) ON DELETE CASCADE ,
    level             VARCHAR(255) NOT NULL
);

CREATE VIEW accesses_view AS
SELECT a.id     AS access_id,
       u.login  AS user_login,
       d.name   AS document_name,
       a.level AS access
FROM accesses a
         JOIN users u on u.id = a.user_id
         JOIN document_stores D on D.id = a.document_store_id;

INSERT INTO users (login, fullname, email)
VALUES ('alice', 'Alice Green', 'alice@evolveum.com');
INSERT INTO users (login, fullname, email)
VALUES ('bob', 'Bob Black', 'bob@evolveum.com');

INSERT INTO document_stores (name, description)
VALUES ('guide', 'Evolveum Guide');
INSERT INTO document_stores (name, description)
VALUES ('development', 'Development');
INSERT INTO document_stores (name, description)
VALUES ('marketing', 'Marketing');

INSERT INTO accesses (user_id, document_store_id, level)
VALUES
    (1, 1, 'read'),
    (1, 1, 'write'),
    (2, 1, 'read'),
    (2, 2, 'admin'),
    (2, 3, 'read'),
    (2, 3, 'write');
