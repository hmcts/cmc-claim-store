package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

@Component
public class RepresentativeConfirmationHandler {
    private static final String PDF_EXTENSION = ".pdf";
    public static final String APPLICATION_PDF = "application/pdf";
    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final DocumentManagementService documentManagementService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final ClaimService claimService;
    private final boolean documentManagementFeatureEnabled;

    public RepresentativeConfirmationHandler(
        final ClaimIssuedNotificationService claimIssuedNotificationService,
        final NotificationsProperties notificationsProperties,
        final DocumentManagementService documentManagementService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final ClaimService claimService,
        @Value("${feature_toggles.document_management}") final boolean documentManagementFeatureEnabled
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
        this.documentManagementService = documentManagementService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.claimService = claimService;
        this.documentManagementFeatureEnabled = documentManagementFeatureEnabled;
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
        if (documentManagementFeatureEnabled) {
            final Claim claim = event.getClaim();
            final byte[] n1FormPdf = legalSealedClaimPdfService.createPdf(claim);
            final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;

            final String documentManagementSelfPath = documentManagementService.uploadSingleDocument(
                event.getAuthorisation(), originalFileName, n1FormPdf, APPLICATION_PDF);

            claimService.linkDocumentManagement(claim.getId(), documentManagementSelfPath);
        }
    }

    private EmailTemplates getEmailTemplates() {
        final NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
