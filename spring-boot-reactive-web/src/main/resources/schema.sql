
CREATE TABLE IF NOT EXISTS author (
    id bigint PRIMARY KEY auto_increment,
    fullName VARCHAR(512),
    region int
);

CREATE TABLE IF NOT EXISTS quote (
    id bigint PRIMARY KEY auto_increment,
    book VARCHAR(255),
    content TEXT,
    authorId bigint DEFAULT NULL,
    CONSTRAINT FK_authorId FOREIGN KEY (authorId) REFERENCES author(id) 
);
