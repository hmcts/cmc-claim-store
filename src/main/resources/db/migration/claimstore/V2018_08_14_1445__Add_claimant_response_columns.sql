ALTER TABLE claim
  ADD COLUMN claimant_response JSONB,
  ADD COLUMN claimant_responded_at TIMESTAMP;
