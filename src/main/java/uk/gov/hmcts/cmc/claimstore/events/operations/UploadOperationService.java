package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static java.util.Collections.singletonList;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class UploadOperationService {

    private final DocumentUploadHandler documentUploadHandler;
    private final ClaimCreationEventsStatusService eventsStatusService;

    @Autowired
    public UploadOperationService(DocumentUploadHandler documentUploadHandler,
                                  ClaimCreationEventsStatusService eventsStatusService) {
        this.documentUploadHandler = documentUploadHandler;
        this.eventsStatusService = eventsStatusService;
    }

    public Claim uploadDocument(Claim claim, String authorisation, PDF document, CaseEvent caseEvent) {
        if (claim.getClaimDocument(document.getClaimDocumentType()).isPresent()) {
            return claim;
        }

        Claim updatedClaim = documentUploadHandler.uploadToDocumentManagement(
            claim, authorisation, singletonList(document));
        return eventsStatusService.updateClaimOperationCompletion(authorisation, updatedClaim, caseEvent);
    }
}
