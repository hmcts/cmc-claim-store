CREATE SEQUENCE claim_reference_number_seq MAXVALUE 999999 NO CYCLE;

CREATE OR REPLACE FUNCTION next_reference_number() RETURNS TEXT AS $$
SELECT
  regexp_replace(
    to_char(
      nextval('claim_reference_number_seq'),
      'FM000000'),
    '(\d{3})(\d{3})', '\1MC\2')
$$ LANGUAGE SQL;
