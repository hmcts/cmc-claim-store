package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimantResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

public class InterlocutoryJudgmentStaffNotificationServiceTest extends BaseMockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private InterlocutoryJudgmentStaffNotificationService service;
    @Autowired
    private StaffEmailProperties emailProperties;

    @MockBean
    protected EmailService emailService;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    private Claim claim;

    @Before
    public void setup() {
        claim = SampleClaim.getWithClaimantResponse();
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyStaffInterlocutoryJudgmentSubmitted(null);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        service.notifyStaffInterlocutoryJudgmentSubmitted(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContent() {
        service.notifyStaffInterlocutoryJudgmentSubmitted(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());
        String subject = String.format("Redetermination request %s %s v %s",
            claim.getReferenceNumber(),
            claim.getClaimData().getClaimant().getName(),
            claim.getClaimData().getDefendant().getName()
        );
        assertThat(emailDataArgument.getValue()
            .getSubject()).isEqualTo(subject);
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            String.format("%s has requested a redetermination, please refer the attached to a District Judge.",
                claim.getClaimData().getClaimant().getName()
            )
        );
        assertThat(emailDataArgument.getValue()
            .getMessage()).contains("Please issue an interlocutory judgement to be made against the defendant & "
            + "re-determination by District Judge.");
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachmentsForReDetermination() {

        service.notifyStaffInterlocutoryJudgmentSubmitted(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        List<EmailAttachment> attachments = emailDataArgument.getValue().getAttachments();
        assertThat(attachments.size()).isEqualTo(3);

        EmailAttachment claimEmailAttachment = attachments.get(0);
        String sealedClaimFileName = buildSealedClaimFileBaseName(claim.getReferenceNumber()) + PDF.EXTENSION;
        assertThat(claimEmailAttachment.getContentType()).isEqualTo("application/pdf");
        assertThat(claimEmailAttachment.getFilename()).isEqualTo(sealedClaimFileName);

        EmailAttachment responseEmailAttachment = attachments.get(1);
        String responseFileName = buildResponseFileBaseName(claim.getReferenceNumber()) + PDF.EXTENSION;
        assertThat(responseEmailAttachment.getContentType()).isEqualTo("application/pdf");
        assertThat(responseEmailAttachment.getFilename()).isEqualTo(responseFileName);

        EmailAttachment claimantResponseEmailAttachment = attachments.get(2);
        String claimantResponseFileName = buildClaimantResponseFileBaseName(claim.getReferenceNumber()) + PDF.EXTENSION;
        assertThat(claimantResponseEmailAttachment.getContentType()).isEqualTo("application/pdf");
        assertThat(claimantResponseEmailAttachment.getFilename()).isEqualTo(claimantResponseFileName);
    }
}
