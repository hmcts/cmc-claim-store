
CREATE TABLE user_authorized_roles
  (
    USER_ID TEXT NOT NULL,
    ROLE  VARCHAR(200) NOT NULL,
    PRIMARY KEY (USER_ID,ROLE)
);

commit;
