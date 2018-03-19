package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BulkPrintStaffNotificationServiceTest extends MockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private BulkPrintStaffNotificationService service;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    private Document defendantLetterDocument;
    private Document sealedClaimDocument;

    private Claim claim;

    @Before
    public void setUp() {
        defendantLetterDocument = new Document("defendantPinTemplate", new HashMap<>());
        sealedClaimDocument = new Document("sealedClaimTemplate", new HashMap<>());

        claim = SampleClaim
            .builder()
            .build();

        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyFailedBulkPrint(defendantLetterDocument, sealedClaimDocument, null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullDefendantLetterDocument() {
        service.notifyFailedBulkPrint(null, sealedClaimDocument, claim);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullSealedClaimDocument() {
        service.notifyFailedBulkPrint(defendantLetterDocument, null, claim);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        service.notifyFailedBulkPrint(defendantLetterDocument, sealedClaimDocument, claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContent() {
        service.notifyFailedBulkPrint(defendantLetterDocument, sealedClaimDocument, claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Print for claim 000CM001 failed");
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The bulk print for this claim failed, please print and post the pin letter and claim form to the defendant"
        );
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachments() throws IOException {
        service.notifyFailedBulkPrint(defendantLetterDocument, sealedClaimDocument, claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment emailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        String expectedPinLetterFileName
            = DocumentNameUtils.buildDefendantLetterFileBaseName(claim.getReferenceNumber());

        assertThat(emailAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(emailAttachment.getFilename()).isEqualTo(expectedPinLetterFileName);

        EmailAttachment sealedClaimEmailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(1);

        String expectedSealedClaimFileName = DocumentNameUtils.buildSealedClaimFileBaseName(claim.getReferenceNumber());

        assertThat(sealedClaimEmailAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(sealedClaimEmailAttachment.getFilename()).isEqualTo(expectedSealedClaimFileName);
    }
}
