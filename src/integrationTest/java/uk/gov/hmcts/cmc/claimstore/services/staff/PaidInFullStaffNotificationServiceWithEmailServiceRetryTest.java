package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.microsoft.applicationinsights.TelemetryClient;
import org.apache.http.HttpException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailSendFailedException;
import uk.gov.hmcts.cmc.email.sendgrid.SendGridClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
public class PaidInFullStaffNotificationServiceWithEmailServiceRetryTest extends BaseMockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @MockBean
    private SendGridClient mailClient;
    @MockBean
    protected TelemetryClient telemetry;
    @MockBean
    protected PostClaimOrchestrationHandler postClaimOrchestrationHandler;

    @Autowired
    private PaidInFullStaffNotificationService service;

    @Before
    public void beforeEachTest() {
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn(PDF_CONTENT);
    }

    @Test
    public void shouldSendEmailWithExpectedContentPaidInFull() throws IOException {

        Claim claimWithPaidInFull = SampleClaim.builder()
            .withMoneyReceivedOn(LocalDate.parse("01/12/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .build();

        service.notifyPaidInFull(claimWithPaidInFull);

        verify(mailClient).sendEmail(anyString(), any(EmailData.class));
    }

    @Test
    public void shouldRetryOnFailure() throws IOException {
        willThrow(
            new EmailSendFailedException("first time", new HttpException()),
            new EmailSendFailedException("second time", new HttpException()),
            new EmailSendFailedException("third time", new HttpException())
        )
            .given(mailClient).sendEmail(anyString(), any(EmailData.class));

        Claim claimWithPaidInFull = SampleClaim.builder()
            .withMoneyReceivedOn(LocalDate.parse("01/12/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .build();

        service.notifyPaidInFull(claimWithPaidInFull);

        verify(mailClient, atLeast(3)).sendEmail(anyString(), any(EmailData.class));
        verify(telemetry).trackEvent(
            eq("Notification - failure"),
            eq(singletonMap("EmailSubject", "Paid in Full 000MC001: John Rambo v Dr. John Smith")),
            eq(null)
        );
    }
}
