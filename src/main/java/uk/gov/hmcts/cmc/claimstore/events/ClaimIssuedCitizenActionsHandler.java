package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;

import java.util.Optional;

@Component
public class ClaimIssuedCitizenActionsHandler {
    private static final String PDF_EXTENSION = ".pdf";
    public static final String APPLICATION_PDF = "application/pdf";
    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final DocumentManagementService documentManagementService;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    private final ClaimService claimService;
    private final boolean documentManagementFeatureEnabled;

    @Autowired
    public ClaimIssuedCitizenActionsHandler(
        final ClaimIssuedNotificationService claimIssuedNotificationService,
        final NotificationsProperties notificationsProperties,
        final DocumentManagementService documentManagementService,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService,
        final ClaimService claimService,
        @Value("${feature_toggles.document_management}") final boolean documentManagementFeatureEnabled
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
        this.documentManagementService = documentManagementService;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
        this.claimService = claimService;
        this.documentManagementFeatureEnabled = documentManagementFeatureEnabled;
    }

    @EventListener
    public void sendClaimantNotification(final ClaimIssuedEvent event) {
        final Claim claim = event.getClaim();

        claimIssuedNotificationService.sendMail(
            claim,
            event.getSubmitterEmail(),
            Optional.empty(),
            getEmailTemplates().getClaimantClaimIssued(),
            "claimant-issue-notification-" + claim.getReferenceNumber(),
            event.getSubmitterName()
        );
    }

    @EventListener
    public void uploadDocumentToEvidenceStore(final ClaimIssuedEvent event) {
        if (documentManagementFeatureEnabled) {
            final Claim claim = event.getClaim();
            final byte[] n1FormPdf = citizenSealedClaimPdfService.createPdf(claim, event.getSubmitterEmail());
            final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;

            final String documentManagementSelfPath = documentManagementService.uploadSingleDocument(
                event.getAuthorisation(), originalFileName, n1FormPdf, APPLICATION_PDF);

            claimService.linkDocumentManagement(claim.getId(), documentManagementSelfPath);
        }
    }

    @EventListener
    public void sendDefendantNotification(final ClaimIssuedEvent event) {
        final Claim claim = event.getClaim();

        if (!claim.getClaimData().isClaimantRepresented()) {
            claim.getClaimData().getDefendant().getEmail()
                .ifPresent(defendantEmail ->
                    claimIssuedNotificationService.sendMail(
                        claim,
                        defendantEmail,
                        Optional.of(event.getPin()),
                        getEmailTemplates().getDefendantClaimIssued(),
                        "defendant-issue-notification-" + claim.getReferenceNumber(),
                        event.getSubmitterName()
                    ));
        }
    }

    private EmailTemplates getEmailTemplates() {
        final NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
