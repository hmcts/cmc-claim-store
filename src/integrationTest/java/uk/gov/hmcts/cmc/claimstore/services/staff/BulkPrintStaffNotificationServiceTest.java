package uk.gov.hmcts.cmc.claimstore.services.staff;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;

public class BulkPrintStaffNotificationServiceTest extends BaseMockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private BulkPrintStaffNotificationService service;
    @Autowired
    private StaffEmailProperties emailProperties;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @MockBean
    protected EmailService emailService;

    private PrintableTemplate defendantLetterDocument;
    private PrintableTemplate sealedClaimDocument;

    private Claim claim;

    @Before
    public void setUp() {
        defendantLetterDocument =
            new PrintableTemplate(
                new Document("defendantPinTemplate", new HashMap<>()),
                "000CM001-defendant-pin-letter"
            );
        sealedClaimDocument = new PrintableTemplate(
            new Document("sealedClaimTemplate", new HashMap<>()),
            "000CM001-claim-form"
        );
        claim = SampleClaim
            .builder()
            .build();

        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyFailedBulkPrint(
            ImmutableList.of(defendantLetterDocument, sealedClaimDocument),
            null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullDefendantLetterDocument() {
        //noinspection ConstantConditions
        service.notifyFailedBulkPrint(
            ImmutableList.of(null, sealedClaimDocument),
            claim);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullSealedClaimDocument() {
        //noinspection ConstantConditions
        service.notifyFailedBulkPrint(
            ImmutableList.of(defendantLetterDocument, null),
            claim);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        service.notifyFailedBulkPrint(
            ImmutableList.of(defendantLetterDocument, sealedClaimDocument),
            claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContent() {
        service.notifyFailedBulkPrint(
            ImmutableList.of(defendantLetterDocument, sealedClaimDocument),
            claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Print for claim 000CM001 failed");
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The bulk print for this claim failed, please print and post the attached documents"
        );
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachments() {
        service.notifyFailedBulkPrint(
            ImmutableList.of(defendantLetterDocument, sealedClaimDocument),
            claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment emailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        String expectedPinLetterFileName
            = DocumentNameUtils.buildDefendantLetterFileBaseName(claim.getReferenceNumber()) + EXTENSION;

        assertThat(emailAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(emailAttachment.getFilename()).isEqualTo(expectedPinLetterFileName);

        EmailAttachment sealedClaimEmailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(1);

        String expectedSealedClaimFileName = DocumentNameUtils.buildSealedClaimFileBaseName(claim.getReferenceNumber())
            + EXTENSION;

        assertThat(sealedClaimEmailAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(sealedClaimEmailAttachment.getFilename()).isEqualTo(expectedSealedClaimFileName);
    }
}
