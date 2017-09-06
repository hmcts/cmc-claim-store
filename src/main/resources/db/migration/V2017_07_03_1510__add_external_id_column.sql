ALTER TABLE claim
 ADD COLUMN external_id VARCHAR(37);

UPDATE claim SET external_id = md5(random()::text)::uuid;

ALTER TABLE claim
 ALTER COLUMN external_id SET NOT NULL,
 ADD CONSTRAINT external_id_unique UNIQUE (external_id);
