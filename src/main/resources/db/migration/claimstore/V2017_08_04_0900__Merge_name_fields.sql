/*
  Merge all first, middle and last name fields into one name field
  name: { first, middle, last } => name: {'My name'}
 */

-- Claimant

WITH subquery AS (
  SELECT
    id,
    TRIM(json_extract_path_text(claim :: JSON, 'claimant', 'name', 'first')) ||
    COALESCE(
      NULLIF(
        ' ' || TRIM(json_extract_path_text(claim :: JSON, 'claimant', 'name', 'middle')) || ' ',
        '  '),
      ' ')
    || TRIM(json_extract_path_text(claim :: JSON, 'claimant', 'name', 'last')) AS name
  FROM claim
)
UPDATE claim c
SET claim = JSONB_SET(claim, '{claimant,name}', ('"' || subquery.name || '"') :: JSONB)
FROM subquery
WHERE c.id = subquery.id;

-- Defendant on claim
WITH subquery AS (
  SELECT
    id,
    TRIM(json_extract_path_text(claim :: JSON, 'defendant', 'name', 'first')) ||
    COALESCE(
      NULLIF(
        ' ' || TRIM(json_extract_path_text(claim :: JSON, 'defendant', 'name', 'middle')) || ' ',
        '  '),
      ' ')
    || TRIM(json_extract_path_text(claim :: JSON, 'defendant', 'name', 'last')) AS name
  FROM claim
)
UPDATE claim c
SET claim = JSONB_SET(claim, '{defendant,name}', ('"' || subquery.name || '"') :: JSONB)
FROM subquery
WHERE c.id = subquery.id;

-- Defendant on response
WITH subquery AS (
  SELECT
    id,
    TRIM(json_extract_path_text(response :: JSON, 'defendantDetails', 'name', 'first')) ||
    COALESCE(
      NULLIF(
        ' ' || TRIM(json_extract_path_text(response :: JSON, 'defendantDetails', 'name', 'middle')) || ' ',
        '  '),
      ' ')
    || TRIM(json_extract_path_text(response :: JSON, 'defendantDetails', 'name', 'last')) AS name
  FROM defendant_response
)
UPDATE defendant_response r
SET response = JSONB_SET(response, '{defendantDetails,name}', ('"' || subquery.name || '"') :: JSONB)
FROM subquery
WHERE r.id = subquery.id;
