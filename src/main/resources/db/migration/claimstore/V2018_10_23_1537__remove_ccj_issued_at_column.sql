UPDATE claim
set county_court_judgment_issued_at = null;

ALTER TABLE claim
  DROP COLUMN county_court_judgment_issued_at;
