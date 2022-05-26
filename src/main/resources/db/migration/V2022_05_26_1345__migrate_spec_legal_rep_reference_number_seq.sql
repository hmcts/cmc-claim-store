CREATE SEQUENCE claim_SPEC_legal_rep_reference_number_seq MAXVALUE 999999 NO CYCLE;

/**
 * Returns a new reference number from a 000MC001...999MC999 range.
 */
CREATE FUNCTION next_SPEC_legal_rep_reference_number() RETURNS TEXT AS $$
SELECT
  regexp_replace(
    to_char(
      nextval('claim_SPEC_legal_rep_reference_number_seq'),
      'FM000000'),
    '(\d{3})(\d{3})', '\1MC\2')
$$ LANGUAGE SQL;
