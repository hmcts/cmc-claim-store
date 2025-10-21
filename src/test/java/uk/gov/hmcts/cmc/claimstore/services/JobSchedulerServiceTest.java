package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class JobSchedulerServiceTest {
    private static final int FIRST_REMINDER_DAY = 5;
    private static final int LAST_REMINDER_DAY = 3;
    private static final LocalDate RESPONSE_DEADLINE = LocalDate.now().plusDays(30L);
    private static final ZonedDateTime EXPECTED_FIRST_REMINDER = RESPONSE_DEADLINE.minusDays(FIRST_REMINDER_DAY)
        .atTime(8, 0).atZone(ZoneOffset.UTC);
    private static final ZonedDateTime EXPECTED_LAST_REMINDER = RESPONSE_DEADLINE.minusDays(LAST_REMINDER_DAY)
        .atTime(8, 0).atZone(ZoneOffset.UTC);
    private static final Claim CLAIM = SampleClaim.getWithResponseDeadline(RESPONSE_DEADLINE);

    @Mock
    private JobService jobService;

    @Captor
    private ArgumentCaptor<JobData> jobDataCaptor;

    private JobSchedulerService service;

    @Test
    public void whenEnabledShouldScheduleFirstReminder() {
        service = new JobSchedulerService(jobService, FIRST_REMINDER_DAY, LAST_REMINDER_DAY, true);
        service.scheduleEmailNotificationsForDefendantResponse(CLAIM);
        Mockito.verify(jobService).scheduleJob(jobDataCaptor.capture(), eq(EXPECTED_FIRST_REMINDER));
    }

    @Test
    public void whenEnabledShouldScheduleLastReminder() {
        service = new JobSchedulerService(jobService, FIRST_REMINDER_DAY, LAST_REMINDER_DAY, true);
        service.scheduleEmailNotificationsForDefendantResponse(CLAIM);
        Mockito.verify(jobService).scheduleJob(jobDataCaptor.capture(), eq(EXPECTED_LAST_REMINDER));
    }

    @Test
    public void whenDisabledShouldNotScheduleFirstReminder() {
        service = new JobSchedulerService(jobService, FIRST_REMINDER_DAY, LAST_REMINDER_DAY, false);
        service.scheduleEmailNotificationsForDefendantResponse(CLAIM);
        Mockito.verify(jobService, never()).scheduleJob(jobDataCaptor.capture(), eq(EXPECTED_FIRST_REMINDER));
    }

    @Test
    public void whenDisabledShouldNotScheduleLastReminder() {
        service = new JobSchedulerService(jobService, FIRST_REMINDER_DAY, LAST_REMINDER_DAY, false);
        service.scheduleEmailNotificationsForDefendantResponse(CLAIM);
        Mockito.verify(jobService, never()).scheduleJob(jobDataCaptor.capture(), eq(EXPECTED_LAST_REMINDER));
    }
}
