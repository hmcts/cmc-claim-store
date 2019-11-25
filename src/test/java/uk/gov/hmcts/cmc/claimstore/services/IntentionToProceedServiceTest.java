package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import groovy.lang.IntRange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class IntentionToProceedServiceTest {

    private IntentionToProceedService intentionToProceedService;

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
    private IntentionToProceedContentProvider emailContentProvider;

    @Mock
    private EmailService emailService;

    @Mock
    private StaffEmailProperties emailProperties;

    private final int intentionToProceedAdjustment = 33;

    private IntentionToProceedDeadlineCalculator intentionToProceedDeadlineCalculator;

    @Before
    public void setUp() {
        intentionToProceedDeadlineCalculator = new IntentionToProceedDeadlineCalculator(
            workingDayIndicator,
            intentionToProceedAdjustment
        );

        intentionToProceedService = new IntentionToProceedService(
            workingDayIndicator,
            caseSearchApi,
            userService,
            appInsights,
            caseRepository,
            emailContentProvider,
            emailService,
            emailProperties,
            intentionToProceedDeadlineCalculator
        );
    }

    @Test
    public void checkClaimsPastIntentionToProceedDeadlineOnAWorkdayAfter4pm() {
        LocalDateTime tuesdayAfter4pm = LocalDateTime.of(2019, Month.OCTOBER, 15, 16, 00, 00);
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(tuesdayAfter4pm, new User(null, null));

        LocalDate responseDate = tuesdayAfter4pm.toLocalDate().minusDays(intentionToProceedAdjustment);
        verify(caseSearchApi, once()).getClaimsPastIntentionToProceed(any(), eq(responseDate));
    }

    @Test
    public void checkClaimsPastIntentionToProceedDeadlineOnAWorkdayBefore4pm() {
        LocalDateTime tuesdayBefore4pm = LocalDateTime.of(2019, Month.OCTOBER, 15, 15, 59, 59);
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(tuesdayBefore4pm, new User(null, null));

        LocalDate responseDate = tuesdayBefore4pm.toLocalDate().minusDays(intentionToProceedAdjustment + 1);
        verify(caseSearchApi, once()).getClaimsPastIntentionToProceed(any(), eq(responseDate));
    }

    @Test
    public void checkClaimsPastIntentionToProceedDeadlineONonWorkdayAfter4pm() {
        LocalDateTime saturday = LocalDateTime.of(2019, Month.OCTOBER, 12, 16, 00, 00);

        int workdayAdjustment = 1;
        when(workingDayIndicator.getPreviousWorkingDay(any()))
            .thenReturn(saturday.minusDays(workdayAdjustment).toLocalDate());

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(saturday, new User(null, null));

        LocalDate responseDate = saturday.toLocalDate()
            .minusDays(intentionToProceedAdjustment + workdayAdjustment);
        verify(caseSearchApi, once()).getClaimsPastIntentionToProceed(any(), eq(responseDate));
    }

    @Test
    public void checkClaimsPastIntentionToProceedDeadlineOnDayAfterNonWorkdayBefore4pm() {
        LocalDateTime mondayBefore4pm = LocalDateTime.of(2019, Month.OCTOBER, 14, 15, 59, 59);
        int workdayAdjustment = 2;
        int timeOfDayAdjustment = 1;
        when(workingDayIndicator.getPreviousWorkingDay(any()))
            .thenReturn(mondayBefore4pm.minusDays(workdayAdjustment + timeOfDayAdjustment).toLocalDate());

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(mondayBefore4pm, new User(null, null));

        LocalDate responseDate = mondayBefore4pm.toLocalDate()
            .minusDays(intentionToProceedAdjustment + timeOfDayAdjustment + workdayAdjustment);
        verify(caseSearchApi, once()).getClaimsPastIntentionToProceed(any(), eq(responseDate));
    }

    @Test
    public void claimStayingSuccessShouldNotSendNotificationEmail() {
        List<Claim> claims =
            new IntRange(1, 3).stream()
                .map(id -> Claim.builder().id(id.longValue()).build())
                .collect(Collectors.toList());

        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());
        when(caseSearchApi.getClaimsPastIntentionToProceed(any(), any())).thenReturn(claims);

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(LocalDateTime.now(), new User("", null));

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void claimStayingFailuresShouldSendNotificationEmail() {
        List<Claim> claims =
            new IntRange(1, 3).stream()
                .map(id -> Claim.builder().id(id.longValue()).build())
                .collect(Collectors.toList());

        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());
        when(caseRepository.saveCaseEvent(any(), any(), any())).thenThrow(RuntimeException.class);
        when(caseSearchApi.getClaimsPastIntentionToProceed(any(), any())).thenReturn(claims);
        when(emailContentProvider.createContent(any())).thenReturn(new EmailContent("", ""));

        final ImmutableMap<String, Object> input = ImmutableMap.of(
            "noOfClaims", claims.size(),
            "claimIds", claims.stream().map(c -> c.getId().toString())
                .collect(Collectors.joining("\n"))
        );

        when(emailContentProvider.createParameters(any())).thenReturn(input);

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(LocalDateTime.now(), new User("", null));

        verify(emailService, once()).sendEmail(any(), any());

        verify(emailContentProvider, once()).createContent(input);
    }

    @Test
    public void scheduleTriggerShouldRunOnWorkday() {
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
        IntentionToProceedService intentionToProceedServiceSpy = Mockito.spy(intentionToProceedService);
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        intentionToProceedServiceSpy.scheduledTrigger();

        verify(intentionToProceedServiceSpy).checkClaimsPastIntentionToProceedDeadline(any(), any());
    }

    @Test
    public void scheduleTriggerShouldNotRunOnWorkday() {
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(false);
        IntentionToProceedService intentionToProceedServiceSpy = Mockito.spy(intentionToProceedService);

        intentionToProceedServiceSpy.scheduledTrigger();

        verify(intentionToProceedServiceSpy, never()).checkClaimsPastIntentionToProceedDeadline(any(), any());
    }

    @Test
    public void saveCaseEventShouldBeTriggeredForFoundCases() {
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        Claim sampleClaim1 = SampleClaim.builder().withClaimId(1L).build();
        Claim sampleClaim2 = SampleClaim.builder().withClaimId(12L).build();
        when(caseSearchApi.getClaimsPastIntentionToProceed(any(), any()))
            .thenReturn(ImmutableList.of(sampleClaim1, sampleClaim2));

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(LocalDateTime.now(), new User(null, null));

        verify(caseRepository, times(2)).saveCaseEvent(any(), any(), eq(CaseEvent.STAY_CLAIM));
    }

    @Test
    public void appInsightsEventShouldBeRaisedForFoundCases() {
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        Claim sampleClaim1 = SampleClaim.builder().withClaimId(1L).build();
        Claim sampleClaim2 = SampleClaim.builder().withClaimId(12L).build();
        when(caseSearchApi.getClaimsPastIntentionToProceed(any(), any()))
            .thenReturn(ImmutableList.of(sampleClaim1, sampleClaim2));

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(LocalDateTime.now(), new User(null, null));

        verify(appInsights, times(2)).trackEvent(eq(AppInsightsEvent.CLAIM_STAYED), eq(REFERENCE_NUMBER), any());
    }

}
