package uk.gov.hmcts.cmc.claimstore.services.rpa;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.services.rpa.ClaimIssuedRpaNotificationService.JSON_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildJsonClaimFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

public class ClaimIssuedRpaNotificationServiceTest extends MockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private ClaimIssuedRpaNotificationService service;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    private Claim claim;
    private DocumentGeneratedEvent event;

    @Before
    public void setUp() {

        claim = SampleClaim
            .builder()
            .build();

        PDF sealedClaimDocument = new PDF(buildSealedClaimFileBaseName(claim.getReferenceNumber()), PDF_CONTENT);

        PDF defendantLetterDocument
            = new PDF(buildDefendantLetterFileBaseName(claim.getReferenceNumber()), PDF_CONTENT);

        event = new DocumentGeneratedEvent(claim, "AUTH_CODE", defendantLetterDocument, sealedClaimDocument);

        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyRobotOfClaimIssue(null);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        service.notifyRobotOfClaimIssue(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContent() {
        service.notifyRobotOfClaimIssue(event);
        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("J new claim 000CM001");
        assertThat(emailDataArgument.getValue()
            .getMessage()).isNull();
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachments() throws IOException {
        service.notifyRobotOfClaimIssue(event);
        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());


        EmailAttachment sealedClaimEmailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        String expectedSealedClaimFileName = buildSealedClaimFileBaseName(claim.getReferenceNumber()) + EXTENSION;

        assertThat(sealedClaimEmailAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(sealedClaimEmailAttachment.getFilename()).isEqualTo(expectedSealedClaimFileName);

        EmailAttachment emailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(1);

        String expectedJsonFileName = buildJsonClaimFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION;

        assertThat(emailAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(emailAttachment.getFilename()).isEqualTo(expectedJsonFileName);
    }
}
