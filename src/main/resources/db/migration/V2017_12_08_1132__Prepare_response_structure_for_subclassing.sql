-- Add response type property set to full defence for all existing responses
UPDATE
  claim
SET
  response = JSONB_SET(response, '{responseType}',  '"FULL_DEFENCE"' :: JSONB)
WHERE response IS NOT NULL;

-- Add full defence type property by mapping existing response type values
UPDATE
  claim
SET
  response = JSONB_SET(response, '{defenceType}',  '"DISPUTE"' :: JSONB)
WHERE response IS NOT NULL AND jsonb_extract_path_text(response, 'response') = 'OWE_NONE';

UPDATE
  claim
SET
  response = JSONB_SET(response, '{defenceType}',  '"ALREADY_PAID"' :: JSONB)
WHERE response IS NOT NULL AND jsonb_extract_path_text(response, 'response') = 'OWE_ALL_PAID_ALL';

-- Remove old response property
WITH subquery AS (
    SELECT
      id,
      response::JSONB #- '{response}' as migrated_response
    FROM claim
    WHERE response IS NOT NULL
)
UPDATE
  claim
SET
  response = subquery.migrated_response::JSONB
FROM
  subquery
WHERE
  claim.id = subquery.id;
