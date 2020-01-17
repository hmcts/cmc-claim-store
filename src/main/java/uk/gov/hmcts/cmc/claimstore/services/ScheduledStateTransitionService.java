package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

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

    private final Environment environment;

    public ScheduledStateTransitionService(
        WorkingDayIndicator workingDayIndicator,
        CaseSearchApi caseSearchApi,
        UserService userService,
        AppInsights appInsights,
        CaseRepository caseRepository,
        ScheduledStateTransitionContentProvider emailContentProvider,
        EmailService emailService,
        StaffEmailProperties emailProperties,
        Environment environment
    ) {
        this.workingDayIndicator = workingDayIndicator;
        this.caseSearchApi = caseSearchApi;
        this.userService = userService;
        this.caseRepository = caseRepository;
        this.appInsights = appInsights;
        this.emailContentProvider = emailContentProvider;
        this.emailService = emailService;
        this.emailProperties = emailProperties;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        for (StateTransition stateTransition : StateTransition.values()) {
            getStateTransitionDaysProperty(stateTransition);
        }
    }

    public void stateChangeTriggered(StateTransition stateTransition) {
        LocalDateTime now = LocalDateTime.now();
        if (workingDayIndicator.isWorkingDay(now.toLocalDate())) {
            User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();
            transitionClaims(now, anonymousCaseWorker, stateTransition);
        }
    }

    public void transitionClaims(LocalDateTime runDateTime, User user, StateTransition stateTransition) {
        StateTransitionCalculator stateTransitionCalculator = getStateTransitionCalculator(stateTransition);
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
            logger.error(String.format("Error whilst transitioning claim %s vis caseEvent %s",
                claim.getReferenceNumber(), stateTransition.getCaseEvent().getValue()), e);
            return Optional.of(claim);
        }
    }

    private void sendFailedNotification(Collection<Claim> failedClaims, CaseEvent caseEvent) {
        EmailContent emailContent = emailContentProvider.createContent(failedClaims, caseEvent);

        EmailData emailData = new EmailData(
            emailProperties.getRecipient(),
            emailContent.getSubject(),
            emailContent.getBody(),
            Collections.emptyList()
        );

        emailService.sendEmail(emailProperties.getSender(), emailData);
    }

    private StateTransitionCalculator getStateTransitionCalculator(StateTransition stateTransition) {
        String numberOfDaysProperty = getStateTransitionDaysProperty(stateTransition);
        return new StateTransitionCalculator(workingDayIndicator, Integer.parseInt(numberOfDaysProperty));
    }

    private String getStateTransitionDaysProperty(StateTransition stateTransition) {
        String numberOfDaysPropertyKey = String.format("dateCalculations.%sDeadlineInDays",
            CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, stateTransition.name()));

        String numberOfDaysProperty = environment.getProperty(numberOfDaysPropertyKey);

        if (StringUtils.isBlank(numberOfDaysProperty)) {
            throw new IllegalArgumentException(String.format("Could not resolve placeholder %s",
                numberOfDaysPropertyKey));
        }
        return numberOfDaysProperty;
    }
}
