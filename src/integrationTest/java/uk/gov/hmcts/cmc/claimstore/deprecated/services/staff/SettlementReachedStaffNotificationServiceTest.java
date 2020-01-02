package uk.gov.hmcts.cmc.claimstore.deprecated.services.staff;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.deprecated.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.staff.SettlementReachedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SettlementReachedStaffNotificationServiceTest extends MockSpringTest {

    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Autowired
    private SettlementReachedStaffNotificationService service;

    @Captor
    private ArgumentCaptor<String> senderArgument;
    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    private Claim claim;

    @Before
    public void setup() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        claim = SampleClaim
            .builder()
            .withSettlementReachedAt(LocalDateTime.now())
            .withResponse(SampleResponse.validDefaults())
            .withSettlement(settlement)
            .build();
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_CONTENT);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifySettlementReached(null);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        service.notifySettlementReached(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContent() {
        service.notifySettlementReached(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Offer accepted");
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The claimant has accepted the defendant’s offer to settle their claim"
        );
    }

    @Test
    public void shouldSendEmailWithExpectedPDFAttachments() throws IOException {
        service.notifySettlementReached(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        EmailAttachment emailAttachment = emailDataArgument.getValue()
            .getAttachments()
            .get(0);

        String expectedFileName = String.format(
            SettlementReachedStaffNotificationService.FILE_NAME_FORMAT,
            claim.getReferenceNumber()
        );

        assertThat(emailAttachment.getContentType()).isEqualTo("application/pdf");
        assertThat(emailAttachment.getFilename()).isEqualTo(expectedFileName);
    }
}
