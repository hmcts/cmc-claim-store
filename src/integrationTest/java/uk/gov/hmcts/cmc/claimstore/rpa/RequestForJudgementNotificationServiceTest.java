package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.config.EmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.cmc.claimstore.documents.output.PDF.EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssuedNotificationService.JSON_EXTENSION;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildJsonRequestForJudgementFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgementFileBaseName;

public class RequestForJudgementNotificationServiceTest extends BaseMockSpringTest {
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private RequestForJudgementNotificationService service;
    @Autowired
    private EmailProperties emailProperties;

    @MockBean
    protected EmailService emailService;

    @Captor
    private ArgumentCaptor<String> senderArgument;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    private Claim claim;

    private CountyCourtJudgmentEvent event;

    private static final CountyCourtJudgment DEFAULT_CCJ = SampleCountyCourtJudgment
        .builder()
        .ccjType(CountyCourtJudgmentType.DEFAULT)
        .build();

    private static final CountyCourtJudgment CCJ_BY_ADMISSION = SampleCountyCourtJudgment
        .builder()
        .ccjType(CountyCourtJudgmentType.ADMISSIONS)
        .build();

    private static final CountyCourtJudgment CCJ_BY_DETERMINATION = SampleCountyCourtJudgment
        .builder()
        .ccjType(CountyCourtJudgmentType.DETERMINATION)
        .build();

    @Before
    public void setUp() {

        claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(LocalDate.of(2018, 4, 26).atStartOfDay())
            .withCountyCourtJudgment(DEFAULT_CCJ)
            .build();

        event = new CountyCourtJudgmentEvent(claim, "AUTH_CODE");

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
        assertThat(emailDataArgument.getValue().getSubject())
            .isEqualToIgnoringNewLines("J default judgement request 000CM001");
        assertThat(emailDataArgument.getValue().getMessage()).isEmpty();
    }

    @Test
    public void shouldSendEmailWithConfiguredValuesAndAttachments() {

        service.notifyRobotics(event);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment ccjPdfAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        String expectedPdfFilename = buildRequestForJudgementFileBaseName(claim.getReferenceNumber(),
            claim.getClaimData().getDefendant().getName()) + EXTENSION;

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

    @Test
    public void shouldNotSendRoboticsEmailWhenCCJByAdmission() {
        Claim claimWithCCJByAdmission = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(LocalDate.of(2018, 4, 26).atStartOfDay())
            .withCountyCourtJudgment(CCJ_BY_ADMISSION)
            .build();

        CountyCourtJudgmentEvent event = new CountyCourtJudgmentEvent(claimWithCCJByAdmission, "AUTH_CODE");

        service.notifyRobotics(event);

        verifyNoInteractions(emailService);
    }

    @Test
    public void shouldNotSendRoboticsEmailWhenCCJByDetermination() {
        Claim claimWithCCJByDetermination = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(LocalDate.of(2018, 4, 26).atStartOfDay())
            .withCountyCourtJudgment(CCJ_BY_DETERMINATION)
            .build();

        CountyCourtJudgmentEvent event = new CountyCourtJudgmentEvent(claimWithCCJByDetermination,
            "AUTH_CODE");

        service.notifyRobotics(event);

        verifyNoInteractions(emailService);
    }
}
