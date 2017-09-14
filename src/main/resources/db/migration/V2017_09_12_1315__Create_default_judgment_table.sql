CREATE TABLE default_judgment (
  id SERIAL,
  claimant_id INTEGER,
  claim_id INTEGER,
  data JSONB,
  external_id TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT (now() at time zone 'utc') ,
  PRIMARY KEY (id)
);
