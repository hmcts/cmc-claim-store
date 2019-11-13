package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.function.Function;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;

@Async("threadPoolTaskExecutor")
@Service
public class PostClaimOrchestrationHandler {
    private static final Logger logger = LoggerFactory.getLogger(PostClaimOrchestrationHandler.class);

    private final DocumentOrchestrationService documentOrchestrationService;
    private final ClaimService claimService;

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
            claim.getClaimSubmissionOperationIndicators().isPinOperationSuccess()
                ? claim
                : pinOrchestrationService.process(claim, event.getAuthorisation(), event.getAuthorisation());

        uploadSealedClaimOperation = (claim, authorisation, sealedClaim) ->
            claim.getClaimSubmissionOperationIndicators().getSealedClaimUpload() == NO
                ? uploadOperationService.uploadDocument(claim, authorisation, sealedClaim)
                : claim;

        uploadClaimIssueReceiptOperation = (claim, authorisation, claimIssueReceipt) ->
            claim.getClaimSubmissionOperationIndicators().getClaimIssueReceiptUpload() == NO
                ? uploadOperationService.uploadDocument(claim, authorisation, claimIssueReceipt)
                : claim;

        rpaOperation = (claim, authorisation, sealedClaim) ->
            claim.getClaimSubmissionOperationIndicators().getRpa() == NO
                ? rpaOperationService.notify(claim, authorisation, sealedClaim)
                : claim;

        notifyStaffOperation = (claim, authorisation, sealedClaim) ->
            claim.getClaimSubmissionOperationIndicators().getStaffNotification() == NO
                ? notifyStaffOperationService.notify(claim, authorisation, sealedClaim)
                : claim;

        notifyClaimantOperation = (claim, event) ->
            claim.getClaimSubmissionOperationIndicators().getClaimantNotification() == NO
                ? claimantOperationService.notifyCitizen(claim, event.getSubmitterName(), event.getAuthorisation())
                : claim;

        notifyRepresentativeOperation = (claim, event) ->
            claim.getClaimSubmissionOperationIndicators().getClaimantNotification() == NO
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

            Claim updatedClaim = doPinOperation
                .andThen(c -> uploadSealedClaimOperation.perform(c, authorisation, sealedClaimPdf))
                .andThen(c -> uploadClaimIssueReceiptOperation.perform(c, authorisation, claimIssueReceiptPdf))
                .andThen(c -> rpaOperation.perform(c, authorisation, sealedClaimPdf))
                .andThen(c -> notifyClaimantOperation.perform(c, event))
                .apply(claim);

            if (updatedClaim.getState().equals(ClaimState.CREATE)) {
                claimService.updateClaimState(authorisation, updatedClaim, ClaimState.OPEN);
            }
        } catch (Exception e) {
            logger.error("Failed operation processing for event {}", event, e);
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

            Claim updatedClaim = doUploadSealedClaim
                .andThen(c -> rpaOperation.perform(c, authorisation, sealedClaim))
                .andThen(c -> notifyStaffOperation.perform(c, authorisation, sealedClaim))
                .andThen(c -> notifyRepresentativeOperation.perform(c, event))
                .apply(claim);

            if (updatedClaim.getState().equals(ClaimState.CREATE)) {
                claimService.updateClaimState(authorisation, updatedClaim, ClaimState.OPEN);
            }
        } catch (Exception e) {
            logger.error("Failed operation processing for event {}", event, e);
        }
    }
}
