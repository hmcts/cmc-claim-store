package uk.gov.hmcts.cmc.claimstore.events.claim;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType;
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ADD_BULK_PRINT_DETAILS;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.HWF_APPLICATION_PENDING;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;

@Service
public class PinOrchestrationService {
    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final DocumentOrchestrationService documentOrchestrationService;
    private final ClaimService claimService;
    private final ClaimCreationEventsStatusService eventsStatusService;
    private final PrintService bulkPrintService;
    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    public PinOrchestrationService(
        PrintService bulkPrintService,
        ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService,
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties,
        ClaimCreationEventsStatusService eventsStatusService,
        DocumentOrchestrationService documentOrchestrationService,
        ClaimService claimService
    ) {
        this.bulkPrintService = bulkPrintService;
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
        this.eventsStatusService = eventsStatusService;
        this.documentOrchestrationService = documentOrchestrationService;
        this.claimService = claimService;
    }

    @LogExecutionTime
    public Claim process(Claim claim, String authorisation, String submitterName) {
        GeneratedDocuments documents = documentOrchestrationService.generateForCitizen(claim, authorisation);
        Claim updatedClaim = documents.getClaim();

        ClaimSubmissionOperationIndicators.ClaimSubmissionOperationIndicatorsBuilder updatedOperationIndicator =
            claim.getClaimSubmissionOperationIndicators().toBuilder()
                .bulkPrint(NO)
                .staffNotification(NO)
                .defendantNotification(NO);

        try {
            BulkPrintDetails bulkPrintDetails = bulkPrintService.printHtmlLetter(
                updatedClaim,
                ImmutableList.of(
                    new PrintableTemplate(
                        documents.getDefendantPinLetterDoc(),
                        buildDefendantLetterFileBaseName(claim.getReferenceNumber())),
                    new PrintableTemplate(
                        documents.getSealedClaimDoc(),
                        buildSealedClaimFileBaseName(claim.getReferenceNumber()))),
                BulkPrintRequestType.FIRST_CONTACT_LETTER_TYPE,
                authorisation
            );

            ImmutableList<BulkPrintDetails> printDetails = ImmutableList.<BulkPrintDetails>builder()
                .addAll(claim.getBulkPrintDetails())
                .add(bulkPrintDetails)
                .build();

            updatedClaim = claimService
                .addBulkPrintDetails(authorisation, printDetails, ADD_BULK_PRINT_DETAILS, updatedClaim);

            updatedOperationIndicator.bulkPrint(YesNoOption.YES);

            claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(
                updatedClaim,
                ImmutableList.of(documents.getSealedClaim(), documents.getDefendantPinLetter())
            );
            updatedOperationIndicator.staffNotification(YesNoOption.YES);

            notifyDefendant(updatedClaim, submitterName, documents);
            updatedOperationIndicator.defendantNotification(YesNoOption.YES);

        } finally {
            updatedClaim = eventsStatusService.updateClaimOperationCompletion(authorisation, updatedClaim.getId(),
                updatedOperationIndicator.build(), CaseEvent.PIN_GENERATION_OPERATIONS);
        }
        return updatedClaim;
    }

    private void notifyDefendant(Claim claim, String submitterName, GeneratedDocuments generatedDocuments) {
        if (Boolean.FALSE.equals((claim.getClaimData().isClaimantRepresented()))
            && !(claim.getState().equals(HWF_APPLICATION_PENDING))) {
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
