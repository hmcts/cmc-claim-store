package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import groovy.lang.IntRange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.content.IntentionToProceedContentProvider;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
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

    @Test
    public void claimStayingSuccessShouldNotSendNotificationEmail() {
        List<Claim> claims =
            new IntRange(1, 3).stream()
                .map(id -> Claim.builder().id(id.longValue()).build())
                .collect(Collectors.toList());

        when(workingDayIndicator.getPreviousWorkingDay(any())).then(returnsFirstArg());
        when(caseSearchApi.getClaimsPastIntentionToProceed(any(), any())).thenReturn(claims);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(new User("", null));

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(LocalDateTime.now());

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
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(new User("", null));
        when(emailContentProvider.createContent(any())).thenReturn(new EmailContent("", ""));

        final ImmutableMap<String, Object> input = ImmutableMap.of(
            "noOfClaims", claims.size(),
            "claimIds", claims.stream().map(c -> c.getId().toString())
                .collect(Collectors.joining("\n"))
        );

        when(emailContentProvider.createParameters(claims)).thenReturn(input);

        intentionToProceedService.checkClaimsPastIntentionToProceedDeadline(LocalDateTime.now());

        verify(emailService, once()).sendEmail(any(), any());

        verify(emailContentProvider, once()).createContent(input);
    }

}
