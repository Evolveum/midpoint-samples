#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE USER extradb WITH PASSWORD 'qwe123';
	CREATE DATABASE extradb;
	GRANT ALL PRIVILEGES ON DATABASE extradb TO extradb;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "extradb" <<-EOSQL
CREATE TABLE portalusers (
  login             VARCHAR(16) NOT NULL,
  ldapDn            VARCHAR(128),
  fullName           VARCHAR(32),
  disabled           BOOLEAN,
  PRIMARY KEY (login)
);
GRANT ALL PRIVILEGES ON TABLE portalusers TO extradb;

CREATE TABLE crmusers (
  userId             VARCHAR(16) NOT NULL,
  password           VARCHAR(16) NOT NULL,
  firstName          VARCHAR(16),
  lastName           VARCHAR(16),
  fullName           VARCHAR(32),
  description        VARCHAR(256),
  empNo              VARCHAR(32),
  accessLevel        VARCHAR(256),
  disabled           BOOLEAN,
  PRIMARY KEY (userId)
);
GRANT ALL PRIVILEGES ON TABLE crmusers TO extradb;

INSERT INTO crmusers (userId, password, firstName, lastName, fullName, empNo, accessLevel, disabled)
VALUES ('dave', 'Jenny123', 'Dave', 'Davies', 'Dave Davies', '004', 'basic', FALSE);

INSERT INTO crmusers (userId, password, firstName, lastName, fullName, empNo, accessLevel, disabled)
VALUES ('irvine', 'IwillHAVEorder', 'Isabella', 'Irvine', 'Isabella Irvine', '009', 'manager', FALSE);

-- Non-correlable accounts, need to correlate manually

INSERT INTO crmusers (userId, password, firstName, lastName, fullName, accessLevel, disabled)
VALUES ('tom', 'L0st in Spac3', 'Thomas', 'Turner', 'Thomas Turner', 'basic', FALSE);


-- Former employees

-- this is disabled, as it should be
INSERT INTO crmusers (userId, password, firstName, lastName, fullName, empNo, accessLevel, disabled)
VALUES ('john', 'dammit!!!', 'John', 'Smith', 'John Smith', '321', 'admin', TRUE);

-- This one is still enabled
INSERT INTO crmusers (userId, password, firstName, lastName, fullName, empNo, accessLevel, disabled)
VALUES ('oscar', '!GOTCHA!', 'Oscar', 'Menace', 'Oscar Menace', '323', 'admin', FALSE);

EOSQL

