package uk.gov.hmcts.cmc.claimstore.events.claim;

import com.google.common.collect.ImmutableList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadHandler;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import static java.util.Collections.singletonList;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class PinOrchestrationService {
    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final DocumentOrchestrationService documentOrchestrationService;
    private final DocumentUploadHandler documentUploadHandler;
    private final ClaimCreationEventsStatusService eventsStatusService;
    private final PrintService bulkPrintService;
    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    public PinOrchestrationService(
        DocumentUploadHandler documentUploadHandler,
        PrintService bulkPrintService,
        ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService,
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties,
        ClaimCreationEventsStatusService eventsStatusService,
        DocumentOrchestrationService documentOrchestrationService
    ) {
        this.documentUploadHandler = documentUploadHandler;
        this.bulkPrintService = bulkPrintService;
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
        this.eventsStatusService = eventsStatusService;
        this.documentOrchestrationService = documentOrchestrationService;
    }

    public Claim process(Claim claim, String authorisation, String submitterName) {
        Claim updatedClaim = claim;
        GeneratedDocuments documents = documentOrchestrationService.generateForCitizen(claim, authorisation);

        ClaimSubmissionOperationIndicators.ClaimSubmissionOperationIndicatorsBuilder updatedOperationIndicator =
            ClaimSubmissionOperationIndicators.builder();

        try {
            updatedClaim = documentUploadHandler.uploadToDocumentManagement(
                updatedClaim,
                authorisation,
                singletonList(documents.getDefendantLetter())
            );
            updatedOperationIndicator.defendantPinLetterUpload(YesNoOption.YES);

            bulkPrintService.print(updatedClaim, documents.getDefendantLetterDoc(),
                documents.getSealedClaimDoc());
            updatedOperationIndicator.bulkPrint(YesNoOption.YES);

            claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(
                updatedClaim,
                ImmutableList.of(documents.getSealedClaim(), documents.getDefendantLetter())
            );
            updatedOperationIndicator.staffNotification(YesNoOption.YES);

            notifyDefendant(updatedClaim, submitterName, documents);
            updatedOperationIndicator.defendantNotification(YesNoOption.YES);

        } finally {
            updatedClaim = eventsStatusService.updateClaimOperationCompletion(authorisation, updatedClaim,
                CaseEvent.PIN_GENERATION_OPERATIONS);
        }
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
