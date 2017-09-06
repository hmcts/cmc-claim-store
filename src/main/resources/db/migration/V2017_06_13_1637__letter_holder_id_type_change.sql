ALTER TABLE claim
 ALTER COLUMN letter_holder_id  TYPE integer USING (letter_holder_id::integer);
