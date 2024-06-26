package uk.gov.hmcts.cmc.claimstore.services.notifications;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.service.notify.NotificationClientException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_EMAIL;

@ExtendWith(SpringExtension.class)
public class ResponseNeededNotificationServiceTest extends BaseNotificationServiceTest {
    public static final String EMAIL_TEMPLATE_ID = "email_template_id";
    private ResponseNeededNotificationService responseNeededNotificationService;

    @Mock
    private AppInsights appInsights;
    @Mock
    private ClaimService claimService;

    @BeforeEach
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
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(PaymentOption.IMMEDIATELY)
                    .build()
            ).build();

        ImmutableMap<String, Object> data = ImmutableMap.of("caseReference", claim.getReferenceNumber());

        JobDataMap jobData = new JobDataMap(data);
        when(jobDetail.getJobDataMap()).thenReturn(jobData);
        when(claimService.getClaimByReferenceAnonymous(claim.getReferenceNumber())).thenReturn(Optional.of(claim));
        //when
        responseNeededNotificationService.sendMail(jobDetail);
        //then

        verify(notificationClient).sendEmail(eq(EMAIL_TEMPLATE_ID),
            eq(claim.getDefendantEmail()),
            anyMap(),
            eq(claim.getReferenceNumber()));
    }

    @Test
    public void recoveryShouldNotLogPII() {
        JobDetail mockJobDetail = mock(JobDetail.class);
        when(mockJobDetail.getJobDataMap()).thenReturn(new JobDataMap(ImmutableMap.of("caseReference", "reference")));
        responseNeededNotificationService.logNotificationFailure(
            new NotificationException("expected exception"),
            mockJobDetail
        );

        assertWasLogged("Response needed notification cannot be sent (reference) due to expected exception");
    }
}
