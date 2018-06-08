ALTER TABLE defendant_response
  ADD COLUMN defendant_email TEXT;

UPDATE defendant_response set defendant_email = 'null@null.com';

ALTER TABLE defendant_response
  ALTER COLUMN defendant_email SET NOT NULL;
