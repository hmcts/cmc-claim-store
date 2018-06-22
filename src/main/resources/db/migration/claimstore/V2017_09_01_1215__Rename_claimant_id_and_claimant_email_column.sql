ALTER TABLE claim
 RENAME COLUMN claimant_email TO submitter_email;

ALTER TABLE claim
 RENAME COLUMN claimant_id TO submitter_id;
