package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.claim.BreathingSpaceEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentOrchestrationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Async("threadPoolTaskExecutor")
@Service
@ConditionalOnProperty("feature_toggles.breathing_space")
public class BreathingSpaceEntetedOrchestrationHandler {

    private final BreathingSpaceLetterService breathingSpaceLetterService;
    private final BreathingSpaceEmailService breathingSpaceEmailService;
    private final DocumentOrchestrationService documentOrchestrationService;
    private final RpaOperationService rpaOperationService;
    private final AppInsights appInsights;

    @Autowired
    public BreathingSpaceEntetedOrchestrationHandler(
        BreathingSpaceLetterService breathingSpaceLetterService,
        BreathingSpaceEmailService breathingSpaceEmailService,
        DocumentOrchestrationService documentOrchestrationService,
        RpaOperationService rpaOperationService, AppInsights appInsights
    ) {
        this.breathingSpaceLetterService = breathingSpaceLetterService;
        this.breathingSpaceEmailService = breathingSpaceEmailService;
        this.documentOrchestrationService = documentOrchestrationService;
        this.rpaOperationService = rpaOperationService;
        this.appInsights = appInsights;
    }

    @LogExecutionTime
    @EventListener
    public void caseworkerBreathingSpaceEnteredEvent(BreathingSpaceEvent event) {
        Claim claim = event.getClaim();
        if (event.isEnteredByCitizen()) {
            breathingSpaceEmailService.sendNotificationToClaimant(claim,
                event.getClaimantEmailTemplateId());
            if (isDefendentLinked(claim)) {
                breathingSpaceEmailService.sendEmailNotificationToDefendant(claim,
                    event.getDefendantEmailTemplateId());
            } else {
                breathingSpaceLetterService.sendLetterToDefendant(event.getCcdCase(), claim,
                    event.getAuthorisation(),
                    event.getLetterTemplateId());
            }
        }
        PDF sealedClaimPdf = documentOrchestrationService.getSealedClaimPdf(claim);
        rpaOperationService.notifyBreathingSpace(claim, event.getAuthorisation(), sealedClaimPdf);
        appInsights.trackEvent(
            AppInsightsEvent.BREATHING_SPACE_ENTERED,
            AppInsights.REFERENCE_NUMBER,
            claim.getReferenceNumber()
        );
    }

    private boolean isDefendentLinked(Claim claim) {
        return !StringUtils.isBlank(claim.getDefendantId());
    }
}
