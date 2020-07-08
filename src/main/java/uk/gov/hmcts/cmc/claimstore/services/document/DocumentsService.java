package uk.gov.hmcts.cmc.claimstore.services.document;

import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

public interface DocumentsService {
    byte[] getOCON9xForm(String externalId, String authorisation);

    byte[] generateDocument(String externalId, ClaimDocumentType claimDocumentType, String authorisation);

    Claim uploadToDocumentManagement(PDF document, String authorisation, Claim claim);
}
