package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.ScheduledStateTransitionContentProvider;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

@Service
public class ScheduledStateTransitionService {
    public static final LocalDateTime DATE_OF_5_POINT_0_RELEASE = LocalDateTime.of(2019, Month.SEPTEMBER, 9, 3, 12, 0);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WorkingDayIndicator workingDayIndicator;

    private final CaseSearchApi caseSearchApi;

    private final UserService userService;

    private final CaseRepository caseRepository;

    private final AppInsights appInsights;

    private final ScheduledStateTransitionContentProvider emailContentProvider;

    private final EmailService emailService;

    private final StaffEmailProperties emailProperties;

    private final ApplicationContext applicationContext;

    public ScheduledStateTransitionService(
        WorkingDayIndicator workingDayIndicator,
        CaseSearchApi caseSearchApi,
        UserService userService,
        AppInsights appInsights,
        CaseRepository caseRepository,
        ScheduledStateTransitionContentProvider emailContentProvider,
        EmailService emailService,
        StaffEmailProperties emailProperties,
        ApplicationContext applicationContext
    ) {
        this.workingDayIndicator = workingDayIndicator;
        this.caseSearchApi = caseSearchApi;
        this.userService = userService;
        this.caseRepository = caseRepository;
        this.appInsights = appInsights;
        this.emailContentProvider = emailContentProvider;
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.applicationContext = applicationContext;
    }

    public void stateChangeTriggered(StateTransition stateTransition) {
        LocalDateTime now = LocalDateTime.now();
        if (workingDayIndicator.isWorkingDay(now.toLocalDate())) {
            User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();
            transitionClaims(now, anonymousCaseWorker, stateTransition);
        }
    }

    public void transitionClaims(LocalDateTime runDateTime, User user, StateTransition stateTransition) {
        StateTransitionCalculator stateTransitionCalculator = applicationContext.getBean(String.format("%sCalculator",
            stateTransition.transitionName()), StateTransitionCalculator.class);
        LocalDate responseDate = stateTransitionCalculator.calculateDateFromDeadline(runDateTime);

        Set<Claim> claims = new HashSet<>(caseSearchApi.getClaims(user,
            stateTransition.getQuery().apply(responseDate)));

        Collection<Claim> failedClaims = claims.stream()
            .map(claim -> updateClaim(user, claim, stateTransition))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        if (!failedClaims.isEmpty()) {
            sendFailedNotification(failedClaims, stateTransition.getCaseEvent());
        }
    }

    private Optional<Claim> updateClaim(User user, Claim claim, StateTransition stateTransition) {
        try {
            appInsights.trackEvent(stateTransition.getAppInsightsEvent(), REFERENCE_NUMBER, claim.getReferenceNumber());
            caseRepository.saveCaseEvent(user.getAuthorisation(), claim, stateTransition.getCaseEvent());

            return Optional.empty();
        } catch (Exception e) {
            logger.error(String.format("Error whilst transitioning claim %s vis caseEvent %s", claim.getId(),
                stateTransition.getCaseEvent().getValue()), e);
            return Optional.of(claim);
        }
    }

    private void sendFailedNotification(Collection<Claim> failedClaims, CaseEvent caseEvent) {
        Map<String, Object> input = emailContentProvider.createParameters(failedClaims, caseEvent);
        EmailContent emailContent = emailContentProvider.createContent(input);

        EmailData emailData = new EmailData(
            emailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            Collections.emptyList()
        );

        emailService.sendEmail(emailProperties.getSender(), emailData);
    }
}
