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
        LocalDateTime now = LocalDateTime.now();
        if (workingDayIndicator.isWorkingDay(now.toLocalDate())) {
            User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();
            checkClaimsPastIntentionToProceedDeadline(now, anonymousCaseWorker);
        }
    }

    public void checkClaimsPastIntentionToProceedDeadline(LocalDateTime runDateTime, User user) {
        LocalDate responseDate = intentionToProceedDeadlineCalculator.calculateResponseDate(runDateTime);
        Collection<Claim> claims = caseSearchApi.getClaimsPastIntentionToProceed(user, responseDate);
        Collection<Claim> failedClaims = claims.stream()
            .map(claim -> updateClaim(user, claim))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (!failedClaims.isEmpty()) {
            sendFailedNotification(failedClaims);
        }
    }

    private Claim updateClaim(User user, Claim claim) {
        try {
            appInsights.trackEvent(AppInsightsEvent.CLAIM_STAYED, REFERENCE_NUMBER, claim.getReferenceNumber());
            caseRepository.saveCaseEvent(
                user.getAuthorisation(),
                claim,
                CaseEvent.STAY_CLAIM
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
