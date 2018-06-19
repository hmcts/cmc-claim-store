-- copy defendant date of birth to its own property
WITH subquery AS (
    SELECT
      id,
      jsonb_extract_path(county_court_judgment::JSONB, 'defendant', 'dateOfBirth') as defendant_date_of_birth
    FROM claim
    WHERE county_court_judgment IS NOT NULL
      AND jsonb_extract_path(county_court_judgment::JSONB, 'defendant', 'dateOfBirth') IS NOT NULL
)
UPDATE
  claim
SET
  county_court_judgment = JSONB_SET(county_court_judgment::JSONB, '{defendantDateOfBirth}', defendant_date_of_birth)
FROM
  subquery
WHERE
  claim.id = subquery.id;

-- remove defendant property
WITH subquery AS (
  SELECT
    id,
    county_court_judgment::JSONB #- '{defendant}' as migrated_county_court_judgment
  FROM claim
  WHERE county_court_judgment IS NOT NULL
)
UPDATE
  claim
SET
  county_court_judgment = subquery.migrated_county_court_judgment::JSONB
FROM
  subquery
WHERE
  claim.id = subquery.id;
