package uk.gov.hmcts.cmc.claimstore.services.notifications;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.service.notify.NotificationClientException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseNeededNotificationServiceTest extends BaseNotificationServiceTest {
    public static final String EMAIL_TEMPLATE_ID = "email_template_id";
    private ResponseNeededNotificationService responseNeededNotificationService;

    @Mock
    private AppInsights appInsights;
    @Mock
    private ClaimService claimService;


    @Before
    public void before() {
        responseNeededNotificationService = new ResponseNeededNotificationService(notificationClient,
            properties,
            appInsights,
            claimService);

        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        when(properties.getRespondToClaimUrl()).thenReturn(RESPOND_TO_CLAIM_URL);
        when(properties.getTemplates()).thenReturn(templates);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getDefendantResponseNeeded()).thenReturn(EMAIL_TEMPLATE_ID);
    }

    @Test
    public void sendShouldMailToDefendant() throws NotificationClientException {
        //given
        JobDetail jobDetail = mock(JobDetail.class);
        String caseReference = "000MC004";
        String defendantEmailAddress = "defendant@mail.com";

        ImmutableMap<String, ? extends Serializable> data = ImmutableMap.of("caseReference", caseReference,
            "defendantEmail", defendantEmailAddress,
            "claimantName", "Mr claimant",
            "responseDeadline", LocalDate.now(),
            "defendantName", "Mr defendant");

        JobDataMap jobData = new JobDataMap(data);
        when(jobDetail.getJobDataMap()).thenReturn(jobData);
        when(claimService.getClaimByReferenceAnonymous(caseReference))
            .thenReturn(Optional.of(SampleClaim.getDefault()));
        //when
        responseNeededNotificationService.sendMail(jobDetail);
        //then

        verify(notificationClient).sendEmail(eq(EMAIL_TEMPLATE_ID),
            eq(defendantEmailAddress),
            anyMap(),
            eq(caseReference));
    }
}
