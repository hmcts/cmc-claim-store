ALTER TABLE claim
  ADD COLUMN country_court_judgment JSONB,
  ADD COLUMN country_court_judgment_requested_at TIMESTAMP;
