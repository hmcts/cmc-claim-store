/*
 * Migrate data model needed for legal representatives
 */

-- Create claimants arrays with existing claimant as single element
WITH subquery AS (
  SELECT
    id,
    jsonb_extract_path(claim::JSONB, 'claimant') as claimant
  FROM claim
)
UPDATE
  claim
SET
  claim = JSONB_SET(claim, '{claimants}', jsonb_build_array(claimant))
FROM
  subquery
WHERE
  claim.id = subquery.id;

-- Remove claimant object which has already been moved to an array of claimants
WITH subquery AS (
  SELECT
    id,
    claim :: JSONB #- '{claimant}' as migrated_claim
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
