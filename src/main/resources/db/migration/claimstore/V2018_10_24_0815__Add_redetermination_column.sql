ALTER TABLE claim
  ADD COLUMN reDetermination JSONB,
  ADD COLUMN reDetermination_requested_at TEXT;
