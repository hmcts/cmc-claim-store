package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.StatesPaidEmailContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.EmailContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;

public class StatesPaidStaffNotificationServiceTest extends BaseMockSpringTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};
    private Claim claimWithFullDefenceResponse;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    @Autowired
    private StatesPaidStaffNotificationService service;

    @MockBean
    protected EmailService emailService;

    @Mock
    private StatesPaidEmailContentProvider emailContentProvider;

    @Autowired
    private DefendantResponseReceiptService defendantResponseReceiptService;

    @BeforeEach
    public void beforeEachTest() {

        claimWithFullDefenceResponse = SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation();

        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test
    public void shouldSendEmailToExpectedRecipientStaffEmailsEnabled() {
        StatesPaidStaffNotificationService staffNotificationService;
        staffNotificationService = new StatesPaidStaffNotificationService(
            emailService, emailProperties, emailContentProvider, defendantResponseReceiptService, true);

        when(emailContentProvider.createContent(anyMap())).thenReturn(new EmailContent("subject", "body"));

        staffNotificationService.notifyStaffClaimantResponseStatesPaidSubmittedFor(claimWithFullDefenceResponse);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldNotSendEmailToExpectedRecipientStaffEmailsDisabled() {

        StatesPaidStaffNotificationService staffNotificationService;
        staffNotificationService = new StatesPaidStaffNotificationService(
            emailService, emailProperties, emailContentProvider, defendantResponseReceiptService, false);

        staffNotificationService.notifyStaffClaimantResponseStatesPaidSubmittedFor(claimWithFullDefenceResponse);

        verify(emailContentProvider, never()).createContent(anyMap());
        verify(emailService, never()).sendEmail(anyString(), any(EmailData.class));
    }

    @Test
    public void shouldSendEmailWithExpectedContentStatesPaidByFullDefence() {

        service.notifyStaffClaimantResponseStatesPaidSubmittedFor(claimWithFullDefenceResponse);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        String subject = String.format("Civil Money Claim States Paid Claimant Response: %s v %s %s",
            claimWithFullDefenceResponse.getClaimData().getClaimant().getName(),
            claimWithFullDefenceResponse.getClaimData().getDefendant().getName(),
            claimWithFullDefenceResponse.getReferenceNumber()
        );

        assertThat(emailDataArgument.getValue()
            .getSubject()).isEqualTo(subject);
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
