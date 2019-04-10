package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssueNotificationService.JSON_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildJsonClaimFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

public class ClaimIssueNotificationServiceTest extends MockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private ClaimIssueNotificationService service;
    @Autowired
    private EmailProperties emailProperties;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    private Claim claim;
    private DocumentGeneratedEvent event;

    @Before
    public void setUp() {
        claim = SampleClaim.getDefault();

        PDF sealedClaimDoc = new PDF(buildSealedClaimFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            SEALED_CLAIM);
        PDF defendantLetterDoc = new PDF(buildDefendantLetterFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            DEFENDANT_PIN_LETTER);

        event = new DocumentGeneratedEvent(claim, "AUTH_CODE", defendantLetterDoc, sealedClaimDoc);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyRobotics(null);
    }

    @Test
    public void shouldSendEmailFromConfiguredSender() {
        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailToConfiguredRecipient() {
        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getTo()).isEqualTo(emailProperties.getSealedClaimRecipient());
    }

    @Test
    public void shouldSendEmailWithContent() {
        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getSubject()).isEqualToIgnoringNewLines("J new claim 000CM001");
        assertThat(emailDataArgument.getValue().getMessage()).isEqualToIgnoringNewLines("Please find attached claim.");
    }

    @Test
    public void shouldSendEmailWithPDFAttachments() {
        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment sealedClaimEmailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        String expectedPdfFilename = buildSealedClaimFileBaseName(claim.getReferenceNumber()) + EXTENSION;

        assertThat(sealedClaimEmailAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(sealedClaimEmailAttachment.getFilename()).isEqualTo(expectedPdfFilename);

        EmailAttachment emailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(1);

        String expectedJsonFilename = buildJsonClaimFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION;

        assertThat(emailAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(emailAttachment.getFilename()).isEqualTo(expectedJsonFilename);
    }
}
