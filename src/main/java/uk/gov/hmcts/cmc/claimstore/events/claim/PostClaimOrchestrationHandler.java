package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.events.ClaimCreationEvent;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.NotifyStaffOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.UploadOperationService;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Async("threadPoolTaskExecutor")
@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled", havingValue = "true")
public class PostClaimOrchestrationHandler {
    private static final Logger logger = LoggerFactory.getLogger(PostClaimOrchestrationHandler.class);
    private final DocumentOrchestrationService documentOrchestrationService;
    private final ClaimService claimService;

    private final Predicate<ClaimSubmissionOperationIndicators> isPinOperationSuccess = indicators ->
        Stream.of(indicators.getBulkPrint(), indicators.getStaffNotification()
                , indicators.getDefendantNotification())
            .anyMatch(ind -> ind.equals(YesNoOption.NO));
    private final Predicate<ClaimSubmissionOperationIndicators> isUploadSealedClaimSuccess = indicators ->
        indicators.getSealedClaimUpload().equals(YesNoOption.NO);
    private final Predicate<ClaimSubmissionOperationIndicators> isUploadClaimReceiptSuccess = indicators ->
        indicators.getClaimIssueReceiptUpload().equals(YesNoOption.NO);
    private final Predicate<ClaimSubmissionOperationIndicators> isRpaOperationSuccess = indicators ->
        indicators.getRpa().equals(YesNoOption.NO);
    private final Predicate<ClaimSubmissionOperationIndicators> isNotifyStaffSuccess = indicators ->
        indicators.getStaffNotification().equals(YesNoOption.NO);
    private final Predicate<ClaimSubmissionOperationIndicators> isNotifyCitizenSuccess = indicators ->
        indicators.getClaimantNotification().equals(YesNoOption.NO);

    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> performPinOperations;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim>
        uploadSealedClaimOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim>
        uploadClaimIssueReceiptOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> rpaOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> notifyStaffOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> notifyCitizenOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim>
        notifyRepresentativeOperation;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public PostClaimOrchestrationHandler(
        DocumentOrchestrationService documentOrchestrationService,
        PinOrchestrationService pinOrchestrationService,
        UploadOperationService uploadOperationService,
        ClaimantOperationService claimantOperationService,
        RpaOperationService rpaOperationService,
        NotifyStaffOperationService notifyStaffOperationService,
        ClaimService claimService
    ) {
        this.documentOrchestrationService = documentOrchestrationService;
        this.claimService = claimService;

        performPinOperations = (claim, event, docs) ->
            isPinOperationSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? pinOrchestrationService.process(claim, event.getAuthorisation(), event.getSubmitterName())
                : claim;

        uploadSealedClaimOperation = (claim, event, docs) ->
            isUploadSealedClaimSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? uploadOperationService.uploadDocument(claim, event.getAuthorisation(), docs.getSealedClaim(),
                CaseEvent.SEALED_CLAIM_UPLOAD)
                : claim;

        uploadClaimIssueReceiptOperation = (claim, event, docs) ->
            isUploadClaimReceiptSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? uploadOperationService.uploadDocument(claim, event.getAuthorisation(), docs.getClaimIssueReceipt(),
                CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD)
                : claim;

        rpaOperation = (claim, event, docs) ->
            isRpaOperationSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? rpaOperationService.notify(claim, event.getAuthorisation(), docs.getSealedClaim())
                : claim;

        notifyStaffOperation = (claim, event, docs) ->
            isNotifyStaffSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? notifyStaffOperationService.notify(claim, event.getAuthorisation(), docs.getSealedClaim(),
                docs.getDefendantPinLetter())
                : claim;

        notifyCitizenOperation = (claim, event, docs) ->
            isNotifyCitizenSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? claimantOperationService.notifyCitizen(claim, event.getSubmitterName(), event.getAuthorisation())
                : claim;

        notifyRepresentativeOperation = (claim, event, docs) ->
            isNotifyCitizenSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? claimantOperationService.confirmRepresentative(claim, event.getSubmitterName(),
                ((RepresentedClaimCreatedEvent) event).getRepresentativeEmail(), event.getAuthorisation())
                : claim;
    }

    @EventListener
    public void citizenIssueHandler(CitizenClaimCreatedEvent event) {
        try {
            String authorisation = event.getAuthorisation();

            GeneratedDocuments generatedDocuments =
                documentOrchestrationService.generateForCitizen(event.getClaim(), authorisation);

            Function<Claim, Claim> doPinOperation = claimPassed ->
                performPinOperations.perform(claimPassed, event, generatedDocuments);

            Supplier<Claim> updatedClaim = () -> doPinOperation
                .andThen(claim -> uploadSealedClaimOperation.perform(claim, event, generatedDocuments))
                .andThen(claim -> uploadClaimIssueReceiptOperation.perform(claim, event, generatedDocuments))
                .andThen(claim -> rpaOperation.perform(claim, event, generatedDocuments))
                .andThen(claim -> notifyCitizenOperation.perform(claim, event, generatedDocuments))
                .apply(event.getClaim());

            claimService.updateClaimState(authorisation, updatedClaim.get(), ClaimState.ISSUED);

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }

    @EventListener
    public void representativeIssueHandler(RepresentedClaimCreatedEvent event) {
        try {
            String authorisation = event.getAuthorisation();

            GeneratedDocuments generatedDocuments = documentOrchestrationService
                .getSealedClaimForRepresentative(event.getClaim());

            Function<Claim, Claim> doUploadSealedClaim = claimPassed ->
                uploadSealedClaimOperation.perform(event.getClaim(), event, generatedDocuments);

            Supplier<Claim> updatedClaim = () -> doUploadSealedClaim
                .andThen(claim -> rpaOperation.perform(claim, event, generatedDocuments))
                .andThen(claim -> notifyStaffOperation.perform(claim, event, generatedDocuments))
                .andThen(claim -> notifyRepresentativeOperation.perform(claim, event, generatedDocuments))
                .apply(event.getClaim());

            claimService.updateClaimState(authorisation, updatedClaim.get(), ClaimState.ISSUED);

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }
}
