CREATE TABLE IF NOT EXISTS member (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(255) NOT NULL,
    member_role VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_member UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS store (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    member_id BIGINT       NOT NULL,
    st_name   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_store_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE TABLE IF NOT EXISTS reservation (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    member_id BIGINT       NOT NULL,
    store_id  BIGINT       NOT NULL,
    res_date  DATE         NOT NULL,
    res_time  TIME         NOT NULL,
    theme     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_reservation_store FOREIGN KEY (store_id) REFERENCES store(id)
);
