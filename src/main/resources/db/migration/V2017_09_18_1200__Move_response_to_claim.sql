ALTER TABLE claim
  ADD COLUMN response JSONB,
  ADD COLUMN responded_at TIMESTAMP,
  ADD COLUMN defendant_email TEXT;

UPDATE claim c
SET
  responded_at    = d.responded_at,
  response        = d.response,
  defendant_email = d.defendant_email
FROM defendant_response d
WHERE c.id = d.claim_id;

DROP TABLE defendant_response;
