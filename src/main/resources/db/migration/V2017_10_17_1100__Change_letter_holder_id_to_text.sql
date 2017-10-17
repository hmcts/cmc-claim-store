ALTER TABLE claim
  ALTER COLUMN letter_holder_id TYPE TEXT USING letter_holder_id :: TEXT;
