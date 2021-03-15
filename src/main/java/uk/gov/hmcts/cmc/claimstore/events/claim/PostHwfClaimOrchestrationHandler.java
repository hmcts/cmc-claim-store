package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.UploadOperationService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.util.function.UnaryOperator;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;

@Service
public class PostHwfClaimOrchestrationHandler {
    private static final Logger logger = LoggerFactory.getLogger(PostHwfClaimOrchestrationHandler.class);

    private final DocumentOrchestrationService documentOrchestrationService;
    private final ClaimService claimService;
    private final AppInsights appInsights;

    private final PDFBasedOperation<Claim, String, PDF, Claim> uploadSealedClaimOperation;
    private final PDFBasedOperation<Claim, String, PDF, Claim> uploadClaimIssueReceiptOperation;
    private final PDFBasedOperation<Claim, String, PDF, Claim> rpaOperation;

    private final NotificationOperation<Claim, CaseworkerHwfClaimIssueEvent, Claim> generatePinOperationHwf;
    private final NotificationOperation<Claim, CaseworkerHwfClaimIssueEvent, Claim> notifyClaimantOperationHwf;
    private static final String FAILED_LOG_MSG = "Failed operation processing for event {}";

    @Autowired
    @SuppressWarnings("squid:S00107")
    public PostHwfClaimOrchestrationHandler(
        DocumentOrchestrationService documentOrchestrationService,
        PinOrchestrationService pinOrchestrationService,
        UploadOperationService uploadOperationService,
        ClaimantOperationService claimantOperationService,
        RpaOperationService rpaOperationService,
        ClaimService claimService,
        AppInsights appInsights
    ) {
        this.documentOrchestrationService = documentOrchestrationService;
        this.claimService = claimService;
        this.appInsights = appInsights;

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

        generatePinOperationHwf = (claim, event) ->
            claim.getClaimSubmissionOperationIndicators().isPinOperationSuccess()
                ? claim
                : pinOrchestrationService.process(claim, event.getAuthorisation(), event.getAuthorisation());

        notifyClaimantOperationHwf = (claim, event) ->
            claim.getClaimSubmissionOperationIndicators().getClaimantNotification() == NO
                ? claimantOperationService.notifyCitizen(claim, event.getSubmitterName(), event.getAuthorisation())
                : claim;
    }

    @LogExecutionTime
    @EventListener
    public void caseworkerHwfClaimIssueEvent(CaseworkerHwfClaimIssueEvent event) {
        try {
            Claim claim = event.getClaim();
            String authorisation = event.getAuthorisation();

            UnaryOperator<Claim> doPinOperation = c -> generatePinOperationHwf.perform(c, event);

            PDF sealedClaimPdf = documentOrchestrationService.getSealedClaimPdf(claim);
            PDF claimIssueReceiptPdf = documentOrchestrationService.getClaimIssueReceiptPdf(claim);

            Claim updatedClaim = doPinOperation
                .andThen(c -> uploadSealedClaimOperation.perform(c, authorisation, sealedClaimPdf))
                .andThen(c -> uploadClaimIssueReceiptOperation.perform(c, authorisation, claimIssueReceiptPdf))
                .andThen(c -> rpaOperation.perform(c, authorisation, sealedClaimPdf))
                .andThen(c -> notifyClaimantOperationHwf.perform(c, event))
                .apply(claim);

            if (updatedClaim.getState() == ClaimState.CREATE) {
                claimService.updateClaimState(authorisation, updatedClaim, ClaimState.OPEN);
                appInsights.trackEvent(
                    AppInsightsEvent.HWF_CLAIM_ISSUED_CITIZEN,
                    AppInsights.REFERENCE_NUMBER,
                    updatedClaim.getReferenceNumber()
                );
            }
        } catch (Exception e) {
            logger.error(FAILED_LOG_MSG, e);
        }
    }
}
