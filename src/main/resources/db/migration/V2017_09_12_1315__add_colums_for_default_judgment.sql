ALTER TABLE claim
  ADD COLUMN default_judgment JSONB,
  ADD COLUMN default_judgment_requested_at TIMESTAMP;
