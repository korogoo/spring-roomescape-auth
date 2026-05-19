CREATE TABLE IF NOT EXISTS member (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(255) NOT NULL,
    member_role VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_member UNIQUE (username)
);
