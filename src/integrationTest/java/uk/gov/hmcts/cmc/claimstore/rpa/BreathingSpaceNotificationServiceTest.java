package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepresentative;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.rpa.BreathingSpaceNotificationService.JSON_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildJsonClaimFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

public class BreathingSpaceNotificationServiceTest extends BaseMockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private BreathingSpaceNotificationService service;
    @Autowired
    private EmailProperties emailProperties;

    @MockBean
    protected EmailService emailService;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    private Claim claim;
    private final List<PDF> documents = new ArrayList<>();

    @Before
    public void setUp() {
        claim = SampleClaim.getDefaultWithBreathingSpaceDetails();

        PDF sealedClaimDoc = new PDF(buildSealedClaimFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            SEALED_CLAIM);
        PDF defendantLetterDoc = new PDF(buildDefendantLetterFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            DEFENDANT_PIN_LETTER);

        documents.add(defendantLetterDoc);
        documents.add(sealedClaimDoc);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyRobotics(null, documents);
    }

    @Test
    public void shouldSendEmailFromConfiguredSender() {
        service.notifyRobotics(claim, documents);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailToCitizenClaimRecipient() {
        service.notifyRobotics(claim, documents);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getTo()).isEqualTo(emailProperties.getSealedClaimRecipient());
    }

    @Test
    public void shouldSendEmailWithCitizenContent() {
        service.notifyRobotics(claim, documents);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getSubject())
            .isEqualToIgnoringNewLines("J Breathing Space CCD Notification 000MC001");
        assertThat(emailDataArgument.getValue().getMessage()).isEqualToIgnoringNewLines("Please find attached claim.");
    }

    @Test
    public void shouldSendEmailWithCitizenSealedPDFAttachments() {
        service.notifyRobotics(claim, documents);

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

    private Claim getLegalSealedClaim() {
        return SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withExternalId(SampleClaim.RAND_UUID)
                .withExternalReferenceNumber("LBA/UM1616668")
                .withAmount(SampleAmountRange.builder().build())
                .clearDefendants()
                .clearClaimants()
                .withClaimant(SampleParty.builder()
                    .withPhone("(0)207 127 0000")
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .individual())
                .withDefendant(SampleTheirDetails.builder()
                    .withRepresentative(SampleRepresentative.builder()
                        .organisationContactDetails(ContactDetails.builder()
                            .dxAddress("xyzrt")
                            .email("abn@gmail.com")
                            .phone("01123456789")
                            .build())
                        .build())
                    .individualDetails())
                .build()
            )
            .withReferenceNumber("006LR003")
            .build();
    }
}
