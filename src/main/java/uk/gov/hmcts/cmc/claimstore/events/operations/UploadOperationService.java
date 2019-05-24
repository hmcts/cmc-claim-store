package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled", havingValue = "true")
public class UploadOperationService {

    private final DocumentsService documentService;

    @Autowired
    public UploadOperationService(DocumentsService documentService) {
        this.documentService = documentService;
    }

    public Claim uploadDocument(Claim claim, String authorisation, PDF document) {
        if (claim.getClaimDocument(document.getClaimDocumentType()).isPresent()) {
            return claim;
        }

        return documentService.uploadToDocumentManagement(document, authorisation, claim);
    }
}
