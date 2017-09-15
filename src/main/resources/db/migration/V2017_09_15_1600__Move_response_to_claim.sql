ALTER TABLE claim
  ADD COLUMN response JSONB,
  ADD COLUMN respondedAt TIMESTAMP NOT NULL DEFAULT (now() at time zone 'utc'),
  ADD COLUMN defendant_email TEXT;

