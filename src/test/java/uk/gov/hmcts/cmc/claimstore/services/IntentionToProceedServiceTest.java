package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
        //Tuesday 15th October
        LocalDateTime workdayAfter4pm = LocalDateTime.of(2019, Month.OCTOBER, 15, 16, 00, 00);
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(workdayAfter4pm);

        LocalDate responseDate = workdayAfter4pm.toLocalDate().minusDays(intentionToProceedAdjustment);
        verify(caseSearchApi, once()).getClaimsPastIntentionToProceed(any(), eq(responseDate));

    }

    @Test
    public void checkClaimsPastIntentionToProceedDeadlineOnAWorkdayBefore4pm() {
        //Tuesday 15th October
        LocalDateTime workdayBefore4pm = LocalDateTime.of(2019, Month.OCTOBER, 15, 15, 59, 59);
        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(workdayBefore4pm);

        LocalDate responseDate = workdayBefore4pm.toLocalDate().minusDays(intentionToProceedAdjustment + 1);
        verify(caseSearchApi, once()).getClaimsPastIntentionToProceed(any(), eq(responseDate));

    }

    @Test
    public void checkClaimsPastIntentionToProceedDeadlineONonWorkdayAfter4pm() {
        //Saturday 14th October
        LocalDateTime nonWorkdayAfter4pm = LocalDateTime.of(2019, Month.OCTOBER, 12, 16, 00, 00);

        int workdayAdjustment = 1;
        when(workingDayIndicator.getPreviousWorkingDay(any()))
            .thenReturn(nonWorkdayAfter4pm.minusDays(workdayAdjustment).toLocalDate());

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(nonWorkdayAfter4pm);

        LocalDate responseDate = nonWorkdayAfter4pm.toLocalDate()
            .minusDays(intentionToProceedAdjustment + workdayAdjustment);
        verify(caseSearchApi, once()).getClaimsPastIntentionToProceed(any(), eq(responseDate));

    }

    @Test
    public void checkClaimsPastIntentionToProceedDeadlineOnDayAfterNonWorkdayBefore4pm() {
        //Monday 14th October
        LocalDateTime workdayBefore4pm = LocalDateTime.of(2019, Month.OCTOBER, 14, 15, 59, 59);
        int workdayAdjustment = 2;
        int timeOfDayAdjustment = 1;
        when(workingDayIndicator.getPreviousWorkingDay(any()))
            .thenReturn(workdayBefore4pm.minusDays(workdayAdjustment + timeOfDayAdjustment).toLocalDate());

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(workdayBefore4pm);

        LocalDate responseDate = workdayBefore4pm.toLocalDate()
            .minusDays(intentionToProceedAdjustment + timeOfDayAdjustment + workdayAdjustment);
        verify(caseSearchApi, once()).getClaimsPastIntentionToProceed(any(), eq(responseDate));

    }
    
}
