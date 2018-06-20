/*
 * Changing schema for Claim
 */

-- update all rows and add type: individual to JSON of claimant
UPDATE
  claim c
SET
  claim = JSONB_SET(claim, '{claimant,type}',  '"individual"' :: JSONB);


-- update all rows and add type: individual to JSON of defendant
UPDATE
  claim c
SET
  claim = JSONB_SET(claim, '{defendant,type}',  '"individual"' :: JSONB);


-- copy all payment data to root level
WITH subquery AS (
  SELECT
    id,
    TRIM(json_extract_path_text(claim :: JSON, 'claimant', 'payment')) as payment
  FROM claim
)
UPDATE
  claim c
SET
  claim = JSONB_SET(claim, '{payment}', subquery.payment :: JSONB)
FROM
  subquery
WHERE
  c.id = subquery.id;

-- remove payment from claimant
WITH subquery AS (
  SELECT
    id,
    claim :: JSONB #- '{claimant, payment}' as migrated_claim
  FROM claim
)
UPDATE
  claim c
SET
  claim = subquery.migrated_claim :: JSONB
FROM
  subquery
WHERE
  c.id = subquery.id;

-- flatten claimant mobilePhone into a string value
WITH subquery AS (
  SELECT
    id,
    TRIM(json_extract_path_text(claim :: JSON, 'claimant', 'mobilePhone', 'number')) as mobilePhone
  FROM claim
)
UPDATE
  claim c
SET
  claim = JSONB_SET(claim, '{claimant,mobilePhone}', ('"' || subquery.mobilePhone || '"') :: JSONB)
FROM
  subquery
WHERE
  c.id = subquery.id;

-- flatten defendant email into a string value
WITH subquery AS (
  SELECT
    id,
    TRIM(json_extract_path_text(claim :: JSON, 'defendant', 'email', 'address')) as email
  FROM claim
)
UPDATE
  claim c
SET
  claim = JSONB_SET(claim, '{defendant,email}', ('"' || subquery.email || '"') :: JSONB)
FROM
  subquery
WHERE
  c.id = subquery.id;


/*
 * Changing schema for DefendantResponse
 */

-- update all rows and add type: individual to JSON of defendantDetails
UPDATE
  defendant_response dr
SET
  response = JSONB_SET(response, '{defendantDetails, type}',  '"individual"' :: JSONB);

-- update all rows and copy defendantDetails to defendant field
WITH subquery AS (
  SELECT
    id,
    TRIM(json_extract_path_text(response :: JSON, 'defendantDetails')) as defendantDetails
  FROM defendant_response
)
UPDATE
  defendant_response dr
SET
  response = JSONB_SET(response, '{defendant}', subquery.defendantDetails :: JSONB)
FROM
  subquery
WHERE
  dr.id = subquery.id;

-- remove defendantDetails key from the response data json
WITH subquery AS (
  SELECT
    id,
    response :: JSONB #- '{defendantDetails}' as migrated_defendant_response
  FROM defendant_response
)
UPDATE
  defendant_response dr
SET
  response = subquery.migrated_defendant_response :: JSONB
FROM
  subquery
WHERE
  dr.id = subquery.id;
