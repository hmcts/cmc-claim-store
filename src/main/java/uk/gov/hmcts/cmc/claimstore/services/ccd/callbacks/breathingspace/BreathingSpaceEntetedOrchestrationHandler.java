package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.BreathingSpaceEnteredEvent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
public class BreathingSpaceEntetedOrchestrationHandler {
    private static final Logger logger = LoggerFactory.getLogger(BreathingSpaceEntetedOrchestrationHandler.class);
    private static final String FAILED_LOG_MSG = "Failed operation processing for event {}";
    private final BreathingSpaceLetterService breathingSpaceLetterService;
    private final BreathingSpaceEmailService breathingSpaceEmailService;
    private final AppInsights appInsights;

    @Autowired
    public BreathingSpaceEntetedOrchestrationHandler(
        BreathingSpaceLetterService breathingSpaceLetterService,
        BreathingSpaceEmailService breathingSpaceEmailService,
        AppInsights appInsights
    ) {
        this.breathingSpaceLetterService = breathingSpaceLetterService;
        this.breathingSpaceEmailService = breathingSpaceEmailService;
        this.appInsights = appInsights;
    }

    @LogExecutionTime
    @EventListener
    public void caseworkerBreathingSpaceEnteredEvent(BreathingSpaceEnteredEvent event) {
        try {
            Claim claim = event.getClaim();
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
            appInsights.trackEvent(
                AppInsightsEvent.BREATHING_SPACE_ENTERED,
                AppInsights.REFERENCE_NUMBER,
                claim.getReferenceNumber()
            );
        } catch (Exception e) {
            logger.error(FAILED_LOG_MSG, e);
        }
    }

    private boolean isDefendentLinked(Claim claim) {
        return !StringUtils.isBlank(claim.getDefendantId());
    }
}
