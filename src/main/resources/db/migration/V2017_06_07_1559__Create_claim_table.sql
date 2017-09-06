CREATE TABLE claim (
  id SERIAL,
  user_id INTEGER,
  application TEXT,
  created_on TIMESTAMP NOT NULL DEFAULT (now() at time zone 'utc') ,
  letter_holder_id VARCHAR(255),
  PRIMARY KEY (id)
);
