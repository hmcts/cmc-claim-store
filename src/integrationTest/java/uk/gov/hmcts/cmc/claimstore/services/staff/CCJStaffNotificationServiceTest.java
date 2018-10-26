package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimantResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

public class CCJStaffNotificationServiceTest extends MockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private CCJStaffNotificationService service;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    private Claim claim;

    @Before
    public void setup() {
        claim = SampleClaim
            .builder()
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .paymentOption(PaymentOption.IMMEDIATELY)
                .build())
            .withClaimData(SampleClaimData.submittedByClaimant())
            .build();
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyStaffCCJRequestSubmitted(null);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        service.notifyStaffCCJRequestSubmitted(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContent() {
        service.notifyStaffCCJRequestSubmitted(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Civil money claims:");
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The claimant has asked for a County Court Judgment to be made against the defendant."
        );
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachmentsForDefaultCCJRequest() throws IOException {
        service.notifyStaffCCJRequestSubmitted(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment emailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        String expectedFileName = String.format(
            CCJStaffNotificationService.FILE_NAME_FORMAT,
            claim.getReferenceNumber(),
            claim.getClaimData().getDefendant().getName()
        );

        assertThat(emailAttachment.getContentType()).isEqualTo("application/pdf");
        assertThat(emailAttachment.getFilename()).isEqualTo(expectedFileName);
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachmentsForReDetermination() throws IOException {
        claim = SampleClaim
            .builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .withRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                .buildAcceptationIssueCCJWithCourtDetermination()
            )
            .withClaimantRespondedAt(LocalDateTime.now())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .paymentOption(PaymentOption.IMMEDIATELY)
                .build())
            .withClaimData(SampleClaimData.submittedByClaimant())
            .build();

        service.notifyStaffCCJReDeterminationRequest(claim, "Michael George");

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        List<EmailAttachment> attachments = emailDataArgument.getValue().getAttachments();
        assertThat(attachments.size()).isEqualTo(3);

        EmailAttachment claimEmailAttachment = attachments.get(0);
        String sealedClaimFileName = buildSealedClaimFileBaseName(claim.getReferenceNumber()) + PDF.PDF;
        assertThat(claimEmailAttachment.getContentType()).isEqualTo("application/pdf");
        assertThat(claimEmailAttachment.getFilename()).isEqualTo(sealedClaimFileName);

        EmailAttachment responseEmailAttachment = attachments.get(1);
        String responseFileName = buildResponseFileBaseName(claim.getReferenceNumber()) + PDF.PDF;
        assertThat(responseEmailAttachment.getContentType()).isEqualTo("application/pdf");
        assertThat(responseEmailAttachment.getFilename()).isEqualTo(responseFileName);

        EmailAttachment claimantResponseEmailAttachment = attachments.get(2);
        String claimantResponseFileName = buildClaimantResponseFileBaseName(claim.getReferenceNumber()) + PDF.PDF;
        assertThat(claimantResponseEmailAttachment.getContentType()).isEqualTo("application/pdf");
        assertThat(claimantResponseEmailAttachment.getFilename()).isEqualTo(claimantResponseFileName);
    }
}
