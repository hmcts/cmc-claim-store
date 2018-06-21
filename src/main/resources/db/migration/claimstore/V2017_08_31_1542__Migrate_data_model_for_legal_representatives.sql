/*
 * Migrate data model needed for legal representatives
 */

-- Create defendants arrays with existing defendant as single element
WITH subquery AS (
  SELECT
    id,
    jsonb_extract_path(claim::JSONB, 'defendant') as defendant
  FROM claim
)
UPDATE
  claim
SET
  claim = JSONB_SET(claim, '{defendants}', jsonb_build_array(defendant))
FROM
  subquery
WHERE
  claim.id = subquery.id;

-- Remove defendant object which has already been moved to an array of defendants
WITH subquery AS (
  SELECT
    id,
    claim :: JSONB #- '{defendant}' as migrated_claim
  FROM claim
)
UPDATE
  claim
SET
  claim = subquery.migrated_claim :: JSONB
FROM
  subquery
WHERE
  claim.id = subquery.id;

-- Add amount type property to existing amount objects
UPDATE
  claim
SET
  claim = JSONB_SET(claim, '{amount,type}',  '"breakdown"' :: JSONB);
