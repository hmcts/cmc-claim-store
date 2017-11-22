package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.SealedClaimToDocumentStoreUploader;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class DocumentManagementSealedClaimHandler {
    private final SealedClaimToDocumentStoreUploader sealedClaimToDocumentStoreUploader;

    @Autowired
    public DocumentManagementSealedClaimHandler(
        final SealedClaimToDocumentStoreUploader sealedClaimToDocumentStoreUploader
    ) {
        this.sealedClaimToDocumentStoreUploader = sealedClaimToDocumentStoreUploader;
    }

    @EventListener
    public void uploadCitizenSealedClaimToDocumentStore(final ClaimIssuedEvent event) {
        sealedClaimToDocumentStoreUploader
            .uploadCitizenSealedClaim(event.getAuthorisation(), event.getClaim(), event.getSubmitterEmail());

    }

    @EventListener
    public void uploadRepresentativeSealedClaimToDocumentStore(final RepresentedClaimIssuedEvent event) {
        sealedClaimToDocumentStoreUploader.uploadRepresentativeSealedClaim(event.getAuthorisation(), event.getClaim());
    }
}
