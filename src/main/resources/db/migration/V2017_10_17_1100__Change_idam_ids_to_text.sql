ALTER TABLE claim
  ALTER COLUMN submitter_id TYPE TEXT USING submitter_id :: TEXT,
  ALTER COLUMN letter_holder_id TYPE TEXT USING letter_holder_id :: TEXT,
  ALTER COLUMN defendant_id TYPE TEXT USING defendant_id :: TEXT;
