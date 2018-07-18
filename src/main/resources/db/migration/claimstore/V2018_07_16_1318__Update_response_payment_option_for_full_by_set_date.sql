UPDATE
  claim
SET
  response = JSONB_SET(response, '{paymentOption}',  '"BY_SPECIFIED_DATE"' :: JSONB)
WHERE response IS NOT NULL AND jsonb_extract_path_text(response, 'paymentOption') = 'FULL_BY_SPECIFIED_DATE';

UPDATE
  claim
SET
  county_court_judgment = JSONB_SET(county_court_judgment, '{paymentOption}',  '"BY_SPECIFIED_DATE"' :: JSONB)
WHERE county_court_judgment IS NOT NULL AND jsonb_extract_path_text(county_court_judgment, 'paymentOption') = 'FULL_BY_SPECIFIED_DATE';
