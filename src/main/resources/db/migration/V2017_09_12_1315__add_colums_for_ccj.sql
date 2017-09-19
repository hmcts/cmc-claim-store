ALTER TABLE claim
  ADD COLUMN county_court_judgment JSONB,
  ADD COLUMN county_court_judgment_requested_at TIMESTAMP;
