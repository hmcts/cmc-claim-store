package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;

import java.util.Optional;

@Component
public class RepresentativeConfirmationHandler {
    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final DocumentManagementService documentManagementService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final boolean dmFeatureToggle;

    public RepresentativeConfirmationHandler(
        final ClaimIssuedNotificationService claimIssuedNotificationService,
        final NotificationsProperties notificationsProperties,
        final DocumentManagementService documentManagementService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        @Value("${feature_toggles.document_management}") final boolean dmFeatureToggle
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
        this.documentManagementService = documentManagementService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.dmFeatureToggle = dmFeatureToggle;
    }

    @EventListener
    public void sendConfirmation(RepresentedClaimIssuedEvent event) {
        final Claim claim = event.getClaim();

        claimIssuedNotificationService.sendMail(
            claim,
            event.getRepresentativeEmail(),
            Optional.empty(),
            getEmailTemplates().getRepresentativeClaimIssued(),
            "representative-issue-notification-" + claim.getReferenceNumber(),
            event.getRepresentativeName());
    }

    @EventListener
    public void uploadDocumentToEvidenceStore(final RepresentedClaimIssuedEvent event) {
        if (dmFeatureToggle) {
            final Claim claim = event.getClaim();
            final byte[] n1FormPdf = legalSealedClaimPdfService.createPdf(claim);

            documentManagementService.storeClaimN1Form(event.getAuthorisation(), claim.getId(),
                claim.getReferenceNumber(), n1FormPdf);
        }
    }

    private EmailTemplates getEmailTemplates() {
        final NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
