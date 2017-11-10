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
import uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;

import java.util.Optional;

@Component
public class ClaimIssuedCitizenActionsHandler {
    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final DocumentManagementService documentManagementService;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;
    private final boolean dmFeatureToggle;

    @Autowired
    public ClaimIssuedCitizenActionsHandler(
        final ClaimIssuedNotificationService claimIssuedNotificationService,
        final NotificationsProperties notificationsProperties,
        final DocumentManagementService documentManagementService,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService,
        @Value("${feature_toggles.document_management}") final boolean dmFeatureToggle
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
        this.documentManagementService = documentManagementService;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
        this.dmFeatureToggle = dmFeatureToggle;
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
    public void handleDocumentUpload(final ClaimIssuedEvent event) {
        if (dmFeatureToggle) {
            final Claim claim = event.getClaim();
            final byte[] n1FormPdf = citizenSealedClaimPdfService.createPdf(claim, event.getSubmitterEmail());
            documentManagementService.storeClaimN1Form(event.getAuthorisation(), claim, n1FormPdf);
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
