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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimantResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

public class ClaimantRejectOrgPaymentPlanStaffNotificationServiceTest extends BaseMockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @MockBean
    protected EmailService emailService;

    @Autowired
    private ClaimantRejectOrgPaymentPlanStaffNotificationService service;
    @Autowired
    private StaffEmailProperties emailProperties;

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
        service.notifyStaffClaimantRejectOrganisationPaymentPlan(null);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        service.notifyStaffClaimantRejectOrganisationPaymentPlan(claim);

        verify(emailService).sendEmail(eq(emailProperties.getSender()), any(EmailData.class));
    }

    @Test
    public void shouldSendEmailWithExpectedContent() {
        service.notifyStaffClaimantRejectOrganisationPaymentPlan(claim);

        verify(emailService).sendEmail(anyString(), emailDataArgument.capture());
        String subject = String.format("Non-individual determination request %s %s v %s",
            claim.getReferenceNumber(),
            claim.getClaimData().getClaimant().getName(),
            claim.getClaimData().getDefendant().getName()
        );

        assertThat(emailDataArgument.getValue().getSubject()).isEqualTo(subject);

        String emailBodyContent = emailDataArgument.getValue().getMessage();

        assertThat(emailBodyContent).startsWith(
                String.format("%s has requested a determination, please transfer to CCBC and enter judgment.",
                    claim.getClaimData().getClaimant().getName()
                )
        );
        assertThat(emailBodyContent).endsWith("This email has been sent from the "
            + "HMCTS Civil Money Claims online court.");
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachmentsForReDetermination() {

        service.notifyStaffClaimantRejectOrganisationPaymentPlan(claim);

        verify(emailService).sendEmail(anyString(), emailDataArgument.capture());

        List<EmailAttachment> attachments = emailDataArgument.getValue().getAttachments();
        assertThat(attachments).hasSize(3);

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
