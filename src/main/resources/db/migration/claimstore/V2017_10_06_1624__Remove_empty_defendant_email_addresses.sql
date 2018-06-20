-- Delete defendant email address property if value is a blank string

WITH subquery AS (
  SELECT
    id,
    jsonb_agg(case value ->> 'email' when '' then value - 'email' else value end) as migrated_defendants
  FROM claim, jsonb_array_elements(claim::JSONB -> 'defendants')
  GROUP BY id
)
UPDATE
  claim
SET
  claim = JSONB_SET(claim, '{defendants}', migrated_defendants)
FROM
  subquery
WHERE
  claim.id = subquery.id;
