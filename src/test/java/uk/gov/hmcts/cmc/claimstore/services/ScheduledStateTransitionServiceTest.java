package uk.gov.hmcts.cmc.claimstore.services;

import groovy.lang.IntRange;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.ScheduledStateTransitionContentProvider;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransition;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private CaseEventsApi caseEventsApi;

    @Mock
    private Environment environment;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private StateTransition stateTransition;

    private final User user = new User("", new UserDetails(null, null, null, null, null));

    private final CaseEvent caseEvent = CaseEvent.values()[0];

    private final AppInsightsEvent appInsightsEvent = AppInsightsEvent.values()[0];

    private final LocalDateTime dateTime = LocalDateTime.now();

    private final Integer deadline = 10;

    @Before
    public void setUp() {
        when(stateTransition.name()).thenReturn("STATE_TRANSITION");
        when(stateTransition.getQuery()).thenReturn(localDate -> QueryBuilders.termQuery("date", localDate));
        when(stateTransition.getCaseEvent()).thenReturn(caseEvent);
        when(stateTransition.getAppInsightsEvent()).thenReturn(appInsightsEvent);

        when(environment.getProperty(anyString())).thenReturn(deadline.toString());

        scheduledStateTransitionService = new ScheduledStateTransitionService(
            workingDayIndicator,
            caseSearchApi,
            userService,
            appInsights,
            caseRepository,
            emailContentProvider,
            emailService,
            emailProperties,
            environment,
            caseEventsApi,
            authTokenGenerator
        );
    }

    @Test
    public void shouldCreateQueryUsingDateFromIntentionToProceedDeadlineCalculator() {
        LocalDateTime tuesdayAfter4pm = LocalDateTime.of(2019, Month.OCTOBER, 15, 16, 0, 0);
        when(workingDayIndicator.getPreviousWorkingDay(any())).thenReturn(tuesdayAfter4pm.toLocalDate());

        scheduledStateTransitionService.transitionClaims(tuesdayAfter4pm, user, stateTransition);

        LocalDate returnDate = tuesdayAfter4pm.toLocalDate().minusDays(deadline);
        QueryBuilder expected = stateTransition.getQuery().apply(returnDate);
        verify(caseSearchApi, once()).getClaims(eq(user), eq(expected));
    }

    @Test
    public void claimStayingSuccessShouldNotSendNotificationEmail() {
        List<Claim> claims =
            new IntRange(1, 3).stream()
                .map(id -> Claim.builder().id(id.longValue()).ccdCaseId(id.longValue()).build())
                .collect(Collectors.toList());
        when(caseSearchApi.getClaims(any(), any())).thenReturn(claims);
        when(workingDayIndicator.getPreviousWorkingDay(any())).thenReturn(LocalDate.now());

        scheduledStateTransitionService.transitionClaims(LocalDateTime.now(), user, stateTransition);

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void claimStayingFailuresShouldSendNotificationEmail() {
        List<Claim> claims =
            new IntRange(1, 3).stream()
                .map(id -> Claim.builder().id(id.longValue()).ccdCaseId(id.longValue()).build())
                .collect(Collectors.toList());
        when(caseRepository.saveCaseEvent(any(), any(), any())).thenThrow(RuntimeException.class);
        when(caseSearchApi.getClaims(any(), any())).thenReturn(claims);
        when(emailContentProvider.createContent(any(), eq(caseEvent))).thenReturn(new EmailContent("", ""));
        when(workingDayIndicator.getPreviousWorkingDay(any())).thenReturn(LocalDate.now());

        scheduledStateTransitionService.transitionClaims(LocalDateTime.now(), user, stateTransition);

        verify(emailService, once()).sendEmail(any(), any());
        verify(emailContentProvider, once()).createContent(any(), eq(caseEvent));
    }

    @Test
    public void stateTransitionScheduleTriggerShouldRunOnWorkday() {
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
        when(workingDayIndicator.getPreviousWorkingDay(any())).thenReturn(LocalDate.now());
        ScheduledStateTransitionService scheduledStateTransitionServiceSpy =
            Mockito.spy(scheduledStateTransitionService);

        scheduledStateTransitionServiceSpy.stateChangeTriggered(stateTransition);

        verify(scheduledStateTransitionServiceSpy).transitionClaims(any(), any(), any());
    }

    @Test
    public void stateTransitionScheduleTriggerShouldNotRunOnWorkday() {
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(false);
        ScheduledStateTransitionService scheduledStateTransitionServiceSpy =
            Mockito.spy(scheduledStateTransitionService);

        scheduledStateTransitionServiceSpy.stateChangeTriggered(stateTransition);

        verify(scheduledStateTransitionServiceSpy, never()).transitionClaims(any(), any(), any());
    }

    @Test
    public void saveCaseEventShouldBeTriggeredForFoundCases() {
        Claim sampleClaim1 = SampleClaim.builder().withClaimId(1L).withCcdCaseId(1L).build();
        Claim sampleClaim2 = SampleClaim.builder().withClaimId(2L).withCcdCaseId(2L).build();
        when(caseSearchApi.getClaims(any(), any())).thenReturn(ImmutableList.of(sampleClaim1, sampleClaim2));
        when(workingDayIndicator.getPreviousWorkingDay(any())).thenReturn(dateTime.toLocalDate());

        scheduledStateTransitionService.transitionClaims(dateTime, user, stateTransition);

        verify(caseRepository, times(2)).saveCaseEvent(any(), any(), eq(caseEvent));
    }

    @Test
    public void appInsightsEventShouldBeRaisedForFoundCases() {
        Claim sampleClaim1 = SampleClaim.builder().withClaimId(1L).withCcdCaseId(1L).build();
        Claim sampleClaim2 = SampleClaim.builder().withClaimId(2L).withCcdCaseId(2L).build();
        when(caseSearchApi.getClaims(any(), any())).thenReturn(ImmutableList.of(sampleClaim1, sampleClaim2));
        when(workingDayIndicator.getPreviousWorkingDay(any())).thenReturn(dateTime.toLocalDate());

        scheduledStateTransitionService.transitionClaims(dateTime, user, stateTransition);

        verify(appInsights, times(2)).trackEvent(eq(appInsightsEvent), eq(REFERENCE_NUMBER), any());
    }

    @Test
    public void triggerEventsShouldTransitionEventIfLastEvent() {
        Claim sampleClaim1 = SampleClaim.builder().withClaimId(1L).withCcdCaseId(1L).build();
        when(caseSearchApi.getClaims(any(), any())).thenReturn(ImmutableList.of(sampleClaim1));
        when(stateTransition.getTriggerEvents()).thenReturn(ImmutableSet.of(caseEvent));
        when(workingDayIndicator.getPreviousWorkingDay(any())).thenReturn(dateTime.toLocalDate());

        List<CaseEventDetail> caseEventDetails = new ArrayList(ImmutableList.of(
            buildCaseEventDetail(caseEvent, LocalDateTime.now())
        ));
        when(caseEventsApi.findEventDetailsForCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(caseEventDetails);

        scheduledStateTransitionService.transitionClaims(dateTime, user, stateTransition);

        verify(caseRepository, once()).saveCaseEvent(any(), any(), eq(caseEvent));

    }

    @Test
    public void triggerEventsShouldNotTransitionEventIfNotLastEvent() {

        Claim sampleClaim1 = SampleClaim.builder().withClaimId(1L).withCcdCaseId(1L).build();
        when(caseSearchApi.getClaims(any(), any())).thenReturn(ImmutableList.of(sampleClaim1));
        when(stateTransition.getTriggerEvents()).thenReturn(ImmutableSet.of(caseEvent));
        when(workingDayIndicator.getPreviousWorkingDay(any())).thenReturn(dateTime.toLocalDate());

        List<CaseEventDetail> caseEventDetails = new ArrayList(ImmutableList.of(
            buildCaseEventDetail(CaseEvent.values()[1], LocalDateTime.now())
        ));
        when(caseEventsApi.findEventDetailsForCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(caseEventDetails);

        scheduledStateTransitionService.transitionClaims(dateTime, user, stateTransition);

        verify(caseRepository, never()).saveCaseEvent(any(), any(), eq(caseEvent));

    }

    @Test
    public void triggerEventsShouldTransitionEventIfIgnoredEventLastEvent() {
        CaseEvent ignoreEvent = CaseEvent.values()[1];

        Claim sampleClaim1 = SampleClaim.builder().withClaimId(1L).withCcdCaseId(1L).build();
        when(caseSearchApi.getClaims(any(), any())).thenReturn(ImmutableList.of(sampleClaim1));
        when(stateTransition.getTriggerEvents()).thenReturn(ImmutableSet.of(caseEvent));
        when(stateTransition.getIgnoredEvents()).thenReturn(ImmutableSet.of(ignoreEvent));
        when(workingDayIndicator.getPreviousWorkingDay(any())).thenReturn(dateTime.toLocalDate());

        LocalDateTime now = LocalDateTime.now();
        List<CaseEventDetail> caseEventDetails =
            new ArrayList(ImmutableList.of(
                buildCaseEventDetail(ignoreEvent, now.plusDays(1)),
                buildCaseEventDetail(caseEvent, now)
            ));
        when(caseEventsApi.findEventDetailsForCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(caseEventDetails);

        scheduledStateTransitionService.transitionClaims(dateTime, user, stateTransition);

        verify(caseRepository, once()).saveCaseEvent(any(), any(), eq(caseEvent));

    }

    private CaseEventDetail buildCaseEventDetail(CaseEvent value, LocalDateTime createdDate) {
        return CaseEventDetail.builder()
            .eventName(value.getValue())
            .createdDate(createdDate)
            .build();
    }
}
