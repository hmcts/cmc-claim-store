CREATE TABLE user_roles
  (
    USER_ID TEXT NOT NULL,
    ROLE  VARCHAR(200) NOT NULL,
    PRIMARY KEY (USER_ID, ROLE)
);
