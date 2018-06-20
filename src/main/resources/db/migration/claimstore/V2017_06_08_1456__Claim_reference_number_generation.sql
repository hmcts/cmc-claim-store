/**
 * This sequence will allow only a limited number of reference numbers which is a current Caseman limitation.
 * The plan is that Caseman will support our own, unlimited case reference numbers before we reach the limit.
 */
CREATE SEQUENCE claim_reference_number_seq MAXVALUE 999999 NO CYCLE;

/**
 * Returns a new reference number from a 000MC001...999MC999 range.
 */
CREATE FUNCTION next_reference_number() RETURNS TEXT AS $$
  SELECT
    regexp_replace(
      to_char(
        nextval('claim_reference_number_seq'),
      'FM000000'),
    '(\d{3})(\d{3})', '\1MC\2')
$$ LANGUAGE SQL;

DELETE FROM claim;

ALTER TABLE claim
  ADD COLUMN reference_number TEXT NOT NULL UNIQUE DEFAULT next_reference_number();
