UPDATE
  claim
SET
  county_court_judgment = JSONB_SET(county_court_judgment, '{ccjType}',  '"DEFAULT"' :: JSONB)
WHERE response IS NULL AND county_court_judgment is not null
