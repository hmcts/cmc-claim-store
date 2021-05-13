package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.launchdarkly.sdk.LDUser;
import com.sendgrid.helpers.mail.objects.Email;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.cmc.email.sendgrid.SendGridClient;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.email.EmailService.EMAIL_SUBJECT;

public class PaidInFullStaffNotificationServiceWithEmailServiceRetryTest extends BaseMockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Mock
    private JavaMailSenderImpl javaMailSender;

    @Mock
    private EmailService emailService;

    @MockBean
    protected PostClaimOrchestrationHandler postClaimOrchestrationHandler;

    @Autowired
    private SendGridClient sendGridClient;

    @Autowired
    private PaidInFullStaffNotificationService service;

    @Before
    public void beforeEachTest() {
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).thenReturn(PDF_CONTENT);
    }

/*    @Test
    public void shouldSendEmailWithExpectedContentPaidInFull() throws IOException {
        Claim claimWithPaidInFull = SampleClaim.builder()
            .withMoneyReceivedOn(LocalDate.parse("01/12/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .build();

        service.notifyPaidInFull(claimWithPaidInFull);

        verify(emailService).sendEmail("no-reply@example.com", new EmailData(
            "user@example.com", "EmailSubject", "My Test Message", Collections.emptyList()
        ));
    }*/

    /*@Test
    public void shouldSendEmailWithExpectedContentPaidInFull() throws IOException {
        Claim claimWithPaidInFull = SampleClaim.builder()
            .withMoneyReceivedOn(LocalDate.parse("01/12/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .build();

        service.notifyPaidInFull(claimWithPaidInFull);

        verify(emailService).sendEmail("no-reply@example.com", new EmailData(
            "user@example.com", "EmailSubject", "My Test Message", Collections.emptyList()
        ));
    }*/

    @Test
    public void shouldRetryOnFailure() {
        given(javaMailSender.createMimeMessage())
            .willThrow(new MailSendException("first time"))
            .willThrow(new MailSendException("second time"))
            .willThrow(new MailSendException("third time"))
        ;

        Claim claimWithPaidInFull = SampleClaim.builder()
            .withMoneyReceivedOn(LocalDate.parse("01/12/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .build();

        service.notifyPaidInFull(claimWithPaidInFull);

        verify(javaMailSender, atLeast(3)).createMimeMessage();
        verify(telemetry).trackEvent(
            eq("Notification - failure"),
            eq(singletonMap("EmailSubject", "Paid in Full 000MC001: John Rambo v Dr. John Smith")),
            eq(null)
        );
    }
}
