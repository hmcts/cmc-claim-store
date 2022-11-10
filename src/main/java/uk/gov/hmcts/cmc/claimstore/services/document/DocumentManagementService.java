package uk.gov.hmcts.cmc.claimstore.services.document;

import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

public interface DocumentManagementService {

    byte[] downloadDocumentByUrl(String authorisation, String documentPath);

    byte[] downloadDocument(String authorisation, ClaimDocument claimDocument);

    byte[] downloadScannedDocument(String authorisation, ScannedDocument scannedDocument);

    Document getDocumentMetaData(String authorisation, String documentPath);

    ClaimDocument uploadDocument(String authorisation, PDF pdf);

}
