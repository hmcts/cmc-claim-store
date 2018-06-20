CREATE TABLE defendant_response (
  id SERIAL,
  defendant_id INTEGER NOT NULL,
  claim_id INTEGER NOT NULL,
  response JSONB,
  responded_on TIMESTAMP NOT NULL DEFAULT (now() at time zone 'utc') ,
  PRIMARY KEY (id)
);
