package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.SealedClaimToDocumentStoreUploader;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class DocumentManagementSealedClaimHandler {
    private final SealedClaimToDocumentStoreUploader sealedClaimToDocumentStoreUploader;
    private final DocumentManagementClaimIssuedStaffNotificationHandler claimIssuedStaffNotificationHandler;

    @Autowired
    public DocumentManagementSealedClaimHandler(
        final SealedClaimToDocumentStoreUploader sealedClaimToDocumentStoreUploader,
        final DocumentManagementClaimIssuedStaffNotificationHandler claimIssuedStaffNotificationHandler
    ) {
        this.sealedClaimToDocumentStoreUploader = sealedClaimToDocumentStoreUploader;
        this.claimIssuedStaffNotificationHandler = claimIssuedStaffNotificationHandler;
    }

    @EventListener
    public void uploadCitizenSealedClaimToDocumentStore(final ClaimIssuedEvent event) {
        sealedClaimToDocumentStoreUploader
            .uploadCitizenSealedClaim(event.getAuthorisation(), event.getClaim(), event.getSubmitterEmail());

        claimIssuedStaffNotificationHandler.onClaimIssued(event);
    }

    @EventListener
    public void uploadRepresentativeSealedClaimToDocumentStore(final RepresentedClaimIssuedEvent event) {
        sealedClaimToDocumentStoreUploader.uploadRepresentativeSealedClaim(event.getAuthorisation(), event.getClaim());

        claimIssuedStaffNotificationHandler.onRepresentedClaimIssued(event);
    }
}
