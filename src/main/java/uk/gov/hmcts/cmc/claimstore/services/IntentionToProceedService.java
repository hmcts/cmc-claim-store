package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

@Service
public class IntentionToProceedService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int intentionToProceedDeadline;

    private final WorkingDayIndicator workingDayIndicator;

    private final CaseSearchApi caseRepository;

    private final UserService userService;

    private final ClaimService claimService;

    private final AppInsights appInsights;

    public IntentionToProceedService(
        WorkingDayIndicator workingDayIndicator,
        CaseSearchApi caseRepository,
        UserService userService,
        ClaimService claimService,
        AppInsights appInsights,
        @Value("${intention.to.proceed.deadline:33}") int intentionToProceedDeadline
    ) {
        this.workingDayIndicator = workingDayIndicator;
        this.caseRepository = caseRepository;
        this.userService = userService;
        this.claimService = claimService;
        this.appInsights = appInsights;
        this.intentionToProceedDeadline = intentionToProceedDeadline;
    }

    @Scheduled(cron = "#{'${claim.stayed.schedule:-}' ?: '-'}")
    public void scheduledTrigger() {
        // if not weekend or bank holiday
        LocalDateTime now = LocalDateTime.now();
        if (workingDayIndicator.isWorkingDay(now.toLocalDate())) {
            checkClaimsToBeStayed(now);
        }
    }

    public void checkClaimsToBeStayed(LocalDateTime dateTime) {
        int adjustDays = dateTime.getHour() >= 16 ? 0 : 1;
        LocalDate runDate = dateTime.toLocalDate().minusDays(adjustDays);
        runDate = workingDayIndicator.getPreviousWorkingDay(runDate).minusDays(intentionToProceedDeadline);

        // get all cases that are not stayed and were created DEADLINE (if run after 4pm)
        // or DEADLINE+1 (if run before 4pm) days ago
        User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();
        Collection<Claim> claims = caseRepository.getCasesPastIntentionToProceed(anonymousCaseWorker, runDate);

        claims.forEach(claim -> {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_STAYED, REFERENCE_NUMBER, claim.getReferenceNumber());
            claimService.updateClaimState(anonymousCaseWorker.getAuthorisation(), claim, ClaimState.STAYED);
        });
    }

}
