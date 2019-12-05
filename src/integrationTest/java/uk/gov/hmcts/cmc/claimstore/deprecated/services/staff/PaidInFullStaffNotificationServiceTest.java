package uk.gov.hmcts.cmc.claimstore.deprecated.services.staff;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.deprecated.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.staff.PaidInFullStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaidInFullStaffNotificationServiceTest extends MockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private PaidInFullStaffNotificationService service;

    @Before
    public void beforeEachTest() {
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test
    public void shouldSendEmailWithExpectedContentPaidInFull() {
        Claim claimWithPaidInFull = SampleClaim.builder()
            .withMoneyReceivedOn(LocalDate.parse("01/12/2025", DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .build();

        service.notifyPaidInFull(claimWithPaidInFull);

        verify(emailService).sendEmail(anyString(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getSubject()).startsWith("Paid in Full");
        assertThat(emailDataArgument.getValue().getMessage()).contains("01/12/2025");
    }
}
