package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

@Service
public class IntentionToProceedService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WorkingDayIndicator workingDayIndicator;

    private final CaseSearchApi caseSearchApi;

    private final UserService userService;

    private final CaseRepository caseRepository;

    private final AppInsights appInsights;

    private final IntentionToProceedDeadlineCalculator intentionToProceedDeadlineCalculator;

    public IntentionToProceedService(
        WorkingDayIndicator workingDayIndicator,
        CaseSearchApi caseSearchApi,
        UserService userService,
        AppInsights appInsights,
        CaseRepository caseRepository,
        IntentionToProceedDeadlineCalculator intentionToProceedDeadlineCalculator
    ) {
        this.workingDayIndicator = workingDayIndicator;
        this.caseSearchApi = caseSearchApi;
        this.userService = userService;
        this.caseRepository = caseRepository;
        this.appInsights = appInsights;
        this.intentionToProceedDeadlineCalculator = intentionToProceedDeadlineCalculator;
    }

    @Scheduled(cron = "#{'${claim.stayed.schedule:-}' ?: '-'}")
    public void scheduledTrigger() {
        // if not weekend or bank holiday
        LocalDateTime now = LocalDateTime.now();
        if (workingDayIndicator.isWorkingDay(now.toLocalDate())) {
            checkClaimsPastIntentionToProceedDeadline(now);
        }
    }

    public void checkClaimsPastIntentionToProceedDeadline(LocalDateTime dateTime) {
        int adjustDays = dateTime.getHour() >= 16 ? 0 : 1;
        LocalDate runDate = dateTime.toLocalDate().minusDays(adjustDays);
        LocalDate responseDate = intentionToProceedDeadlineCalculator.calculateResponseDate(runDate);

        // get all cases that are not stayed and were created DEADLINE (if run after 4pm)
        // or DEADLINE+1 (if run before 4pm) days ago
        User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();
        Collection<Claim> claims = caseSearchApi.getClaimsPastIntentionToProceed(anonymousCaseWorker, responseDate);

        claims.forEach(claim -> updateClaim(anonymousCaseWorker, claim));
    }

    private void updateClaim(User anonymousCaseWorker, Claim claim) {
        try {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_STAYED, REFERENCE_NUMBER, claim.getReferenceNumber());
            caseRepository.saveCaseEvent(
                anonymousCaseWorker.getAuthorisation(),
                claim,
                CaseEvent.INTENTION_TO_PROCEED_DEADLINE_PASSED
            );
        } catch (Exception e) {
            logger.error(String.format("Error whilst staying claim %s", claim.getId()), e);
        }
    }
}
