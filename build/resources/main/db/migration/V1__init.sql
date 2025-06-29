CREATE TABLE IF NOT EXISTS users (
    id BIGINT,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    passwordless VARCHAR(600) NOT NULL,
    secret VARCHAR(600),
    sstatus VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE (name)
    );

CREATE TABLE IF NOT EXISTS user_seq (
  next_val BIGINT DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS diary (
    id BIGINT,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    username VARCHAR(255) NOT NULL,
    entryname VARCHAR(255) NOT NULL,
    content VARCHAR(600) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (username, entryname)
    );

CREATE TABLE IF NOT EXISTS diary_seq (
  next_val BIGINT DEFAULT NULL
);
