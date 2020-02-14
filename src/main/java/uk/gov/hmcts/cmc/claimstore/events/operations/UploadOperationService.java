package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
public class UploadOperationService {

    private final DocumentsService documentService;

    @Autowired
    public UploadOperationService(DocumentsService documentService) {
        this.documentService = documentService;
    }

    @LogExecutionTime
    public Claim uploadDocument(Claim claim, String authorisation, PDF document) {
        if (claim.getClaimDocument(document.getClaimDocumentType()).isPresent()) {
            return claim;
        }

        return documentService.uploadToDocumentManagement(document, authorisation, claim);
    }
}
