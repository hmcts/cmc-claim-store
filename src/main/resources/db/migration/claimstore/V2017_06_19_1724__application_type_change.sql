ALTER TABLE claim
 ALTER COLUMN application TYPE JSONB USING (application::jsonb);

ALTER TABLE claim
 RENAME COLUMN application TO claim;
