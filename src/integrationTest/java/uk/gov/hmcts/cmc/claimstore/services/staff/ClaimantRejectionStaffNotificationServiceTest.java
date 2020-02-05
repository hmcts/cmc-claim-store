package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClaimantRejectionStaffNotificationServiceTest extends BaseMockSpringTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};
    private Claim claimWithPartAdmission;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    @Autowired
    private ClaimantRejectionStaffNotificationService service;

    @MockBean
    protected EmailService emailService;

    @Before
    public void beforeEachTest() {
        claimWithPartAdmission = SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();

        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {

        service.notifyStaffClaimantRejectPartAdmission(claimWithPartAdmission);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContentClaimantRejectionWithPartAdmission() {

        service.notifyStaffClaimantRejectPartAdmission(claimWithPartAdmission);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        String subject = String.format("Partial admission rejected %s %s v %s",
            claimWithPartAdmission.getReferenceNumber(),
            claimWithPartAdmission.getClaimData().getClaimant().getName(),
            claimWithPartAdmission.getClaimData().getDefendant().getName()
        );

        String body = String.format("%s has rejected a partial admission",
            claimWithPartAdmission.getClaimData().getClaimant().getName());

        assertThat(emailDataArgument.getValue()
            .getSubject()).isEqualTo(subject);
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(body);
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachments() throws IOException {

        service.notifyStaffClaimantRejectPartAdmission(claimWithPartAdmission);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment emailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        assertThat(emailAttachment.getContentType()).isEqualTo("application/pdf");

        byte[] pdfContent = IOUtils.toByteArray(emailAttachment.getData()
            .getInputStream());
        assertThat(pdfContent).isEqualTo(PDF_CONTENT);
    }
}
