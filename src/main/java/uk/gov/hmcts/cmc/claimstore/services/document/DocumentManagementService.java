package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;

@Service
public interface DocumentManagementService<T> {

    byte[] downloadDocumentByUrl(String authorisation, String documentPath);

    byte[] downloadDocument(String authorisation, ClaimDocument claimDocument);

    byte[] downloadScannedDocument(String authorisation, ScannedDocument scannedDocument);

    T getDocumentMetaData(String authorisation, String documentPath);

    ClaimDocument uploadDocument(String authorisation, PDF pdf);

}
