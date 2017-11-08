ALTER TABLE claim
  ADD COLUMN settlement JSONB,
  ADD COLUMN settlement_reached_at TIMESTAMP;
