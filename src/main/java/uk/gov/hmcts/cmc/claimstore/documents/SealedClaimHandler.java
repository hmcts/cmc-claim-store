package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class SealedClaimHandler {

    private final DocumentManagementService documentManagementService;
    private final ClaimService claimService;

    @Autowired
    public SealedClaimHandler(final DocumentManagementService documentManagementService,
                              final ClaimService claimService) {
        this.documentManagementService = documentManagementService;
        this.claimService = claimService;
    }

    @EventListener
    public void uploadIntoDocumentManagementService(DocumentGeneratedEvent event) {
        event.getDocuments().forEach(document -> {
            final String documentSelfPath = this.documentManagementService.uploadDocument(event.getAuthorisation(),
                document.getFilename(), document.getBytes(), PDF.CONTENT_TYPE);

            if (document.getFilename().endsWith("sealed-claim.pdf")) {
                claimService.linkSealedClaimDocument(event.getClaim().getId(), documentSelfPath);
            }
        });
    }
}
