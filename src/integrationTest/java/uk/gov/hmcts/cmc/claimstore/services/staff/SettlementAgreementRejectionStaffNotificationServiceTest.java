package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.email.EmailData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class SettlementAgreementRejectionStaffNotificationServiceTest extends MockSpringTest {

    @Autowired
    private SettlementAgreementRejectedStaffNotificationService service;

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
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.CLAIMANT);
        settlement.acceptCourtDetermination(MadeBy.CLAIMANT);
        settlement.reject(MadeBy.DEFENDANT);

        claim = SampleClaim
            .builder()
            .withResponse(SampleResponse.validDefaults())
            .withSettlement(settlement)
            .build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifySettlementRejected(null);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        service.notifySettlementRejected(claim);

        verify(emailService).sendEmail(senderArgument.capture(), emailDataArgument.capture());

        assertThat(senderArgument.getValue()).isEqualTo(emailProperties.getSender());
    }

    @Test
    public void shouldSendEmailWithExpectedContent() {
        service.notifySettlementRejected(claim);

        verify(emailService).sendEmail(eq(emailProperties.getSender()), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Settlement Agreement rejected");
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The defendant has rejected the claimant's offer to settle their claim"
        );
    }
}
