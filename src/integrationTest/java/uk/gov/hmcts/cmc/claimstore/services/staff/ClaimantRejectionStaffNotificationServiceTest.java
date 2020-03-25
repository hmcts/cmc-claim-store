package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantDirectionsHearingContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.ClaimantRejectPartAdmissionContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClaimantRejectionStaffNotificationServiceTest extends BaseMockSpringTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};
    private Claim claimWithPartAdmission;
    private Claim claimWithIntentionToProceed;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    private ClaimantRejectionStaffNotificationService service;

    @MockBean
    protected EmailService emailService;

    @Autowired
    private StaffPdfCreatorService pdfCreatorService;

    @Autowired
    private ClaimantRejectPartAdmissionContentProvider claimantRejectPartAdmissionContentProvider;

    @Autowired
    private ClaimantDirectionsHearingContentProvider claimantDirectionsHearingContentProvider;

    @Before
    public void beforeEachTest() {
        claimWithPartAdmission = SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation();
        service = new ClaimantRejectionStaffNotificationService(
            emailService,
            emailProperties,
            pdfCreatorService,
            claimantRejectPartAdmissionContentProvider,
            claimantDirectionsHearingContentProvider,
            true);

        claimWithIntentionToProceed = Claim.builder()
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .claimantResponse(
                SampleClaimantResponse.ClaimantResponseRejection.builder()
                    .buildRejectionWithDirectionsQuestionnaire()
            )
            .build();

        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test
    public void shouldSendPartAdmissionRejectionEmailWhenStaffEmailsEnabled() {

        service.notifyStaffClaimantRejectPartAdmission(claimWithPartAdmission);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldNotSendPartAdmissionRejectionEmailWhenStaffEmailsDisabled() {
        service = new ClaimantRejectionStaffNotificationService(
            emailService,
            emailProperties,
            pdfCreatorService,
            claimantRejectPartAdmissionContentProvider,
            claimantDirectionsHearingContentProvider,
            false);

        service.notifyStaffClaimantRejectPartAdmission(claimWithPartAdmission);

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldSendIntentionToProceedEmailWhenStaffEmailsEnabled() {

        service.notifyStaffWithClaimantsIntentionToProceed(claimWithIntentionToProceed);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldNotSendIntentionToProceedEmailWhenStaffEmailsDisabled() {
        service = new ClaimantRejectionStaffNotificationService(
            emailService,
            emailProperties,
            pdfCreatorService,
            claimantRejectPartAdmissionContentProvider,
            claimantDirectionsHearingContentProvider,
            false);

        service.notifyStaffClaimantRejectPartAdmission(claimWithIntentionToProceed);

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldSendEmailWithExpectedContentClaimantRejectionWithPartAdmissionWhenStaffEmailsEnabled() {

        service.notifyStaffClaimantRejectPartAdmission(claimWithPartAdmission);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        String subject = String.format("Partial admission rejected %s %s v %s",
            claimWithPartAdmission.getReferenceNumber(),
            claimWithPartAdmission.getClaimData().getClaimant().getName(),
            claimWithPartAdmission.getClaimData().getDefendant().getName()
        );

        String body = String.format("%s has rejected a partial admission",
            claimWithPartAdmission.getClaimData().getClaimant().getName());

        assertThat(emailDataArgument.getValue()
            .getSubject()).isEqualTo(subject);
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(body);
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachmentsWhenStaffEmailsEnabled() throws IOException {

        service.notifyStaffClaimantRejectPartAdmission(claimWithPartAdmission);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment emailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        assertThat(emailAttachment.getContentType()).isEqualTo("application/pdf");

        byte[] pdfContent = IOUtils.toByteArray(emailAttachment.getData()
            .getInputStream());
        assertThat(pdfContent).isEqualTo(PDF_CONTENT);
    }
}
