ALTER TABLE claim
  ADD COLUMN re_determination JSONB,
  ADD COLUMN re_determination_requested_at TEXT;
