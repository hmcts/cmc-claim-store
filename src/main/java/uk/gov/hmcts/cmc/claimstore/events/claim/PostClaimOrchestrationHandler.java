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
import uk.gov.hmcts.cmc.domain.models.Claim;

@Async("threadPoolTaskExecutor")
@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class PostClaimOrchestrationHandler {
    private static final Logger logger = LoggerFactory.getLogger(PostClaimOrchestrationHandler.class);

    private final PinOrchestrationService pinOrchestrationService;
    private final ClaimantOperationService claimantOperationService;
    private final RpaOperationService rpaOperationService;
    private final UploadOperationService uploadOperationService;
    private final DocumentGenerationService documentGenerationService;
    private final NotifyStaffOperationService notifyStaffOperationService;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public PostClaimOrchestrationHandler(
        DocumentGenerationService documentGenerationService,
        PinOrchestrationService pinOrchestrationService,
        UploadOperationService uploadOperationService,
        ClaimantOperationService claimantOperationService,
        RpaOperationService rpaOperationService,
        NotifyStaffOperationService notifyStaffOperationService
    ) {
        this.pinOrchestrationService = pinOrchestrationService;
        this.claimantOperationService = claimantOperationService;
        this.rpaOperationService = rpaOperationService;
        this.uploadOperationService = uploadOperationService;
        this.documentGenerationService = documentGenerationService;
        this.notifyStaffOperationService = notifyStaffOperationService;
    }

    @EventListener
    public void citizenIssueHandler(CitizenClaimCreatedEvent event) {
        try {
            Claim claim = event.getClaim();
            String authorisation = event.getAuthorisation();
            String submitterName = event.getSubmitterName();

            GeneratedDocuments generatedDocuments = documentGenerationService.generateForCitizen(claim, authorisation);

            Claim updatedClaim
                = pinOrchestrationService.process(claim, authorisation, submitterName, generatedDocuments);

            updatedClaim = uploadOperationService.uploadDocument(
                updatedClaim,
                authorisation,
                generatedDocuments.getSealedClaim()
            );

            updatedClaim = uploadOperationService.uploadDocument(
                updatedClaim,
                authorisation,
                generatedDocuments.getClaimIssueReceipt()
            );

            updatedClaim = rpaOperationService.notify(updatedClaim, authorisation, generatedDocuments.getSealedClaim());
            updatedClaim = claimantOperationService.notifyCitizen(updatedClaim, submitterName, authorisation);

            //TODO update claim state
            //claimService.updateState

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
            PDF sealedClaim = generatedDocuments.getSealedClaim();

            Claim updatedClaim = uploadOperationService.uploadDocument(claim, authorisation, sealedClaim);
            updatedClaim = rpaOperationService.notify(updatedClaim, authorisation, sealedClaim);
            updatedClaim = notifyStaffOperationService.notify(updatedClaim, authorisation, sealedClaim);

            String submitterName = event.getRepresentativeName().orElse(null);
            String representativeEmail = event.getRepresentativeEmail();

            claimantOperationService
                .confirmRepresentative(updatedClaim, submitterName, representativeEmail, authorisation);

            //TODO update claim state
            //claimService.updateState

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }
}
