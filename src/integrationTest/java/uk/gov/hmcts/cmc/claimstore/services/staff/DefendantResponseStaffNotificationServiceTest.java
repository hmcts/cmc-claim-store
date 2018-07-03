package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;

public class DefendantResponseStaffNotificationServiceTest extends MockSpringTest {

    private static final String DEFENDANT_EMAIL = "defendant@mail.com";

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    @Autowired
    private DefendantResponseStaffNotificationService service;

    @Before
    public void beforeEachTest() {
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        Claim claimWithFullDefenceResponse = SampleClaim.builder()
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(null)
                    .build()
            )
            .withRespondedAt(LocalDateTime.now())
            .build();

        service.notifyStaffDefenceSubmittedFor(claimWithFullDefenceResponse, DEFENDANT_EMAIL);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContentFullDefence() {
        Claim claimWithFullDefenceResponse = SampleClaim.builder()
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(null)
                    .build()
            )
            .withRespondedAt(LocalDateTime.now())
            .build();

        service.notifyStaffDefenceSubmittedFor(claimWithFullDefenceResponse, DEFENDANT_EMAIL);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Civil Money Claim defence submitted");
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The defendant has submitted an already paid defence which is attached as a PDF."
        );
    }

    @Test
    public void shouldSendEmailWithExpectedContentFullAdmissionPayImmediately() {
        Claim claimWithFullAdmission = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionImmediately())
            .withRespondedAt(LocalDateTime.now())
            .build();

        service.notifyStaffDefenceSubmittedFor(claimWithFullAdmission, DEFENDANT_EMAIL);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Pay immediately " + claimWithFullAdmission.getReferenceNumber());
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The defendant has offered to pay immediately in response to the money claim made against them"
        );
    }

    @Test
    public void shouldSendEmailWithExpectedContentFullAdmissionPayBySetDate() {
        Claim claimWithFullAdmission = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionBySpecifiedDate())
            .withRespondedAt(LocalDateTime.now())
            .build();

        service.notifyStaffDefenceSubmittedFor(claimWithFullAdmission, DEFENDANT_EMAIL);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Pay by a set date " + claimWithFullAdmission.getReferenceNumber());
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The defendant has offered to pay by a set date in response to the money claim made against them"
        );
    }

    @Test
    public void shouldSendEmailWithExpectedContentFullAdmissionPayByInstalments() {
        Claim claimWithFullAdmission = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .withRespondedAt(LocalDateTime.now())
            .build();

        service.notifyStaffDefenceSubmittedFor(claimWithFullAdmission, DEFENDANT_EMAIL);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Pay by instalments " + claimWithFullAdmission.getReferenceNumber());
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The defendant has offered to pay by instalments in response to the money claim made against them"
        );
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachments() throws IOException {
        Claim claimWithFullDefenceResponse = SampleClaim.builder()
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(null)
                    .build()
            )
            .withRespondedAt(LocalDateTime.now())
            .build();

        service.notifyStaffDefenceSubmittedFor(claimWithFullDefenceResponse, DEFENDANT_EMAIL);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment emailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        String expectedFileName = buildResponseFileBaseName(claimWithFullDefenceResponse.getReferenceNumber())
            + EXTENSION;

        assertThat(emailAttachment.getContentType()).isEqualTo("application/pdf");
        assertThat(emailAttachment.getFilename()).isEqualTo(expectedFileName);

        byte[] pdfContent = IOUtils.toByteArray(emailAttachment.getData()
            .getInputStream());
        assertThat(pdfContent).isEqualTo(PDF_CONTENT);
    }

}
