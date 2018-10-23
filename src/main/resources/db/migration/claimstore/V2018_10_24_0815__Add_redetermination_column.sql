ALTER TABLE claim
  ADD COLUMN redetermination JSONB,
  ADD COLUMN redetermination_requested_at TEXT;
