package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import groovy.lang.IntRange;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.ScheduledStateTransitionContentProvider;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledStateTransitionServiceTest {

    private ScheduledStateTransitionService scheduledStateTransitionService;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private CaseSearchApi caseSearchApi;

    @Mock
    private UserService userService;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private AppInsights appInsights;

    @Mock
    private ScheduledStateTransitionContentProvider emailContentProvider;

    @Mock
    private EmailService emailService;

    @Mock
    private StaffEmailProperties emailProperties;

    @Mock
    private StateTransitionCalculator stateTransitionCalculator;

    @Mock
    private ApplicationContext applicationContext;

    @Before
    public void setUp() {
        when(applicationContext.getBean(any(String.class), any(Class.class))).thenReturn(stateTransitionCalculator);
        scheduledStateTransitionService = new ScheduledStateTransitionService(
            workingDayIndicator,
            caseSearchApi,
            userService,
            appInsights,
            caseRepository,
            emailContentProvider,
            emailService,
            emailProperties,
            applicationContext
        );
    }

    @Test
    public void shouldCreateQueryUsingDateFromIntentionToProceedDeadlineCalculator() {
        LocalDateTime tuesdayAfter4pm = LocalDateTime.of(2019, Month.OCTOBER, 15, 16, 0, 0);
        Integer deadline = 10;
        LocalDate returnDate = tuesdayAfter4pm.toLocalDate().minusDays(deadline);

        when(stateTransitionCalculator.calculateDateFromDeadline(any())).thenReturn(returnDate);
        User user = new User("", null);

        scheduledStateTransitionService.transitionClaims(tuesdayAfter4pm, user, StateTransition.values()[0]);

        QueryBuilder expected = StateTransition.values()[0].getQuery().apply(returnDate);
        verify(caseSearchApi, once()).getClaims(eq(user), eq(expected));
    }

    @Test
    public void claimStayingSuccessShouldNotSendNotificationEmail() {
        List<Claim> claims =
            new IntRange(1, 3).stream()
                .map(id -> Claim.builder().id(id.longValue()).build())
                .collect(Collectors.toList());

        when(caseSearchApi.getClaims(any(), any())).thenReturn(claims);

        scheduledStateTransitionService.transitionClaims(LocalDateTime.now(),
            new User(null, null), StateTransition.values()[0]);

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void claimStayingFailuresShouldSendNotificationEmail() {
        List<Claim> claims =
            new IntRange(1, 3).stream()
                .map(id -> Claim.builder().id(id.longValue()).build())
                .collect(Collectors.toList());

        when(caseRepository.saveCaseEvent(any(), any(), any())).thenThrow(RuntimeException.class);
        when(caseSearchApi.getClaims(any(), any())).thenReturn(claims);
        when(emailContentProvider.createContent(any())).thenReturn(new EmailContent("", ""));

        final ImmutableMap<String, Object> input = ImmutableMap.of(
            "noOfClaims", claims.size(),
            "caseEvent", CaseEvent.STAY_CLAIM,
            "claimIds", claims.stream().map(c -> c.getId().toString())
                .collect(Collectors.joining("\n"))
        );

        when(emailContentProvider.createParameters(any(), eq(CaseEvent.STAY_CLAIM))).thenReturn(input);

        scheduledStateTransitionService.transitionClaims(LocalDateTime.now(), new User(null, null),
            StateTransition.values()[0]);

        verify(emailService, once()).sendEmail(any(), any());

        verify(emailContentProvider, once()).createContent(input);
    }

    @Test
    public void stateTransitionScheduleTriggerShouldRunOnWorkday() {
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
        ScheduledStateTransitionService scheduledStateTransitionServiceSpy =
            Mockito.spy(scheduledStateTransitionService);

        when(stateTransitionCalculator.calculateDateFromDeadline(any())).thenReturn(LocalDate.now());

        scheduledStateTransitionServiceSpy.stateChangeTriggered(StateTransition.values()[0]);

        verify(scheduledStateTransitionServiceSpy).transitionClaims(any(), any(), any());
    }

    @Test
    public void stateTransitionScheduleTriggerShouldNotRunOnWorkday() {
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(false);
        ScheduledStateTransitionService scheduledStateTransitionServiceSpy =
            Mockito.spy(scheduledStateTransitionService);

        scheduledStateTransitionServiceSpy.stateChangeTriggered(StateTransition.values()[0]);

        verify(scheduledStateTransitionServiceSpy, never()).transitionClaims(any(), any(), any());
    }

    @Test
    public void saveCaseEventShouldBeTriggeredForFoundCases() {
        LocalDateTime dateTime = LocalDateTime.now();
        when(stateTransitionCalculator.calculateDateFromDeadline(any())).thenReturn(dateTime.toLocalDate());

        Claim sampleClaim1 = SampleClaim.builder().withClaimId(1L).build();
        Claim sampleClaim2 = SampleClaim.builder().withClaimId(2L).build();
        when(caseSearchApi.getClaims(any(), any()))
            .thenReturn(ImmutableList.of(sampleClaim1, sampleClaim2));

        scheduledStateTransitionService.transitionClaims(dateTime, new User(null, null),
            StateTransition.values()[0]);

        verify(caseRepository, times(2)).saveCaseEvent(any(), any(), eq(CaseEvent.STAY_CLAIM));
    }

    @Test
    public void appInsightsEventShouldBeRaisedForFoundCases() {
        LocalDateTime dateTime = LocalDateTime.now();
        when(stateTransitionCalculator.calculateDateFromDeadline(any())).thenReturn(dateTime.toLocalDate());

        Claim sampleClaim1 = SampleClaim.builder().withClaimId(1L).build();
        Claim sampleClaim2 = SampleClaim.builder().withClaimId(2L).build();
        when(caseSearchApi.getClaims(any(), any())).thenReturn(ImmutableList.of(sampleClaim1, sampleClaim2));

        scheduledStateTransitionService.transitionClaims(dateTime, new User(null, null), StateTransition.values()[0]);

        verify(appInsights, times(2)).trackEvent(eq(AppInsightsEvent.CLAIM_STAYED), eq(REFERENCE_NUMBER), any());
    }

}
