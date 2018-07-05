/*
 * Migrate data model for interest date
 */

-- `interest` and `interestDate` are both at the root of the claim data object.
-- Example BEFORE migration:
--
--
-- {
-- ...
--     "interest": {
--         "rate": 8,
--         "type": "standard"
--     },
--     "interestDate": {
--         "date": "2016-09-10",
--         "type": "custom",
--         "reason": "test",
--         "endDateType": "settled_or_judgment"
--     }
-- ...
-- }
--
--
-- This migration script moves `interestDate` under  `interest`.
-- Example AFTER migration:
--
--
-- {
--     ...
--         "interest": {
--             "rate": 8,
--             "type": "standard",
--             "interestDate": {
--                 "date": "2016-09-10",
--                 "type": "custom",
--                 "reason": "test",
--                 "endDateType": "settled_or_judgment"
--             }
--         }
--     ...
--     }
--

WITH subquery AS (
    SELECT id, jsonb_extract_path(response::JSONB, 'howMuchHaveYouPaid') as howMuchHaveYouPaid
      FROM claim
     WHERE jsonb_extract_path(response::JSONB, 'howMuchHaveYouPaid') IS NOT NULL
)
UPDATE
  claim
SET
  claim=jsonb_set(claim.response, '{paymentDetails}', subquery.howMuchHaveYouPaid, true) - 'howMuchHaveYouPaid'
FROM
  subquery
WHERE
  claim.id = subquery.id;
