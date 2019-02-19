ALTER TABLE claim
DROP COLUMN IF EXISTS sealed_claim_document_management_self_path;

ALTER TABLE claim
  ADD COLUMN claim_documents JSONB NULL;

