/*
 * Migrate external reference to claim for legal representatives
 */

WITH subquery AS (
  SELECT
    id,
    jsonb_extract_path(claim :: JSONB, 'externalReferenceNumber') AS externalReferenceNumber
  FROM claim
)
UPDATE
  claim
SET
  external_reference = externalReferenceNumber
FROM
  subquery
WHERE
  claim.id = subquery.id;
