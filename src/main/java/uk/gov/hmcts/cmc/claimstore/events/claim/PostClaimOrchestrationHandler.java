package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.NotifyStaffOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.UploadOperationService;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;

@Async("threadPoolTaskExecutor")
@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled", havingValue = "true")
public class PostClaimOrchestrationHandler {
    private static final Logger logger = LoggerFactory.getLogger(PostClaimOrchestrationHandler.class);
    private final DocumentOrchestrationService documentOrchestrationService;
    private final ClaimService claimService;

    private final Predicate<ClaimSubmissionOperationIndicators> isPinOperationSuccess = indicators ->
        Stream.of(indicators.getBulkPrint(), indicators.getStaffNotification(),
            indicators.getDefendantNotification())
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

    private final PDFBasedOperation<Claim, String, PDF, Claim> uploadSealedClaimOperation;
    private final PDFBasedOperation<Claim, String, PDF, Claim> uploadClaimIssueReceiptOperation;
    private final PDFBasedOperation<Claim, String, PDF, Claim> rpaOperation;
    private final PDFBasedOperation<Claim, String, PDF, Claim> notifyStaffOperation;
    private final NotificationOperation<Claim, CitizenClaimCreatedEvent, Claim> generatePinOperation;
    private final NotificationOperation<Claim, CitizenClaimCreatedEvent, Claim> notifyClaimantOperation;
    private final RepNotificationOperation<Claim, RepresentedClaimCreatedEvent, Claim> notifyRepresentativeOperation;

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

        generatePinOperation = (claim, event) ->
            isPinOperationSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? pinOrchestrationService.process(claim, event.getAuthorisation(), event.getAuthorisation())
                : claim;

        uploadSealedClaimOperation = (claim, authorisation, sealedClaim) ->
            isUploadSealedClaimSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? uploadOperationService
                .uploadDocument(claim, authorisation, sealedClaim, SEALED_CLAIM_UPLOAD)
                : claim;

        uploadClaimIssueReceiptOperation = (claim, authorisation, claimIssueReceipt) ->
            isUploadClaimReceiptSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? uploadOperationService
                .uploadDocument(claim, authorisation, claimIssueReceipt, CLAIM_ISSUE_RECEIPT_UPLOAD)
                : claim;

        rpaOperation = (claim, authorisation, sealedClaim) ->
            isRpaOperationSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? rpaOperationService.notify(claim, authorisation, sealedClaim)
                : claim;

        notifyStaffOperation = (claim, authorisation, sealedClaim) ->
            isNotifyStaffSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? notifyStaffOperationService.notify(claim, authorisation, sealedClaim)
                : claim;

        notifyClaimantOperation = (claim, event) ->
            isNotifyCitizenSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? claimantOperationService.notifyCitizen(claim, event.getSubmitterName(), event.getAuthorisation())
                : claim;

        notifyRepresentativeOperation = (claim, event) ->
            isNotifyCitizenSuccess.test(claim.getClaimSubmissionOperationIndicators())
                ? claimantOperationService.confirmRepresentative(
                claim,
                event.getSubmitterName(),
                event.getRepresentativeEmail(),
                event.getAuthorisation())
                : claim;
    }

    @LogExecutionTime
    @EventListener
    public void citizenIssueHandler(CitizenClaimCreatedEvent event) {
        try {
            Claim claim = event.getClaim();
            String authorisation = event.getAuthorisation();

            Function<Claim, Claim> doPinOperation = c -> generatePinOperation.perform(c, event);

            PDF sealedClaimPdf = documentOrchestrationService.getSealedClaimPdf(claim);
            PDF claimIssueReceiptPdf = documentOrchestrationService.getClaimIssueReceiptPdf(claim);

            Supplier<Claim> updatedClaim = () ->
                doPinOperation
                    .andThen(c -> uploadSealedClaimOperation.perform(c, authorisation, sealedClaimPdf))
                    .andThen(c -> uploadClaimIssueReceiptOperation.perform(c, authorisation, claimIssueReceiptPdf))
                    .andThen(c -> rpaOperation.perform(c, authorisation, sealedClaimPdf))
                    .andThen(c -> notifyClaimantOperation.perform(c, event))
                    .apply(claim);

            claimService.updateClaimState(authorisation, updatedClaim.get(), ClaimState.OPEN);

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }

    @LogExecutionTime
    @EventListener
    public void representativeIssueHandler(RepresentedClaimCreatedEvent event) {
        try {
            Claim claim = event.getClaim();
            String authorisation = event.getAuthorisation();

            GeneratedDocuments generatedDocuments = documentOrchestrationService.getSealedClaimForRepresentative(claim);
            PDF sealedClaim = generatedDocuments.getSealedClaim();

            Function<Claim, Claim> doUploadSealedClaim =
                c -> uploadSealedClaimOperation.perform(c, authorisation, sealedClaim);

            Supplier<Claim> updatedClaim = () ->
                doUploadSealedClaim
                    .andThen(c -> rpaOperation.perform(c, authorisation, sealedClaim))
                    .andThen(c -> notifyStaffOperation.perform(c, authorisation, sealedClaim))
                    .andThen(c -> notifyRepresentativeOperation.perform(c, event))
                    .apply(claim);

            claimService.updateClaimState(authorisation, updatedClaim.get(), ClaimState.OPEN);

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }
}
