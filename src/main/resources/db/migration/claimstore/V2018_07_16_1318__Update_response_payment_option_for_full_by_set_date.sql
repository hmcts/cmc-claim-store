UPDATE
  claim
SET
  response = JSONB_SET(response, '{paymentOption}',  '"BY_SPECIFIED_DATE"' :: JSONB)
WHERE response IS NOT NULL AND jsonb_extract_path_text(response, 'paymentOption') = 'FULL_BY_SPECIFIED_DATE';
