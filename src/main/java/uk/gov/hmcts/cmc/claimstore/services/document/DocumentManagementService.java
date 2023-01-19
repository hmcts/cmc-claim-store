package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;

import java.net.URI;

@Service
public interface DocumentManagementService<T> {

    @Retryable(value = DocumentManagementException.class, backoff = @Backoff(delay = 200))
    byte[] downloadDocumentByUrl(String authorisation, URI documentManagementUrl);

    byte[] downloadDocument(String authorisation, ClaimDocument claimDocument);

    byte[] downloadScannedDocument(String authorisation, ScannedDocument scannedDocument);

    T getDocumentMetaData(String authorisation, String documentPath);

    @Retryable(value = {DocumentManagementException.class}, backoff = @Backoff(delay = 200))
    ClaimDocument uploadDocument(String authorisation, PDF pdf);

}
