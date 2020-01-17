package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssuedNotificationService.JSON_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildJsonMoreTimeRequestedFileBaseName;

public class MoreTimeRequestedNotificationServiceTest extends BaseMockSpringTest {

    @Autowired
    private MoreTimeRequestedNotificationService service;
    @Autowired
    private EmailProperties emailProperties;

    @Captor
    private ArgumentCaptor<String> senderArgument;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @MockBean
    protected EmailService emailService;

    private Claim claim;

    private MoreTimeRequestedEvent event;

    @Before
    public void setUp() {
        claim = SampleClaim
            .builder()
            .withRespondedAt(LocalDateTime.of(2018, 4, 26, 1, 1))
            .build();

        event = new MoreTimeRequestedEvent(claim, LocalDateTime.now().toLocalDate(), "<any-email>");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyRobotics(null);
    }

    @Test
    public void shouldSendEmailWithConfiguredValues() {

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
        assertThat(emailDataArgument.getValue().getTo()).isEqualTo(emailProperties.getResponseRecipient());
        assertThat(emailDataArgument.getValue().getSubject()).isEqualToIgnoringNewLines("J additional time 000CM001");
        assertThat(emailDataArgument.getValue().getMessage()).isEmpty();
    }

    @Test
    public void shouldSendEmailWithConfiguredValuesAndAttachments() {

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment moreTimeReqJsonAttachment = emailDataArgument.getValue().getAttachments().get(0);

        String expectedJson = buildJsonMoreTimeRequestedFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION;

        assertThat(moreTimeReqJsonAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(moreTimeReqJsonAttachment.getFilename()).isEqualTo(expectedJson);
    }
}
