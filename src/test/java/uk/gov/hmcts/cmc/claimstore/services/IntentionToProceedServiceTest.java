package uk.gov.hmcts.cmc.claimstore.services;

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
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

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
    public void scheduleTriggerShouldRunOnWorkday(){
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
        IntentionToProceedService intentionToProceedServiceSpy = Mockito.spy(intentionToProceedService);
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        intentionToProceedServiceSpy.scheduledTrigger();

        verify(intentionToProceedServiceSpy).checkClaimsPastIntentionToProceedDeadline(any(), any());
    }

    @Test
    public void scheduleTriggerShouldNotRunOnWorkday(){
        when(workingDayIndicator.isWorkingDay(any())).thenReturn(false);
        IntentionToProceedService intentionToProceedServiceSpy = Mockito.spy(intentionToProceedService);

        intentionToProceedServiceSpy.scheduledTrigger();

        verify(intentionToProceedServiceSpy, never()).checkClaimsPastIntentionToProceedDeadline(any(), any());
    }

    @Test
    public void saveCaseEventShouldBeTriggeredForFoundCases() {
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        Claim sampleClaim = SampleClaim.builder().build();
        when(caseSearchApi.getClaimsPastIntentionToProceed(any(), any()))
            .thenReturn(ImmutableList.of(sampleClaim, sampleClaim));

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(LocalDateTime.now(), new User(null, null));

        verify(caseRepository, times(2)).saveCaseEvent(any(), any(), eq(CaseEvent.STAY_CLAIM));
    }

    @Test
    public void appInsightsEventShouldBeRaisedForFoundCases() {
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        Claim sampleClaim = SampleClaim.builder().build();
        when(caseSearchApi.getClaimsPastIntentionToProceed(any(), any()))
            .thenReturn(ImmutableList.of(sampleClaim, sampleClaim));

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(LocalDateTime.now(), new User(null, null));

        verify(appInsights, times(2)).trackEvent(eq(AppInsightsEvent.CLAIM_STAYED), eq(REFERENCE_NUMBER), any());
    }

}
