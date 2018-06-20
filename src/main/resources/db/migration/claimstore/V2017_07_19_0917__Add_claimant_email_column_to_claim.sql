ALTER TABLE claim
  ADD COLUMN claimant_email TEXT;

UPDATE claim set claimant_email = 'null@null.com';

ALTER TABLE claim
  ALTER COLUMN claimant_email SET NOT NULL;
