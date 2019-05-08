package uk.gov.hmcts.cmc.claimstore.events.claim;

import com.google.common.collect.ImmutableList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class PinOrchestrationService {
    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final DocumentOrchestrationService documentOrchestrationService;
    private final PrintService bulkPrintService;
    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    public PinOrchestrationService(
        PrintService bulkPrintService,
        ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService,
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties,
        DocumentOrchestrationService documentOrchestrationService
    ) {
        this.bulkPrintService = bulkPrintService;
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
        this.documentOrchestrationService = documentOrchestrationService;
    }

    public Claim process(Claim claim, String authorisation, String submitterName) {
        Claim updatedClaim = claim;
        GeneratedDocuments documents = documentOrchestrationService.generateForCitizen(claim, authorisation);
        bulkPrintService.print(claim, documents.getDefendantPinLetterDoc(), documents.getSealedClaimDoc());
        claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(
            updatedClaim,
            ImmutableList.of(documents.getSealedClaim(), documents.getDefendantPinLetter())
        );
        notifyDefendant(claim, submitterName, documents);
        return updatedClaim;
    }

    private void notifyDefendant(Claim claim, String submitterName, GeneratedDocuments generatedDocuments) {
        if (!claim.getClaimData().isClaimantRepresented()) {
            claim.getClaimData().getDefendant().getEmail().ifPresent(defendantEmail ->
                claimIssuedNotificationService.sendMail(
                    claim,
                    defendantEmail,
                    generatedDocuments.getPin(),
                    notificationsProperties.getTemplates().getEmail().getDefendantClaimIssued(),
                    "defendant-issue-notification-" + claim.getReferenceNumber(),
                    submitterName
                ));
        }
    }
}
