package uk.gov.hmcts.cmc.claimstore.deprecated.services.staff;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.deprecated.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.staff.RejectSettlementAgreementStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.email.EmailData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class RejectSettlementAgreementStaffNotificationServiceTest extends MockSpringTest {

    @Autowired
    private RejectSettlementAgreementStaffNotificationService service;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Autowired
    private StaffEmailProperties emailProperties;

    private Claim claim;

    @Before
    public void setup() {
        claim = SampleClaim.getClaimWithSettlementAgreementRejected();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifySettlementRejected(null);
    }

    @Test
    public void shouldSendEmailToExpectedRecipient() {
        service.notifySettlementRejected(claim);

        verify(emailService).sendEmail(eq(emailProperties.getSender()), any(EmailData.class));
    }

    @Test
    public void shouldSendEmailWithExpectedContent() {
        service.notifySettlementRejected(claim);

        verify(emailService).sendEmail(anyString(), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue()
            .getSubject()).startsWith("Settlement Agreement rejected");
        assertThat(emailDataArgument.getValue()
            .getMessage()).startsWith(
            "The defendant has rejected the claimant's offer to settle their claim"
        );
    }
}
