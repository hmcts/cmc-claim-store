package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @MockBean
    private CaseEventsApi caseEventsApi;

    private Claim claim;

    private MoreTimeRequestedEvent event;

    private static final String SERVICE_AUTHORISATION = "service auth";

    private List<CaseEventDetail> caseEventDetailList = new ArrayList<>();

    private final User user = new User("", new UserDetails(null, null, null, null, null));

    @BeforeEach
    public void setUp() {
        claim = SampleClaim
            .builder()
            .withRespondedAt(LocalDateTime.of(2018, 4, 26, 1, 1))
            .build();

        event = new MoreTimeRequestedEvent(claim, LocalDateTime.now().toLocalDate(), "<any-email>");
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(SampleUser.getDefault());
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORISATION);
    }

    @Test
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        assertThrows(NullPointerException.class, () -> {
            service.notifyRobotics(null);
        });
    }

    @Test
    public void shouldSendEmailWithConfiguredValues() {
        when(caseEventsApi.findEventDetailsForCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(caseEventDetailList);

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
        assertThat(emailDataArgument.getValue().getTo()).isEqualTo(emailProperties.getResponseRecipient());
        assertThat(emailDataArgument.getValue().getSubject()).isEqualToIgnoringNewLines("J additional time 000MC001");
        assertThat(emailDataArgument.getValue().getMessage()).isEmpty();
    }

    @Test
    public void shouldSendEmailWithConfiguredValuesAndAttachments() {
        when(caseEventsApi.findEventDetailsForCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(caseEventDetailList);
        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment moreTimeReqJsonAttachment = emailDataArgument.getValue().getAttachments().get(0);

        String expectedJson = buildJsonMoreTimeRequestedFileBaseName(claim.getReferenceNumber()) + JSON_EXTENSION;

        assertThat(moreTimeReqJsonAttachment.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(moreTimeReqJsonAttachment.getFilename()).isEqualTo(expectedJson);
    }

    @Test
    public void shouldSendRPAEmailWithPreviousMoreTimeRequestedOnlineDateTime() {
        caseEventDetailList.add(CaseEventDetail.builder().id(CaseEvent.MORE_TIME_REQUESTED_ONLINE.getValue())
            .eventName(CaseEvent.MORE_TIME_REQUESTED_PAPER.name())
            .createdDate(LocalDateTime.now().minusDays(5)).build());
        when(caseEventsApi.findEventDetailsForCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(caseEventDetailList);

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
        assertThat(emailDataArgument.getValue().getTo()).isEqualTo(emailProperties.getResponseRecipient());
        assertThat(emailDataArgument.getValue().getSubject()).isEqualToIgnoringNewLines("J additional time 000MC001");
        assertThat(emailDataArgument.getValue().getMessage()).isEmpty();
    }

    @Test
    public void shouldSendRPAEmailWithPreviousMoreTimeRequestedViaCCDDateTime() {
        caseEventDetailList.add(CaseEventDetail.builder().id(CaseEvent.RESPONSE_MORE_TIME.getValue())
            .eventName(CaseEvent.RESPONSE_MORE_TIME.name())
            .createdDate(LocalDateTime.now().minusDays(5)).build());
        when(caseEventsApi.findEventDetailsForCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(caseEventDetailList);

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
        assertThat(emailDataArgument.getValue().getTo()).isEqualTo(emailProperties.getResponseRecipient());
        assertThat(emailDataArgument.getValue().getSubject()).isEqualToIgnoringNewLines("J additional time 000MC001");
        assertThat(emailDataArgument.getValue().getMessage()).isEmpty();
    }

    @Test
    public void shouldSendRPAEmailWithNoPreviousMoreTimeRequested() {
        caseEventDetailList.add(CaseEventDetail.builder().id(CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD.getValue())
            .eventName(CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD.name())
            .createdDate(LocalDateTime.now().minusDays(5)).build());
        when(caseEventsApi.findEventDetailsForCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(caseEventDetailList);

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
        assertThat(emailDataArgument.getValue().getTo()).isEqualTo(emailProperties.getResponseRecipient());
        assertThat(emailDataArgument.getValue().getSubject()).isEqualToIgnoringNewLines("J additional time 000MC001");
        assertThat(emailDataArgument.getValue().getMessage()).isEmpty();
    }
}
