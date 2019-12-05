package uk.gov.hmcts.cmc.claimstore.deprecated.rpa;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.deprecated.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.DefenceResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssuedNotificationService.JSON_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildJsonResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;

public class DefenceResponseNotificationServiceTest extends MockSpringTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private DefenceResponseNotificationService service;
    @Autowired
    private EmailProperties emailProperties;

    @Captor
    private ArgumentCaptor<String> senderArgument;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    private Claim claim;

    private DefendantResponseEvent event;

    @Before
    public void setUp() {
        claim = SampleClaim
            .builder()
            .withResponse(SampleResponse.validDefaults())
            .withRespondedAt(LocalDateTime.of(2018, 4, 26, 1, 1))
            .build();

        event = new DefendantResponseEvent(claim, "AUTH_CODE");

        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).willReturn(PDF_CONTENT);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyRobotics(null);
    }

    @Test
    public void shouldSendResponseEmailWithConfiguredValues() {

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
        assertThat(emailDataArgument.getValue().getTo()).isEqualTo(emailProperties.getResponseRecipient());
        assertThat(emailDataArgument.getValue().getSubject()).isEqualToIgnoringNewLines("J defence response 000CM001");
        assertThat(emailDataArgument.getValue().getMessage()).isEmpty();
    }

    @Test
    public void shouldSendEmailWithConfiguredValuesAndAttachments() {

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment responsePdfAttachment = emailDataArgument.getValue().getAttachments().get(0);

        String expectedPdfFilename = buildResponseFileBaseName(claim.getReferenceNumber()) + EXTENSION;

        assertThat(responsePdfAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(responsePdfAttachment.getFilename()).isEqualTo(expectedPdfFilename);

        EmailAttachment responseJsonAttachment = emailDataArgument.getValue().getAttachments().get(1);

        String expectedDefenceJsonFilename = buildJsonResponseFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION;

        assertThat(responseJsonAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(responseJsonAttachment.getFilename()).isEqualTo(expectedDefenceJsonFilename);
    }
}
