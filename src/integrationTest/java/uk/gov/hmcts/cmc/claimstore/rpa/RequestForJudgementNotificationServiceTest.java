package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssuedNotificationService.JSON_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildJsonRequestForJudgementFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;

public class RequestForJudgementNotificationServiceTest extends MockSpringTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private RequestForJudgementNotificationService service;
    @Autowired
    private EmailProperties emailProperties;

    @MockBean
    private CCJStaffNotificationService ccjStaffNotificationService;

    @Captor
    private ArgumentCaptor<String> senderArgument;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    private Claim claim;

    private CountyCourtJudgmentRequestedEvent event;

    @Before
    public void setUp() {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder().build();

        claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(LocalDate.of(2018, 4, 26).atStartOfDay())
            .withCountyCourtJudgment(countyCourtJudgment)
            .build();

        event = new CountyCourtJudgmentRequestedEvent(claim, "AUTH_CODE");

    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyRobotics(null);
    }

    @Test
    public void shouldSendResponseEmailWithConfiguredValues() {
        countyCourtJudgementStub();

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());
        verify(ccjStaffNotificationService).generateCountyCourtJudgmentPdf(claimArgumentCaptor.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
        assertThat(emailDataArgument.getValue().getTo()).isEqualTo(emailProperties.getResponseRecipient());
        assertThat(emailDataArgument.getValue().getSubject())
            .isEqualToIgnoringNewLines("J default judgement request 000CM001");
        assertThat(emailDataArgument.getValue().getMessage()).isEmpty();
    }

    @Test
    public void shouldSendEmailWithConfiguredValuesAndAttachments() {
        countyCourtJudgementStub();

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment ccjPdfAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        String expectedPdfFilename = buildResponseFileBaseName(claim.getReferenceNumber()) + EXTENSION;

        assertThat(ccjPdfAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(ccjPdfAttachment.getFilename()).isEqualTo(expectedPdfFilename);

        EmailAttachment ccjJsonAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(1);

        String expectedCcjJsonFilename = buildJsonRequestForJudgementFileBaseName(claim.getReferenceNumber())
            + JSON_EXTENSION;

        assertThat(ccjJsonAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(ccjJsonAttachment.getFilename()).isEqualTo(expectedCcjJsonFilename);
    }

    private void countyCourtJudgementStub() {
        when(ccjStaffNotificationService.generateCountyCourtJudgmentPdf(claim))
            .thenReturn(EmailAttachment.pdf(PDF_CONTENT,
                buildResponseFileBaseName(claim.getReferenceNumber()) + EXTENSION));
    }

}
