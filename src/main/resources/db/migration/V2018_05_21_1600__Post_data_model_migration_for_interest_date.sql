/*
 * Post migration of data model for interest date.
 *
 * Further cleanup of records that have been created with `interestDate` at
 * the root to allow backward compatibility of Claim Store till merge of:
 *
 * PR [ROC-3429: Remove backward compatibility for `interestDate`](https://github.com/hmcts/cmc-claim-store/pull/353)
 *
 *
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
    SELECT id, jsonb_extract_path(claim::JSONB, 'interestDate') as interestDate
      FROM claim
     WHERE jsonb_extract_path(claim::JSONB, 'interestDate') IS NOT NULL
)
UPDATE
  claim
SET
  claim=jsonb_set(claim.claim, '{interest, interestDate}', subquery.interestDate, true) - 'interestDate'
FROM
  subquery
WHERE
  claim.id = subquery.id;
