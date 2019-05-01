package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Async("threadPoolTaskExecutor")
@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class ClaimCreatedOperationHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClaimCreatedOperationHandler.class);

    private final PinBasedOperationService pinBasedOperationService;
    private final ClaimantOperationService claimantOperationService;
    private final RpaOperationService rpaOperationService;
    private final UploadOperationService uploadOperationService;
    private final DocumentGenerationService documentGenerationService;
    private final NotifyStaffOperationService notifyStaffOperationService;
    private final ClaimCreationEventsStatusService eventsStatusService;
    private final ClaimService claimService;

    private final Predicate<ClaimSubmissionOperationIndicators> isPinOperationSuccess = indicators ->
        Stream.of(indicators.getDefendantNotification(), indicators.getRPA(),
            indicators.getBulkPrint(), indicators.getStaffNotification())
            .anyMatch(ind -> ind.equals(YesNoOption.NO));
    private final Predicate<ClaimSubmissionOperationIndicators> isUploadSealedClaimSuccess = indicators ->
        Stream.of(indicators.getSealedClaimUpload())
            .anyMatch(ind -> ind.equals(YesNoOption.NO));
    private final Predicate<ClaimSubmissionOperationIndicators> isUploadClaimReceiptSuccess = indicators ->
        Stream.of(indicators.getClaimIssueReceiptUpload())
            .anyMatch(ind -> ind.equals(YesNoOption.NO));
    private final Predicate<ClaimSubmissionOperationIndicators> isRpaOperationSuccess = indicators ->
        Stream.of(indicators.getRPA())
            .anyMatch(ind -> ind.equals(YesNoOption.NO));
    private final Predicate<ClaimSubmissionOperationIndicators> isNotifyStaffSuccess = indicators ->
        Stream.of(indicators.getStaffNotification())
            .anyMatch(ind -> ind.equals(YesNoOption.NO));
    private final Predicate<ClaimSubmissionOperationIndicators> isNotifyCitizenSuccess = indicators ->
        Stream.of(indicators.getClaimantNotification())
            .anyMatch(ind -> ind.equals(YesNoOption.NO));

    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> performPinOperations;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> uploadSealedClaimOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> uploadClaimIssueReceiptOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> rpaOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> notifyStaffOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> notifyCitizenOperation;
    private final ClaimCreationOperation<Claim, ClaimCreationEvent, GeneratedDocuments, Claim> notifyRepresentativeOperation;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public ClaimCreatedOperationHandler(
        DocumentGenerationService documentGenerationService,
        PinBasedOperationService pinBasedOperationService,
        UploadOperationService uploadOperationService,
        ClaimantOperationService claimantOperationService,
        RpaOperationService rpaOperationService,
        NotifyStaffOperationService notifyStaffOperationService,
        ClaimCreationEventsStatusService eventsStatusService,
        ClaimService claimService
    ) {
        this.pinBasedOperationService = pinBasedOperationService;
        this.claimantOperationService = claimantOperationService;
        this.rpaOperationService = rpaOperationService;
        this.uploadOperationService = uploadOperationService;
        this.documentGenerationService = documentGenerationService;
        this.notifyStaffOperationService = notifyStaffOperationService;
        this.eventsStatusService = eventsStatusService;
        this.claimService = claimService;

        performPinOperations = (claim, event, docs) ->
            isPinOperationSuccess.test(claim.getClaimSubmissionOperationIndicators()) ?
                pinBasedOperationService.process(claim, event.getAuthorisation(), event.getSubmitterName(), docs) : claim;

        uploadSealedClaimOperation = (claim, event, docs) ->
            isUploadSealedClaimSuccess.test(claim.getClaimSubmissionOperationIndicators()) ?
                uploadOperationService.uploadDocument(claim, event.getAuthorisation(), docs.getSealedClaim()) : claim;

        uploadClaimIssueReceiptOperation = (claim, event, docs) ->
            isUploadClaimReceiptSuccess.test(claim.getClaimSubmissionOperationIndicators()) ?
                uploadOperationService.uploadDocument(claim, event.getAuthorisation(), docs.getClaimIssueReceipt()) : claim;

        rpaOperation = (claim, event, docs) ->
            isRpaOperationSuccess.test(claim.getClaimSubmissionOperationIndicators()) ?
                rpaOperationService.notify(claim, event.getAuthorisation(), docs.getSealedClaim()) : claim;

        notifyStaffOperation = (claim, event, docs) ->
            isNotifyStaffSuccess.test(claim.getClaimSubmissionOperationIndicators()) ?
                notifyStaffOperationService.notify(claim, event.getAuthorisation(), docs.getSealedClaim(),
                    docs.getDefendantLetter()) : claim;

        notifyCitizenOperation = (claim, event, docs) ->
            isNotifyCitizenSuccess.test(claim.getClaimSubmissionOperationIndicators()) ?
                claimantOperationService.notifyCitizen(claim, event.getSubmitterName(), event.getAuthorisation()) : claim;

        notifyRepresentativeOperation = (claim, event, docs) ->
            isNotifyCitizenSuccess.test(claim.getClaimSubmissionOperationIndicators()) ?
                claimantOperationService.confirmRepresentative(claim, event.getSubmitterName(),
                    ((RepresentedClaimCreatedEvent) event).getRepresentativeEmail(), event.getAuthorisation()) : claim;
    }

    @EventListener
    public void citizenIssueHandler(CitizenClaimCreatedEvent event) {
        try {
            Claim claim = event.getClaim();
            String authorisation = event.getAuthorisation();
            Claim updatedClaim;

            GeneratedDocuments generatedDocuments = documentGenerationService.generateForCitizen(claim, authorisation);


            updatedClaim = CompletableFuture
                .supplyAsync(() -> performPinOperations.perform(claim, event, generatedDocuments))
                .thenApplyAsync(
                    pinCompletedClaim -> uploadSealedClaimOperation.perform(pinCompletedClaim, event, generatedDocuments))
                .thenApplyAsync(
                    uploadedSealedClaim -> uploadClaimIssueReceiptOperation.perform(uploadedSealedClaim, event,
                        generatedDocuments))
                .thenApplyAsync(
                    claimAfterReceiptUpload -> rpaOperation.perform(claimAfterReceiptUpload, event, generatedDocuments))
                .thenApplyAsync(
                    claimAfterRpa -> notifyCitizenOperation.perform(claimAfterRpa, event, generatedDocuments))
                .get();

            claimService.updateClaimState(authorisation, updatedClaim, ClaimState.ISSUED);

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }

    @EventListener
    public void representativeIssueHandler(RepresentedClaimCreatedEvent event) {
        try {
            Claim claim = event.getClaim();
            String authorisation = event.getAuthorisation();

            GeneratedDocuments generatedDocuments = documentGenerationService.generateForRepresentative(claim);

            Claim updatedClaim = CompletableFuture
                .supplyAsync(() -> uploadSealedClaimOperation.perform(claim, event, generatedDocuments))
                .thenApplyAsync(
                    claimAfterReceiptUpload -> rpaOperation.perform(claimAfterReceiptUpload, event, generatedDocuments))
                .thenApplyAsync(
                    claimAfterRpa -> notifyStaffOperation.perform(claimAfterRpa, event, generatedDocuments))
                .thenApplyAsync(
                    claimAfterStaffNotify ->
                        notifyRepresentativeOperation.perform(claimAfterStaffNotify, event, generatedDocuments)
                ).get();

            claimService.updateClaimState(authorisation, updatedClaim, ClaimState.ISSUED);

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }
}
