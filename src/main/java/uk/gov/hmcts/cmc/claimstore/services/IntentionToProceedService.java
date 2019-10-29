package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.IntentionToProceedContentProvider;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

@Service
public class IntentionToProceedService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WorkingDayIndicator workingDayIndicator;

    private final CaseSearchApi caseSearchApi;

    private final UserService userService;

    private final CaseRepository caseRepository;

    private final AppInsights appInsights;

    private final IntentionToProceedContentProvider emailContentProvider;

    private final EmailService emailService;

    private final StaffEmailProperties emailProperties;

    private final IntentionToProceedDeadlineCalculator intentionToProceedDeadlineCalculator;

    public IntentionToProceedService(
        WorkingDayIndicator workingDayIndicator,
        CaseSearchApi caseSearchApi,
        UserService userService,
        AppInsights appInsights,
        CaseRepository caseRepository,
        IntentionToProceedContentProvider emailContentProvider,
        EmailService emailService,
        StaffEmailProperties emailProperties,
        IntentionToProceedDeadlineCalculator intentionToProceedDeadlineCalculator
    ) {
        this.workingDayIndicator = workingDayIndicator;
        this.caseSearchApi = caseSearchApi;
        this.userService = userService;
        this.caseRepository = caseRepository;
        this.appInsights = appInsights;
        this.emailContentProvider = emailContentProvider;
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.intentionToProceedDeadlineCalculator = intentionToProceedDeadlineCalculator;
    }

    @Scheduled(cron = "#{'${claim_stayed.schedule}' ?: '-'}")
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

        Collection<Claim> failedClaims = claims.stream()
            .map(claim -> updateClaim(anonymousCaseWorker, claim))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (!failedClaims.isEmpty()) {
            sendFailedNotification(failedClaims);
        }
    }

    private Claim updateClaim(User anonymousCaseWorker, Claim claim) {
        try {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_STAYED, REFERENCE_NUMBER, claim.getReferenceNumber());
            caseRepository.saveCaseEvent(
                anonymousCaseWorker.getAuthorisation(),
                claim,
                CaseEvent.INTENTION_TO_PROCEED_DEADLINE_PASSED
            );
            return null;
        } catch (Exception e) {
            logger.error(String.format("Error whilst staying claim %s", claim.getId()), e);
            return claim;
        }
    }

    private void sendFailedNotification(Collection<Claim> failedClaims) {
        Map<String, Object> input = emailContentProvider.createParameters(failedClaims);
        EmailContent emailContent = emailContentProvider.createContent(input);

        emailService.sendEmail(
            emailProperties.getSender(),
            new EmailData(
                emailProperties.getRecipient(),
                emailContent.getSubject(),
                emailContent.getBody(),
                Collections.emptyList()
            )
        );
    }
}
