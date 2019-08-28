CREATE SEQUENCE claim_legal_rep_reference_number_seq MAXVALUE 999999 NO CYCLE;

/**
 * Returns a new reference number from a 000LR001...999LR999 range.
 */
CREATE FUNCTION next_legal_rep_reference_number() RETURNS TEXT AS $$
SELECT
  regexp_replace(
    to_char(
      nextval('claim_legal_rep_reference_number_seq'),
      'FM000000'),
    '(\d{3})(\d{3})', '\1LR\2')
$$ LANGUAGE SQL;
