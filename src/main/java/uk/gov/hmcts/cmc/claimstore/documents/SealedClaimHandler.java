package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.SealedClaimGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class SealedClaimHandler {

    private static final String EXTENSION = ".pdf";
    private static final String CONTENT_TYPE = "application/pdf";

    private final DocumentManagementService documentManagementService;
    private final ClaimService claimService;

    @Autowired
    public SealedClaimHandler(final DocumentManagementService documentManagementService,
                              final ClaimService claimService) {
        this.documentManagementService = documentManagementService;
        this.claimService = claimService;
    }

    @EventListener
    public void uploadIntoDocumentManagementService(SealedClaimGeneratedEvent event) {
        final String fileName = event.getClaim().getReferenceNumber() + EXTENSION;

        final String documentSelfPath = this.documentManagementService.uploadDocument(event.getAuthorisation(),
            fileName, event.getDocument(), CONTENT_TYPE);
        claimService.linkSealedClaimDocument(event.getClaim().getId(), documentSelfPath);
    }

}
