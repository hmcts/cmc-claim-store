package uk.gov.hmcts.cmc.claimstore.services.document;

import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentSubtype;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;

public interface DocumentsService {
    byte[] generateScannedDocument(String externalId, ScannedDocumentType scannedDocumentType,
                                   ScannedDocumentSubtype scannedDocumentSubtype, String authorisation);

    byte[] generateDocument(String externalId, ClaimDocumentType claimDocumentType, String authorisation);

    byte[] generateDocument(String externalId, ClaimDocumentType claimDocumentType, String claimDocumentId,
                            String authorisation);

    Claim uploadToDocumentManagement(PDF document, String authorisation, Claim claim);
}
