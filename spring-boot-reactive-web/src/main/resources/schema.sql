
CREATE TABLE IF NOT EXISTS author (
    id bigint PRIMARY KEY auto_increment,
    full_name VARCHAR(512),
    region VARCHAR(512)
);

CREATE TABLE IF NOT EXISTS quote (
    id bigint PRIMARY KEY auto_increment,
    book VARCHAR(255),
    content TEXT,
    author_id bigint DEFAULT NULL,
    CONSTRAINT FK_authorId FOREIGN KEY (author_id) REFERENCES author(id) 
);
