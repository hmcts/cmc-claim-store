package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.PaidInFullStaffEmailContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PaidInFullStaffNotificationServiceTest extends BaseMockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @MockBean
    protected EmailService emailService;

    @Autowired
    private StaffEmailProperties emailProperties;

    @Autowired
    private PaidInFullStaffEmailContentProvider emailContentProvider;

    private PaidInFullStaffNotificationService service;

    @Before
    public void beforeEachTest() {
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test
    public void shouldSendStaffEmailPaidInFullWhenStaffEmailsEnabled() {

        service = new PaidInFullStaffNotificationService(
            emailService, emailProperties, emailContentProvider, true );

        Claim claimWithPaidInFull = SampleClaim.builder()
            .withMoneyReceivedOn(LocalDate.parse("01/12/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .build();

        service.notifyPaidInFull(claimWithPaidInFull);

        verify(emailService).sendEmail(anyString(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getSubject()).startsWith("Paid in Full");
        assertThat(emailDataArgument.getValue().getMessage()).contains("01/12/2025");
    }

    @Test
    public void shouldNotSendStaffEmailPaidInFullWhenStaffEmailsDisabled() {

        service = new PaidInFullStaffNotificationService(
            emailService, emailProperties, emailContentProvider, false );

        service.notifyPaidInFull(SampleClaim.getDefault());

        verify(emailService, never()).sendEmail(anyString(), any(EmailData.class));
    }
}
