ALTER TABLE claim
  ADD COLUMN partyStatement JSONB,
  ADD COLUMN settlement_reached_at TIMESTAMP;
