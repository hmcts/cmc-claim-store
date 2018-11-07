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
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
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

public class StatesPaidStaffNotificationServiceTest extends MockSpringTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};
    private static final String DEFENDANT_EMAIL = "defendant@mail.com";
    private Claim claimWithFullDefenceResponse;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    @Autowired
    private StatesPaidStaffNotificationService service;

    @Before
    public void beforeEachTest() {
        claimWithFullDefenceResponse = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(null)
                    .build())
            .withRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
            .build();


        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {

        service.notifyStaffClaimantResponseStatesPaidSubmittedFor(claimWithFullDefenceResponse);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContentStatesPaidByFullDefence() {

        service.notifyStaffClaimantResponseStatesPaidSubmittedFor(claimWithFullDefenceResponse);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Civil Money Claim States Paid");
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The defendant has submitted a States Paid defence."
        );
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachments() throws IOException {


        service.notifyStaffClaimantResponseStatesPaidSubmittedFor(claimWithFullDefenceResponse);

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
